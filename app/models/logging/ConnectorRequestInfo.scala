/*
 * Copyright 2024 HM Revenue & Customs
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

package models.logging

import models.connector.ApiName
import play.api.Logger
import play.api.libs.json.{Json, Writes}

final case class ConnectorRequestInfo(method: String, url: String, apiId: ApiName) {
  private def apiIdStr = s"API#${apiId.entryName}"

  def logRequestWithBody[A: Writes](logger: Logger, body: A): Unit = {
    val jsonBody = implicitly[Writes[A]].writes(body)
    logger.info(s"Connector: Sending Request $apiIdStr $method $url:\n===\n${Json.prettyPrint(jsonBody)}\n===")
  }

  def logRequest(logger: Logger): Unit =
    logger.info(s"Connector: Sending Request $apiIdStr $method $url")

}
