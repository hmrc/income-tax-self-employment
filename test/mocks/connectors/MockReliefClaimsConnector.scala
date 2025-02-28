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
import models.common.{BusinessId, JourneyContextWithNino, TaxYear}
import models.connector.ReliefClaimType
import models.connector.api_1505.ClaimId
import models.connector.common.ReliefClaim
import models.domain.ApiResultT
import models.error.ServiceError
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.when
import org.mockito.stubbing.ScalaOngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock

import scala.concurrent.ExecutionContext.Implicits.global

object MockReliefClaimsConnector {

  val mockInstance: ReliefClaimsConnector = mock[ReliefClaimsConnector]

  def createReliefClaim(ctx: JourneyContextWithNino, answer: ReliefClaimType)(returnValue: ClaimId): ScalaOngoingStubbing[ApiResultT[ClaimId]] =
    when(mockInstance.createReliefClaim(ArgumentMatchers.eq(ctx), ArgumentMatchers.eq(answer))(any())).thenReturn(EitherT.pure(returnValue))

  def createReliefClaimError(ctx: JourneyContextWithNino, answer: ReliefClaimType)(
      returnValue: ServiceError): ScalaOngoingStubbing[ApiResultT[ClaimId]] =
    when(mockInstance.createReliefClaim(eqTo(ctx), eqTo(answer))(any()))
      .thenReturn(EitherT.leftT(returnValue))

  def deleteReliefClaim(ctx: JourneyContextWithNino, claimId: String): ScalaOngoingStubbing[ApiResultT[Unit]] =
    when(mockInstance.deleteReliefClaim(eqTo(ctx), eqTo(claimId))(any()))
      .thenReturn(EitherT.pure(()))

  def getAllReliefClaims(taxYear: TaxYear, businessId: BusinessId)(
      returnValue: List[ReliefClaim]): ScalaOngoingStubbing[ApiResultT[List[ReliefClaim]]] =
    when(mockInstance.getAllReliefClaims(eqTo(taxYear), eqTo(businessId))(any()))
      .thenReturn(EitherT.pure(returnValue))

  def getAllReliefClaimsError(taxYear: TaxYear, businessId: BusinessId)(
      returnValue: ServiceError): ScalaOngoingStubbing[ApiResultT[List[ReliefClaim]]] =
    when(mockInstance.getAllReliefClaims(eqTo(taxYear), eqTo(businessId))(any()))
      .thenReturn(EitherT.leftT(returnValue))

}
