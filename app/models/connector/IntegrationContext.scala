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
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.http.HeaderCarrier.Config

import scala.concurrent.ExecutionContext

sealed trait IntegrationContext {
  val url: String
  val hc: HeaderCarrier
  val ec: ExecutionContext
}

object IntegrationContext {
  final case class IFSHeaderCarrier(headerCarrierConfig: Config, appConfig: AppConfig, api: IFSApiName, url: String)(implicit
      headerCarrier: HeaderCarrier,
      executionContext: ExecutionContext)
      extends IntegrationContext {
    val hc: HeaderCarrier = {
      val hcWithAuth = headerCarrier.copy(authorization = Some(Authorization(s"Bearer ${appConfig.ifsAuthorisationToken(api.entryName)}")))
      ApiConnector.apiHeaderCarrier(headerCarrierConfig, url, hcWithAuth, "Environment" -> appConfig.ifsEnvironment)
    }

    val ec: ExecutionContext = executionContext
  }
}
