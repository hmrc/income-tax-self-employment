/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.actions

import common.{DelegatedAuthRules, EnrolmentIdentifiers, EnrolmentKeys}
import controllers.actions.AuthorisedAction.User
import models.common.Mtditid
import play.api.Logger
import play.api.mvc.Results.{InternalServerError, Unauthorized}
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedAction @Inject() ()(implicit
    val authConnector: AuthConnector,
    defaultActionBuilder: DefaultActionBuilder,
    val cc: ControllerComponents)
    extends AuthorisedFunctions {

  lazy val logger: Logger                         = Logger.apply(this.getClass)
  implicit val executionContext: ExecutionContext = cc.executionContext

  private val unauthorized: Future[Result] = Future(Unauthorized)

  def async(block: User[AnyContent] => Future[Result]): Action[AnyContent] = defaultActionBuilder.async { implicit request =>
    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    request.headers
      .get("mtditid")
      .fold {
        logger.warn("[AuthorisedAction][async] - No MTDITID in the header. Returning unauthorised.")
        unauthorized
      }(mtdItId =>
        authorised().retrieve(affinityGroup) {
          case Some(AffinityGroup.Agent) => agentAuthentication(block, mtdItId)(request, headerCarrier)
          case _                         => individualAuthentication(block, mtdItId)(request, headerCarrier)
        } recover {
          case ex: NoActiveSession =>
            logger.info(s"[AuthorisedAction][async] - No active session. Reason: ${ex.getMessage}")
            Unauthorized
          case _: AuthorisationException =>
            logger.info(s"[AuthorisedAction][async] - User failed to authenticate")
            Unauthorized
          case e =>
            logger.error(s"[AuthorisedAction][async] - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
            InternalServerError
        })
  }

  private[actions] def individualAuthentication[A](block: User[A] => Future[Result], requestMtdItId: String)(implicit
      request: Request[A],
      hc: HeaderCarrier): Future[Result] =
    authorised().retrieve(allEnrolments and confidenceLevel) {
      case enrolments ~ userConfidence if userConfidence.level >= ConfidenceLevel.L250.level =>
        val optionalMtdItId: Option[String] = enrolmentGetIdentifierValue(EnrolmentKeys.Individual, EnrolmentIdentifiers.individualId, enrolments)
        val optionalNino: Option[String]    = enrolmentGetIdentifierValue(EnrolmentKeys.nino, EnrolmentIdentifiers.nino, enrolments)

        (optionalMtdItId, optionalNino) match {
          case (Some(authMTDITID), Some(_)) =>
            enrolments.enrolments.collectFirst {
              case Enrolment(EnrolmentKeys.Individual, enrolmentIdentifiers, _, _)
                  if enrolmentIdentifiers.exists(identifier =>
                    identifier.key == EnrolmentIdentifiers.individualId && identifier.value == requestMtdItId) =>
                block(User(requestMtdItId, None))
            } getOrElse {
              logger.info(
                s"[AuthorisedAction][individualAuthentication] Non-agent with an invalid MTDITID. " +
                  s"MTDITID in auth matches MTDITID in request: ${authMTDITID == requestMtdItId}")
              unauthorized
            }
          case (_, None) =>
            logger.info(s"[AuthorisedAction][individualAuthentication] - User has no nino.")
            unauthorized
          case (None, _) =>
            logger.info(s"[AuthorisedAction][individualAuthentication] - User has no MTD IT enrolment.")
            unauthorized
        }
      case _ =>
        logger.info("[AuthorisedAction][individualAuthentication] User has confidence level below 250.")
        unauthorized
    }

  private def agentAuthPredicate(mtdId: String): Predicate =
    Enrolment(EnrolmentKeys.Individual)
      .withIdentifier(EnrolmentIdentifiers.individualId, mtdId)
      .withDelegatedAuthRule(DelegatedAuthRules.agentDelegatedAuthRule)

  private[actions] def agentAuthentication[A](block: User[A] => Future[Result], mtdItId: String)(implicit
      request: Request[A],
      hc: HeaderCarrier): Future[Result] =
    authorised(agentAuthPredicate(mtdItId))
      .retrieve(allEnrolments) {
        populateAgent(block, mtdItId, _)
      }
      .recoverWith(agentRecovery())

  private val agentAuthLogString: String = "[AuthorisedAction][agentAuthentication]"

  private def agentRecovery(): PartialFunction[Throwable, Future[Result]] = {
    case _: NoActiveSession =>
      logger.info(s"$agentAuthLogString - No active session.")
      unauthorized
    case _: AuthorisationException =>
      logger.info(s"$agentAuthLogString - Agent does not have delegated authority for Client.")
      unauthorized
    case e =>
      logger.error(s"$agentAuthLogString - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
      Future.successful(InternalServerError)
  }

  private def populateAgent[A](block: User[A] => Future[Result], mtdItId: String, enrolments: Enrolments)(implicit request: Request[A]) =
    enrolmentGetIdentifierValue(EnrolmentKeys.Agent, EnrolmentIdentifiers.agentReference, enrolments) match {
      case Some(arn) =>
        block(User(mtdItId, Some(arn)))
      case None =>
        logger.info("[AuthorisedAction][agentAuthentication] Agent with no HMRC-AS-AGENT enrolment.")
        unauthorized
    }

  private[actions] def enrolmentGetIdentifierValue(checkedKey: String, checkedIdentifier: String, enrolments: Enrolments): Option[String] =
    enrolments.enrolments.collectFirst { case Enrolment(`checkedKey`, enrolmentIdentifiers, _, _) =>
      enrolmentIdentifiers.collectFirst { case EnrolmentIdentifier(`checkedIdentifier`, identifierValue) =>
        identifierValue
      }
    }.flatten

}

object AuthorisedAction {
  case class User[T](mtditid: String, arn: Option[String])(implicit val request: Request[T]) extends WrappedRequest[T](request) {
    def getMtditid: Mtditid = Mtditid(mtditid)
  }
}
