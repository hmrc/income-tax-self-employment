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

package models.connector.api_1502

import models.connector.api_1500.LossType
import play.api.libs.json.{Format, Json}

import java.time.LocalDateTime

case class SuccessResponseSchema(businessId: String,
                                 typeOfLoss: LossType,
                                 lossAmount: BigDecimal,
                                 taxYearBroughtForwardFrom: String,
                                 lastModified: LocalDateTime,
                                 links: Option[List[SuccessResponseSchemaLinks]])

object SuccessResponseSchema {
  implicit val formats: Format[SuccessResponseSchema] =
    Json.format[SuccessResponseSchema]
}
