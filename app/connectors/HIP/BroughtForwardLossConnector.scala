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

package connectors.HIP

import cats.data.EitherT
import config.AppConfig
import connectors.delete
import models.common._
import models.connector._
import models.domain.ApiResultT
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext


@Singleton
class BroughtForwardLossConnector @Inject() (http: HttpClient, appConfig: AppConfig) extends Logging {

  private def deleteBroughtForwardLossUrl(nino: Nino, taxYear: TaxYear, lossId: String) =
    s"${appConfig.hipBaseUrl}/income-tax/v1/brought-forward-losses/$nino/${TaxYear.asTys(taxYear)}/$lossId"

  def deleteBroughtForwardLoss(nino: Nino, taxYear: TaxYear, lossId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] = {

    val url                                          = deleteBroughtForwardLossUrl(nino, taxYear, lossId)
    val context                                      = appConfig.mkMetadata(HipApiName.Api1504, url)
    implicit val reads: HttpReads[ApiResponse[Unit]] = commonNoBodyResponse

    EitherT(delete(http, context))
  }

}
