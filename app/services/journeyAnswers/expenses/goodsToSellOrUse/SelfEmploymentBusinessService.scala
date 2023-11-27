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

package services.journeyAnswers.expenses.goodsToSellOrUse

import cats.implicits.catsSyntaxEitherId
import connectors.SelfEmploymentBusinessConnector
import models.common.RequestData
import models.error.DownstreamError
import play.api.libs.json.Writes
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentBusinessService @Inject() (connector: SelfEmploymentBusinessConnector)(implicit ec: ExecutionContext) {

  def createSEPeriodSummary[T](data: RequestData, answers: T)(implicit writes: Writes[T], hc: HeaderCarrier): Future[Either[DownstreamError, Unit]] =
    connector.createSEPeriodSummary(data, answers).map {
      case Right(_)              => ().asRight
      case Left(downstreamError) => downstreamError.asLeft
    }
}
