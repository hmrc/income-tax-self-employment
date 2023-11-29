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

package services

import cats.data.EitherT
import cats.implicits.{catsSyntaxApplicativeId, toTraverseOps}
import connectors.BusinessDetailsConnector
import connectors.BusinessDetailsConnector.IdType.Nino
import models.database.JourneyState
import models.domain.TradesJourneyStatuses.JourneyStatus
import models.domain.{Business, TradesJourneyStatuses}
import models.error.{DownstreamError, ServiceError}
import repositories.JourneyStateRepository
import services.BusinessService.{GetBusinessJourneyStatesResponse, GetBusinessResponse}
import uk.gov.hmrc.http.HeaderCarrier
import utils.PagerDutyHelper.PagerDutyKeys.FAILED_TO_GET_JOURNEY_STATE_DATA
import utils.PagerDutyHelper.WithRecoveryEither

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessService @Inject() (connector: BusinessDetailsConnector, journeyStateRepository: JourneyStateRepository)(implicit ec: ExecutionContext) {

  def getBusinesses(nino: String)(implicit hc: HeaderCarrier): Future[GetBusinessResponse] =
    (for {
      businessData <- EitherT(connector.getBusinessDetails(Nino, nino))
      tpdr       = businessData.taxPayerDisplayResponse
      businesses = tpdr.businessData.map(_.toBusiness(tpdr))
    } yield businesses).value

  def getBusiness(nino: String, businessId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetBusinessResponse] =
    EitherT(getBusinesses(nino))
      .map(business => business.filter(_.businessId == businessId))
      .value

  def getBusinessJourneyStates(nino: String, taxYear: Int)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[GetBusinessJourneyStatesResponse] = {
    lazy val pagerMsg = "[Self-Employment BE SessionRepository][get] Failed to find journey state data."

    val resultT = for {
      allBusinesses    <- EitherT(getBusinesses(nino))
      allJourneyStates <- EitherT.right[DownstreamError](journeyStates(allBusinesses, taxYear))
      allJourneyStatus <- EitherT.right[DownstreamError](journeyStatus(allJourneyStates))
      result           <- EitherT.right[DownstreamError](tradesJourneyStatuses(allBusinesses, allJourneyStatus))
    } yield result

    resultT.value
      .recoverEitherWithPagerDutyLog(FAILED_TO_GET_JOURNEY_STATE_DATA, pagerMsg)
  }

  private def journeyStates(allBusinesses: Seq[Business], taxYear: Int): Future[Seq[JourneyState]] =
    allBusinesses
      .map(business => journeyStateRepository.get(business.businessId, taxYear))
      .sequence
      .map(_.flatten)

  private def journeyStatus(allJourneyStates: Seq[JourneyState]): Future[Seq[JourneyStatus]] =
    allJourneyStates
      .map(js => JourneyStatus(js.journeyStateData.journey, js.journeyStateData.completedState))
      .pure[Future]

  private def tradesJourneyStatuses(allBusinesses: Seq[Business], allJourneyStatus: Seq[JourneyStatus]): Future[Seq[TradesJourneyStatuses]] =
    allBusinesses
      .map(business => TradesJourneyStatuses(business.businessId, business.tradingName, allJourneyStatus))
      .pure[Future]

}

object BusinessService {
  type GetBusinessResponse              = Either[DownstreamError, Seq[Business]]
  type GetBusinessJourneyStatesResponse = Either[ServiceError, Seq[TradesJourneyStatuses]]
}
