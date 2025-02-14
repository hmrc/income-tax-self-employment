
package mocks.connectors

import cats.data.EitherT
import cats.implicits.catsStdInstancesForFuture
import connectors.ReliefClaimsConnector
import models.common.{BusinessId, JourneyContextWithNino, TaxYear}
import models.connector.ReliefClaimType
import models.connector.api_1505.CreateLossClaimSuccessResponse
import models.connector.common.ReliefClaim
import models.domain.ApiResultT
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.mockito.stubbing.ScalaOngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock

import scala.concurrent.ExecutionContext.Implicits.global


class MockReliefClaimsConnector {

  val mockInstance: ReliefClaimsConnector = mock[ReliefClaimsConnector]

  def createReliefClaim(ctx: JourneyContextWithNino, answer: ReliefClaimType)
                       (returnValue: CreateLossClaimSuccessResponse): ScalaOngoingStubbing[ApiResultT[CreateLossClaimSuccessResponse]] =
    when(mockInstance.createReliefClaim(
      ArgumentMatchers.eq(ctx),
      ArgumentMatchers.eq(answer))(any())
    ).thenReturn(EitherT.pure(returnValue))


  def deleteReliefClaim(ctx: JourneyContextWithNino, claimId: String): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.deleteReliefClaim(
      ArgumentMatchers.eq(ctx),
      ArgumentMatchers.eq(claimId))(any())
    ).thenReturn(EitherT.pure(()))
  }

  def getAllReliefClaims(taxYear: TaxYear, businessId: BusinessId)
                        (returnValue: List[ReliefClaim]): ScalaOngoingStubbing[ApiResultT[List[ReliefClaim]]] = {
    when(mockInstance.getAllReliefClaims(
      ArgumentMatchers.eq(taxYear),
      ArgumentMatchers.eq(businessId))(any())
    ).thenReturn(EitherT.pure(returnValue))
  }

}
