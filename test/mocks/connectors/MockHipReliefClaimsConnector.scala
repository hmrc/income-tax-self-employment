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
import connectors.HIP.HipReliefClaimsConnector
import models.common.JourneyContextWithNino
import models.connector.ReliefClaimType
import models.connector.api_1505.ClaimId
import models.domain.ApiResultT
import org.scalamock.handlers.{CallHandler3, CallHandler4}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, TestSuite}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

trait MockHipReliefClaimsConnector extends TestSuite with MockFactory with OneInstancePerTest {

  val mockHipReliefClaimsConnector: HipReliefClaimsConnector = mock[HipReliefClaimsConnector]

  object HipReliefClaimsConnectorMock {

    def deleteReliefClaim(ctx: JourneyContextWithNino,
                          claimId: String): CallHandler4[JourneyContextWithNino, String, HeaderCarrier, ExecutionContext, ApiResultT[Unit]] =
      (mockHipReliefClaimsConnector.deleteReliefClaim(_: JourneyContextWithNino, _: String)(_: HeaderCarrier, _: ExecutionContext))
        .expects(ctx, claimId, *, *)
        .returning(EitherT.pure(()))

    def createReliefClaim(ctx: JourneyContextWithNino,
                          answer: ReliefClaimType)
                         (returnValue: ClaimId): CallHandler3[JourneyContextWithNino, ReliefClaimType, HeaderCarrier, ApiResultT[ClaimId]] =
      (mockHipReliefClaimsConnector.createReliefClaim(_: JourneyContextWithNino, _: ReliefClaimType)(_: HeaderCarrier))
        .expects(ctx, answer, *)
        .returning(EitherT.pure(returnValue))

  }

}
