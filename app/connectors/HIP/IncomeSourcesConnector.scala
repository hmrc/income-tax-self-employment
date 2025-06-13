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
import jakarta.inject.{Inject, Singleton}
import models.common.Nino
import models.connector.api_2085.ListOfIncomeSources
import models.connector.{ApiResponse, HipApiName, createCommonErrorParser}
import models.domain.ApiResultT
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}
import utils.{IdGenerator, TimeMachine}

import java.net.URL
import scala.concurrent.ExecutionContext

@Singleton
class IncomeSourcesConnector @Inject()(httpClientV2: HttpClientV2,
                                       appConfig: AppConfig,
                                       timeMachine: TimeMachine,
                                       idGenerator: IdGenerator)
                                      (implicit ec: ExecutionContext) extends Logging {

  val getUrl: Nino => URL = (nino) => url"${appConfig.hipBaseUrl}/itsd/income-sources/v2/$nino"

  private def additionalHeaders: Seq[(String, String)] = Seq(
    "correlationid" -> idGenerator.generateCorrelationId(),
    "X-Message-Type" -> "TaxpayerDisplay",
    "X-Originating-System" -> "MDTP",
    "X-Receipt-Date" -> timeMachine.now.toString,
    "X-Regime-Type" -> "ITSA",
    "X-Transmitting-System" -> "HIP"
  )

  def getIncomeSources(nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[ListOfIncomeSources] = {
    val enrichedHeaderCarrier: HeaderCarrier = appConfig.mkMetadata(HipApiName.Api5190, getUrl(nino).toString).enrichedHeaderCarrier

    implicit object Reads extends HttpReads[ApiResponse[ListOfIncomeSources]] {
      override def read(method: String, url: String, response: HttpResponse): ApiResponse[ListOfIncomeSources] = {
        response.status match {
          case OK =>
            response.json
              .validate[ListOfIncomeSources]
              .fold[ApiResponse[ListOfIncomeSources]](
                errors => Left(createCommonErrorParser(method, url, response).reportInvalidJsonError(errors.toList)),
                parsedModel => Right(parsedModel)
              )
          case NOT_FOUND =>
            logger.warn(s"")
            Right(ListOfIncomeSources(Nil))
          case _ =>
            logger.error(s"HIP Get Self Employment Income Sources API returned unexpected status '${response.status}'")
            Left(createCommonErrorParser(method, url, response).handleDownstreamError(response))
        }
      }
    }

    EitherT {
      httpClientV2
        .get(getUrl(nino))(enrichedHeaderCarrier)
        .transform(_.addHttpHeaders(additionalHeaders : _*))
        .execute
    }
  }

}
