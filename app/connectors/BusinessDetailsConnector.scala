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

package connectors

import config.AppConfig
import connectors.BusinessDetailsConnector.{BusinessesBaseApi, IdType, businessUriPath}
import connectors.httpParsers.GetBusinessesHttpParser._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessDetailsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit ec: ExecutionContext) extends ApiConnector {

  private def businessIncomeSourceUri(idType: IdType, idNumber: String): String =
    appConfig.ifsBaseUrl + businessUriPath(idType, idNumber)

  def getBusinessDetails(idType: IdType, idNumber: String)(implicit hc: HeaderCarrier): Future[GetBusinessesRequestResponse] = {
    val incomeSourceUri: String = businessIncomeSourceUri(idType, idNumber)

    def apiCall(implicit hc: HeaderCarrier): Future[GetBusinessesRequestResponse] =
      http.GET[GetBusinessesRequestResponse](incomeSourceUri)
    apiCall(ifsHeaderCarrier(BusinessesBaseApi.Get)(incomeSourceUri)(hc))
  }
}

object BusinessDetailsConnector {
  sealed trait IdType
  object IdType {
    case object Nino extends IdType {
      override def toString: String = "nino"
    }
    case object MtdId extends IdType {
      override def toString: String = "mtdId"
    }
  }

  def businessUriPath(idType: IdType, idNumber: String): String =
    s"/registration/business-details/$idType/$idNumber"

  object BusinessesBaseApi {
    val Get = "1171"
  }
}
