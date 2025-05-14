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

package mocks.services

import cats.data.EitherT
import models.common.{BusinessId, JourneyContextWithNino, Mtditid, Nino, TaxYear}
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.connector.businessDetailsConnector.BusinessDetailsSuccessResponseSchema
import models.domain.{ApiResultT, Business}
import models.frontend.adjustments.NetBusinessProfitOrLossValues
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar.when
import org.mockito.stubbing.ScalaOngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import services.BusinessService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

object MockBusinessService {

  val mockInstance: BusinessService = mock[BusinessService]

  def getBusinessDetails(businessId: Option[BusinessId], mtditid: Mtditid, nino: Nino)
                        (returnValue: BusinessDetailsSuccessResponseSchema):
  ScalaOngoingStubbing[ApiResultT[Option[BusinessDetailsSuccessResponseSchema]]] = {
    when(mockInstance.getBusinessDetails(eqTo(businessId), eqTo(mtditid), eqTo(nino))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(EitherT.rightT(Some(returnValue)))
  }

  def getBusiness(businessId: BusinessId, mtditid: Mtditid, nino: Nino)
                 (returnValue: Business):
  ScalaOngoingStubbing[ApiResultT[Business]] = {
    when(mockInstance.getBusiness(eqTo(businessId), eqTo(mtditid), eqTo(nino))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def getBusinesses(mtditid: Mtditid, nino: Nino)
                   (returnValue: List[Business]):
  ScalaOngoingStubbing[ApiResultT[List[Business]]] = {
    when(mockInstance.getBusinesses(eqTo(mtditid), eqTo(nino))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def getUserBusinessIds(mtditid: Mtditid, nino: Nino)
                        (returnValue: List[BusinessId]):
  ScalaOngoingStubbing[ApiResultT[List[BusinessId]]] = {
    when(mockInstance.getUserBusinessIds(eqTo(mtditid), eqTo(nino))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def getUserDateOfBirth(nino: Nino)
                        (returnValue: LocalDate): ScalaOngoingStubbing[ApiResultT[LocalDate]] =
    when(mockInstance.getUserDateOfBirth(eqTo(nino))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))

  def getAllBusinessIncomeSourcesSummaries(taxYear: TaxYear, mtditid: Mtditid, nino: Nino)
                                          (returnValue: List[BusinessIncomeSourcesSummaryResponse]): ScalaOngoingStubbing[ApiResultT[List[BusinessIncomeSourcesSummaryResponse]]] =
    when(mockInstance.getAllBusinessIncomeSourcesSummaries(eqTo(taxYear), eqTo(mtditid), eqTo(nino))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)
                                     (returnValue: BusinessIncomeSourcesSummaryResponse): ScalaOngoingStubbing[ApiResultT[BusinessIncomeSourcesSummaryResponse]] =
    when(mockInstance.getBusinessIncomeSourcesSummary(eqTo(taxYear), eqTo(nino), eqTo(businessId))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))

  def getNetBusinessProfitOrLossValues(ctx: JourneyContextWithNino)
                                      (returnValue: NetBusinessProfitOrLossValues): ScalaOngoingStubbing[ApiResultT[NetBusinessProfitOrLossValues]] =
    when(mockInstance.getNetBusinessProfitOrLossValues(eqTo(ctx))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))

  def hasOtherIncomeSources(taxYear: TaxYear, nino: Nino)
                           (returnValue: Boolean): ScalaOngoingStubbing[ApiResultT[Boolean]] =
    when(mockInstance.hasOtherIncomeSources(eqTo(taxYear), eqTo(nino))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))

}