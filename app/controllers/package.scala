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

import controllers.actions.AuthorisedAction
import models.common.{BusinessId, JourneyContextWithNino, Nino, TaxYear}
import models.domain.ApiResultT
import models.error.ServiceError
import models.error.ServiceError.{CannotParseJsonError, CannotReadJsonError}
import models.frontend.FrontendAnswers
import play.api.Logger
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

package object controllers {

  def handleOptionalApiResult[A: Writes](result: ApiResultT[Option[A]])(implicit ec: ExecutionContext, logger: Logger): Future[Result] = {
    val resultT = result.map(r => r.fold(NoContent)(_ => Ok(Json.toJson(r))))
    handleResultT(resultT)
  }

  def handleApiResultT[A: Writes](result: ApiResultT[A])(implicit ec: ExecutionContext, logger: Logger): Future[Result] = {
    val resultT = result.map(r => Ok(Json.toJson(r)))
    handleResultT(resultT)
  }

  def handleApiUnitResultT(result: ApiResultT[Unit])(implicit ec: ExecutionContext, logger: Logger): Future[Result] = {
    val resultT = result.map(_ => NoContent)
    handleResultT(resultT)
  }

  private def handleResultT(result: ApiResultT[Result])(implicit ec: ExecutionContext, logger: Logger): Future[Result] =
    result.leftMap { error =>
      handleError(error)
    }.merge

  // TODO Implement proper error handling, right we assume error status == downstream status, or Internal Server Error if it's different type of error
  private def handleError(error: ServiceError)(implicit logger: Logger) = {
    logger.error(s"HttpError encountered: ${error.errorMessage}")
    Status(error.status)(error.errorMessage)
  }

  def getBody[A: Reads](user: AuthorisedAction.User[AnyContent])(
      invokeBlock: A => ApiResultT[Result])(implicit ec: ExecutionContext, logger: Logger): Future[Result] =
    parseBody[A](user) match {
      case Success(validatedRes) =>
        validatedRes.fold[Future[Result]](Future.successful(BadRequest)) {
          case JsSuccess(value, _) => handleResultT(invokeBlock(value))
          case JsError(err)        => Future.successful(toBadRequest(CannotReadJsonError(err.toList)))
        }
      case Failure(err) => Future.successful(toBadRequest(CannotParseJsonError(err)))
    }

  def getCapitalAllowanceBodyWithCtx[A, B <: FrontendAnswers[A]: Reads](taxYear: TaxYear, businessId: BusinessId, nino: Nino)(
      invokeBlock: (JourneyContextWithNino, B) => ApiResultT[Result])(implicit
      ec: ExecutionContext,
      logger: Logger,
      request: AuthorisedAction.User[AnyContent]): Future[Result] = {
    val ctx = JourneyContextWithNino(taxYear, businessId, request.getMtditid, nino)
    getBody(request)(invokeBlock(ctx, _))
  }
  def getBodyWithCtx[A: Reads](taxYear: TaxYear, businessId: BusinessId, nino: Nino)(
      invokeBlock: (JourneyContextWithNino, A) => ApiResultT[Result])(implicit
      ec: ExecutionContext,
      logger: Logger,
      request: AuthorisedAction.User[AnyContent]): Future[Result] = {
    val ctx = JourneyContextWithNino(taxYear, businessId, request.getMtditid, nino)
    getBody(request)(invokeBlock(ctx, _))
  }

  private def parseBody[A: Reads](user: AuthorisedAction.User[AnyContent]): Try[Option[JsResult[A]]] =
    Try(user.body.asJson.map(_.validate[A]))

  private def toBadRequest(error: ServiceError)(implicit logger: Logger): Result = {
    logger.error(s"Bad Request: ${error.errorMessage}")
    BadRequest(Json.obj("code" -> BAD_REQUEST, "reason" -> error.errorMessage))
  }

}
