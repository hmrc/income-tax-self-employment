/*
 * Copyright 2025 HM Revenue & Customs
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

package mocks.connectors

import cats.data.EitherT
import cats.implicits.catsStdInstancesForFuture
import connectors.ReliefClaimsConnector
import models.common.JourneyContextWithNino
import models.connector.ReliefClaimType
import models.connector.api_1505.ClaimId
import models.connector.common.ReliefClaim
import models.domain.ApiResultT
import models.error.ServiceError
import org.scalamock.handlers.{CallHandler2, CallHandler3}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, TestSuite}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

trait MockReliefClaimsConnector extends TestSuite with MockFactory with OneInstancePerTest {

  val mockReliefClaimsConnector: ReliefClaimsConnector = mock[ReliefClaimsConnector]

  object ReliefClaimsConnectorMock {

    def createReliefClaim(ctx: JourneyContextWithNino,
                          answer: ReliefClaimType)
                         (returnValue: ClaimId): CallHandler3[JourneyContextWithNino, ReliefClaimType, HeaderCarrier, ApiResultT[ClaimId]] =
      (mockReliefClaimsConnector.createReliefClaim(_: JourneyContextWithNino, _: ReliefClaimType)(_: HeaderCarrier))
        .expects(ctx, answer, *)
        .returning(EitherT.pure(returnValue))

    def createReliefClaimError(ctx: JourneyContextWithNino,
                               answer: ReliefClaimType)
                              (returnValue: ServiceError): CallHandler3[JourneyContextWithNino, ReliefClaimType, HeaderCarrier, ApiResultT[ClaimId]] =
      (mockReliefClaimsConnector.createReliefClaim(_: JourneyContextWithNino, _: ReliefClaimType)(_: HeaderCarrier))
        .expects(ctx, answer, *)
        .returning(EitherT.leftT(returnValue))

    def deleteReliefClaim(ctx: JourneyContextWithNino,
                          claimId: String): CallHandler3[JourneyContextWithNino, String, HeaderCarrier, ApiResultT[Unit]] =
      (mockReliefClaimsConnector.deleteReliefClaim(_: JourneyContextWithNino, _: String)(_: HeaderCarrier))
        .expects(ctx, claimId, *)
        .returning(EitherT.pure(()))

    def getAllReliefClaims(ctx: JourneyContextWithNino)
                          (returnValue: List[ReliefClaim]): CallHandler2[JourneyContextWithNino, HeaderCarrier, ApiResultT[List[ReliefClaim]]] =
      (mockReliefClaimsConnector.getAllReliefClaims(_: JourneyContextWithNino)(_: HeaderCarrier))
        .expects(ctx, *)
        .returning(EitherT.pure(returnValue))

    def getAllReliefClaimsError(ctx: JourneyContextWithNino)
                               (returnValue: ServiceError): CallHandler2[JourneyContextWithNino, HeaderCarrier, ApiResultT[List[ReliefClaim]]] =
      (mockReliefClaimsConnector.getAllReliefClaims(_: JourneyContextWithNino)(_: HeaderCarrier))
        .expects(ctx, *)
        .returning(EitherT.leftT(returnValue))

  }

}
