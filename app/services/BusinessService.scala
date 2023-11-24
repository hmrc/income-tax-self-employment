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

import connectors.BusinessConnector
import connectors.BusinessConnector.IdType.Nino
import models.error.{ServiceError, DownstreamError}
import models.domain.{Business, TradesJourneyStatuses}
import repositories.JourneyStateRepository
import services.BusinessService.{GetBusinessJourneyStatesResponse, GetBusinessResponse}
import uk.gov.hmrc.http.HeaderCarrier
import utils.PagerDutyHelper.PagerDutyKeys.FAILED_TO_GET_JOURNEY_STATE_DATA
import utils.PagerDutyHelper.WithRecoveryEither
import utils.ScalaHelper.FutureEither

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessService @Inject() (businessConnector: BusinessConnector, journeyStateRepository: JourneyStateRepository)(implicit
    ec: ExecutionContext) {

  def getBusinesses(nino: String)(implicit hc: HeaderCarrier): Future[GetBusinessResponse] =
    businessConnector
      .getBusinesses(Nino, nino)
      .map(
        _.map(_.taxPayerDisplayResponse)
          .map(taxPayerDisplayResponse => taxPayerDisplayResponse.businessData.map(_.toBusiness(taxPayerDisplayResponse))))

  def getBusiness(nino: String, businessId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetBusinessResponse] =
    getBusinesses(nino).map(_.map(_.filter(_.businessId == businessId)))

  def getBusinessJourneyStates(nino: String, taxYear: Int)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[GetBusinessJourneyStatesResponse] = {
    lazy val pagerMsg = "[Self-Employment BE SessionRepository][get] Failed to find journey state data."

    getBusinesses(nino)
      .map(_.map(_.map(bus =>
        journeyStateRepository
          .get(bus.businessId, taxYear)
          .map(seqJs =>
            (bus.businessId, Some(bus.tradingName.getOrElse("")), seqJs.map(j => (j.journeyStateData.journey, j.journeyStateData.completedState)))))))
      .map(_.map(seq => Future.sequence(seq)))
      .map(_.toFuture())
      .flatten
      .map(_.map(_.map(TradesJourneyStatuses(_))))
      .recoverEitherWithPagerDutyLog(FAILED_TO_GET_JOURNEY_STATE_DATA, pagerMsg)
  }

}

object BusinessService {
  type GetBusinessResponse              = Either[DownstreamError, Seq[Business]]
  type GetBusinessJourneyStatesResponse = Either[ServiceError, Seq[TradesJourneyStatuses]]
}
