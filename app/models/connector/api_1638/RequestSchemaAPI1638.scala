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

package models.connector.api_1638

import models.frontend.nics.NICsAnswers
import play.api.libs.json._

/** Represents the Swagger definition for requestSchemaAPI1638.
  */
case class RequestSchemaAPI1638(
    taxAvoidance: Option[List[RequestSchemaAPI1638TaxAvoidanceInner]],
    class2Nics: Option[RequestSchemaAPI1638Class2Nics]
)

object RequestSchemaAPI1638 {
  implicit lazy val requestSchemaAPI1638JsonFormat: Format[RequestSchemaAPI1638] = Json.format[RequestSchemaAPI1638]

  /** Some(false) is not possible to set on class2VoluntaryContributions. We cannot send an empty object therefore if there are no other fields we
    * have to call DELETE.
    *
    * @return
    *   None if the object needs to be DELETED or Some() if it needs to be updated via PUT
    */
  def mkRequestBody(answers: NICsAnswers, existingData: RequestSchemaAPI1638): Option[RequestSchemaAPI1638] =
    if (answers.class2NICs) {
      Some(existingData.copy(class2Nics = Some(RequestSchemaAPI1638Class2Nics(Some(true)))))
    } else if (existingData.taxAvoidance.isDefined) {
      Some(existingData.copy(class2Nics = Some(RequestSchemaAPI1638Class2Nics(None))))
    } else {
      None
    }
}
