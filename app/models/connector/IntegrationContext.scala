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

package models.connector

import config.AppConfig
import connectors.ApiConnector
import uk.gov.hmrc.http.HeaderCarrier.Config
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}

sealed trait IntegrationContext {
  val url: String
  val api: ApiName

  def enrichedHeaderCarrier(implicit headerCarrier: HeaderCarrier): HeaderCarrier
}

object IntegrationContext {

  final case class IntegrationHeaderCarrier(headerCarrierConfig: Config, appConfig: AppConfig, api: ApiName, url: String) extends IntegrationContext {
    def enrichedHeaderCarrier(implicit headerCarrier: HeaderCarrier): HeaderCarrier = {
      val (hcWithAuth, headers) = api match {
        case _: IFSApiName =>
          val updatedHeader = headerCarrier.copy(authorization = Option(Authorization(s"Bearer ${appConfig.ifsAuthorisationToken(api.entryName)}")))
          (updatedHeader, List("Environment" -> appConfig.ifsEnvironment))
        case _: HipApiName =>
          val updatedHeader = headerCarrier.copy(authorization = Option(Authorization(s"Bearer ${appConfig.hipAuthorisationToken(api.entryName)}")))
          (updatedHeader, List("Environment" -> appConfig.hipEnvironment))
        case _: MDTPApiName => // We don't need bearer for MDTP services
          (headerCarrier, Nil)
      }

      ApiConnector.apiHeaderCarrier(headerCarrierConfig, url, hcWithAuth, appConfig.testMode, headers: _*)
    }
  }
}
