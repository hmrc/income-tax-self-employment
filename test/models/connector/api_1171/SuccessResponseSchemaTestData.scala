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

package models.connector.api_1171

import models.common.{Mtditid, Nino}
import models.connector.businessDetailsConnector.{BusinessDataDetails, ResponseType, BusinessDetailsSuccessResponseSchema}

import java.time.OffsetDateTime

object SuccessResponseSchemaTestData {
  def mkExample(nino: Nino,
                mtditid: Mtditid,
                businesses: List[BusinessDataDetails]
               ): BusinessDetailsSuccessResponseSchema = BusinessDetailsSuccessResponseSchema(
    OffsetDateTime.now().toString,
    ResponseType(
      "safeId",
      nino.value,
      mtditid.value,
      None,
      propertyIncome = false,
      Some(businesses)
    )
  )
}
