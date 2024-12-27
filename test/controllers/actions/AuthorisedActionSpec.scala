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

import common._
import controllers.actions.AuthorisedAction.User
import mocks.MockAppConfig
import play.api.http.Status._
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.{ExecutionContext, Future}

class AuthorisedActionSpec extends TestUtils with MockAppConfig {

  override val mockAuthorisedAction: AuthorisedAction =
    new AuthorisedAction()(mockAuthConnector, defaultActionBuilder, stubControllerComponents, mockedAppConfig)
  lazy val auth: AuthorisedAction = mockAuthorisedAction

  lazy val block: User[AnyContent] => Future[Result] =
    user => Future.successful(Ok(s"mtditid: ${user.mtditid}${user.arn.fold("")(arn => " arn: " + arn)}"))

  ".enrolmentGetIdentifierValue" should {
    "return the value for a given identifier" in {
      val returnValue      = "anIdentifierValue"
      val returnValueAgent = "anAgentIdentifierValue"

      val enrolments = Enrolments(
        Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, returnValue)), "Activated"),
          Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, returnValueAgent)), "Activated")
        ))

      auth.enrolmentGetIdentifierValue(EnrolmentKeys.Individual, EnrolmentIdentifiers.individualId, enrolments) mustBe Some(returnValue)
      auth.enrolmentGetIdentifierValue(EnrolmentKeys.Agent, EnrolmentIdentifiers.agentReference, enrolments) mustBe Some(returnValueAgent)
    }
    "return a None" when {
      val key           = "someKey"
      val identifierKey = "anIdentifier"
      val returnValue   = "anIdentifierValue"

      val enrolments = Enrolments(Set(Enrolment(key, Seq(EnrolmentIdentifier(identifierKey, returnValue)), "someState")))

      "the given identifier cannot be found" in {
        auth.enrolmentGetIdentifierValue(key, "someOtherIdentifier", enrolments) mustBe None
      }

      "the given key cannot be found" in {
        auth.enrolmentGetIdentifierValue("someOtherKey", identifierKey, enrolments) mustBe None
      }

    }
  }

  ".individualAuthentication" should {
    "perform the block action" when {
      "the correct enrolment exist and nino exist" which {
        val enrolments = Enrolments(
          Set(
            Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, testMtditid)), "Activated"),
            Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, testNino)), "Activated")
          )
        )

        lazy val result: Future[Result] = {
          (mockAuthConnector
            .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L250))
          auth.individualAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)
        }

        "returns an OK status" in {
          status(result) mustBe OK
        }

        "returns a body of the mtditid" in {
          bodyOf(result) mustBe s"mtditid: $testMtditid"
        }
      }

      "the correct enrolment and nino exist but the request is for a different id" which {

        val enrolments = Enrolments(
          Set(
            Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "123456")), "Activated"),
            Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, testNino)), "Activated")
          ))

        lazy val result: Future[Result] = {
          (mockAuthConnector
            .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L250))
          auth.individualAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)
        }

        "returns an UNAUTHORIZED status" in {
          status(result) mustBe UNAUTHORIZED
        }
      }
      "the correct enrolment and nino exist but low CL" which {

        val enrolments = Enrolments(
          Set(
            Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, testMtditid)), "Activated"),
            Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, testNino)), "Activated")
          ))

        lazy val result: Future[Result] = {
          (mockAuthConnector
            .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L50))
          auth.individualAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)
        }

        "returns an UNAUTHORIZED status" in {
          status(result) mustBe UNAUTHORIZED
        }
      }

      "the correct enrolment exist but no nino" which {

        val enrolments =
          Enrolments(Set(Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, testMtditid)), "Activated")))

        lazy val result: Future[Result] = {
          (mockAuthConnector
            .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L250))
          auth.individualAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)
        }

        "returns an 401 status" in {
          status(result) mustBe UNAUTHORIZED
        }
      }
      "the correct nino exist but no enrolment" which {

        val enrolments = Enrolments(Set(Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, testNino)), "Activated")))

        lazy val result: Future[Result] = {
          (mockAuthConnector
            .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L250))
          auth.individualAuthentication(block, testNino)(fakeRequest, emptyHeaderCarrier)
        }

        "returns an 401 status" in {
          status(result) mustBe UNAUTHORIZED
        }
      }

    }
    "return a UNAUTHORIZED" when {

      "the correct enrolment is missing" which {

        val enrolments =
          Enrolments(Set(Enrolment("notAnIndividualOops", Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, testMtditid)), "Activated")))

        lazy val result: Future[Result] = {
          (mockAuthConnector
            .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L250))
          auth.individualAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)
        }

        "returns a forbidden" in {
          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "the correct enrolment and nino exist but the request is for a different id" which {

      val enrolments = Enrolments(
        Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "123456")), "Activated"),
          Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, testNino)), "Activated")
        ))

      lazy val result: Future[Result] = {
        (mockAuthConnector
          .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
          .returning(Future.successful(enrolments and ConfidenceLevel.L250))
        auth.individualAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)
      }

      "returns an UNAUTHORIZED status" in {
        status(result) mustBe UNAUTHORIZED
      }
    }
    "the correct enrolment and nino exist but low CL" which {

      val enrolments = Enrolments(
        Set(
          Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, testMtditid)), "Activated"),
          Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, testNino)), "Activated")
        ))

      lazy val result: Future[Result] = {
        (mockAuthConnector
          .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
          .returning(Future.successful(enrolments and ConfidenceLevel.L50))
        auth.individualAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)
      }

      "returns an UNAUTHORIZED status" in {
        status(result) mustBe UNAUTHORIZED
      }
    }

    "the correct enrolment exist but no nino" which {

      val enrolments =
        Enrolments(Set(Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, testMtditid)), "Activated")))

      lazy val result: Future[Result] = {
        (mockAuthConnector
          .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
          .returning(Future.successful(enrolments and ConfidenceLevel.L250))
        auth.individualAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)
      }

      "returns an 401 status" in {
        status(result) mustBe UNAUTHORIZED
      }
    }
    "the correct nino exist but no enrolment" which {

      val enrolments = Enrolments(Set(Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, testNino)), "Activated")))

      lazy val result: Future[Result] = {
        (mockAuthConnector
          .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
          .returning(Future.successful(enrolments and ConfidenceLevel.L250))
        auth.individualAuthentication(block, testNino)(fakeRequest, emptyHeaderCarrier)
      }

      "returns an 401 status" in {
        status(result) mustBe UNAUTHORIZED
      }
    }
  }

  ".agentAuthenticated" should {
    "perform the block action" when {
      "the agent is authorised for the given user (Primary Agent)" which {
        val enrolments = Enrolments(
          Set(
            Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, testMtditid)), "Activated"),
            Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, testArn)), "Activated")
          ))

        lazy val result = {
          (mockAuthConnector
            .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, Retrievals.allEnrolments, *, *)
            .returning(Future.successful(enrolments))

          auth.agentAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)
        }

        "has a status of OK" in {
          status(result) mustBe OK
        }

        "has the correct body" in {
          bodyOf(result) mustBe s"mtditid: $testMtditid arn: $testArn"
        }
      }

      "the agent is authorised for the given user (Secondary Agent - EMA enabled)" which {
        val enrolments = Enrolments(
          Set(
            Enrolment(EnrolmentKeys.SupportingAgent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, testMtditid)), "Activated"),
            Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, testArn)), "Activated")
          ))

        lazy val result = {

          // Enabled EMA Supporting/Secondary Agent feature
          mockEmaSupportingAgentEnabled(true)

          // Simulate first call failing for Primary Agent check
          mockAuthReturnException(InsufficientEnrolments())

          // Simulate second call for Secondary Agent check being successful
          (mockAuthConnector
            .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, Retrievals.allEnrolments, *, *)
            .returning(Future.successful(enrolments))
            .once()

          auth.agentAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)
        }

        "has a status of OK" in {
          status(result) mustBe OK
        }

        "has the correct body" in {
          bodyOf(result) mustBe s"mtditid: $testMtditid arn: $testArn"
        }
      }
    }

    "return an Unauthorised" when {
      "the authorisation service returns an AuthorisationException exception (EMA Secondary Agent Disabled)" in {
        lazy val result = {
          // Disable EMA Supporting/Secondary Agent feature
          mockEmaSupportingAgentEnabled(false)
          mockAuthReturnException(InsufficientEnrolments())

          auth.agentAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)
        }
        status(result) mustBe UNAUTHORIZED
      }

      "the authorisation service returns an AuthorisationException exception on the second call (EMA Secondary Enabled)" in {
        lazy val result = {
          // Enabled EMA Supporting/Secondary Agent feature
          mockEmaSupportingAgentEnabled(true)

          // Simulate first & second call failing for Primary Agent check
          mockAuthReturnException(InsufficientEnrolments()).twice()

          auth.agentAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)
        }
        status(result) mustBe UNAUTHORIZED
      }

      "the authorisation service returns a NoActiveSession exception" in {
        object NoActiveSession extends NoActiveSession("Some reason")

        lazy val result = {
          mockAuthReturnException(NoActiveSession)
          auth.agentAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)
        }

        status(result) mustBe UNAUTHORIZED
      }

      "the user does not have an enrolment for the agent" in {
        val enrolments = Enrolments(
          Set(
            Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, testMtditid)), "Activated")
          ))

        lazy val result = {
          (mockAuthConnector
            .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, Retrievals.allEnrolments, *, *)
            .returning(Future.successful(enrolments))
          auth.agentAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)
        }
        status(result) mustBe UNAUTHORIZED
      }
    }

    "return an InternalServerError" when {
      "an unexpected error occurs during primary agent auth call" in {
        object RandomError extends IndexOutOfBoundsException("Some reason")

        mockAuthReturnException(RandomError)

        lazy val result = auth.agentAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "[EMA enabled] an unexpected error occurs during secondary agent auth call" in {
        object RandomError extends IndexOutOfBoundsException("Some reason")
        object AuthError extends AuthorisationException("Some reason")

        mockEmaSupportingAgentEnabled(true)
        mockAuthReturnException(AuthError)
        mockAuthReturnException(RandomError)

        lazy val result = auth.agentAuthentication(block, testMtditid)(fakeRequest, emptyHeaderCarrier)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  ".async" should {
    "perform the block action" when {
      "the user is successfully verified as an agent" which {
        lazy val result: Future[Result] = {
          mockAuthAsAgent()
          auth.async(block)(fakeRequest)
        }

        "should return an OK(200) status" in {
          status(result) mustBe OK
          bodyOf(result) mustBe s"mtditid: $testMtditid arn: $testArn"
        }
      }

      "the user is successfully verified as an individual" in {
        lazy val result = {
          mockAuth()
          auth.async(block)(fakeRequest)
        }

        status(result) mustBe OK
        bodyOf(result) mustBe s"mtditid: $testMtditid"
      }
    }

    "return an Unauthorised" when {
      "the authorisation service returns an AuthorisationException exception" in {
        object AuthException extends AuthorisationException("Some reason")

        lazy val result = {
          mockAuthReturnException(AuthException)
          auth.async(block)
        }

        status(result(fakeRequest)) mustBe UNAUTHORIZED
      }

    }

    "return an Unauthorised" when {
      "the authorisation service returns a NoActiveSession exception" in {
        object NoActiveSession extends NoActiveSession("Some reason")

        lazy val result = {
          mockAuthReturnException(NoActiveSession)
          auth.async(block)
        }

        status(result(fakeRequest)) mustBe UNAUTHORIZED
      }
      "the request does not contain mtditid header" in {
        lazy val result =
          auth.async(block)

        status(result(FakeRequest())) mustBe UNAUTHORIZED
      }
    }

    "return an InternalServerError" when {
      "the authorisation service returns a unexpected exception" in {
        object RandomException extends IndexOutOfBoundsException("Some error")

        mockAuthReturnException(RandomException)

        lazy val result = auth.async(block)(fakeRequest)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
