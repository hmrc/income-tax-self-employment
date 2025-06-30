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

package mocks

import common._
import controllers.actions.AuthorisedAction
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils.defaultActionBuilder

import scala.concurrent.{ExecutionContext, Future}

trait MockAuth extends TestSuite with MockFactory {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  protected val individualEnrolments: Enrolments = Enrolments(
    Set(
      Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
      Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, "1234567890")), "Activated")
    ))

  (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
    .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
    .returns(Future.successful(individualEnrolments and ConfidenceLevel.L250))

  (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
    .expects(*, Retrievals.affinityGroup, *, *)
    .returning(Future.successful(Some(AffinityGroup.Individual)))

  protected val mockAuthorisedAction = new AuthorisedAction()(mockAuthConnector, defaultActionBuilder, stubControllerComponents())

}
