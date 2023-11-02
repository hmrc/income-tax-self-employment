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

package models.connector.api_1894

import play.api.libs.json._

/**
  * Represents the Swagger definition for postSelfEmploymentPeriod_201_response.
  * @param ibdSubmissionPeriodId ID of the created period submission.
  */
case class PostSelfEmploymentPeriod201Response(
  ibdSubmissionPeriodId: String
)

object PostSelfEmploymentPeriod201Response {
  implicit lazy val postSelfEmploymentPeriod201ResponseJsonFormat: Format[PostSelfEmploymentPeriod201Response] = Json.format[PostSelfEmploymentPeriod201Response]
}

