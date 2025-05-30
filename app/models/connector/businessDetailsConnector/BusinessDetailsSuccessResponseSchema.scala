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

package models.connector.businessDetailsConnector

import models.domain.Business
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

case class BusinessDetailsSuccessResponseSchema(processingDate: String,
                                                taxPayerDisplayResponse: ResponseType) {

  def toBusinesses: List[Business] =
    for {
      business <- taxPayerDisplayResponse.businessData.getOrElse(Nil)
      maybeYearOfMigration = taxPayerDisplayResponse.yearOfMigration
    } yield Business.mkBusiness(business, maybeYearOfMigration)

}

object BusinessDetailsSuccessResponseSchema {
  implicit lazy val successResponseSchemaJsonFormat: Format[BusinessDetailsSuccessResponseSchema] = Json.format[BusinessDetailsSuccessResponseSchema]

  val hipReads: Reads[BusinessDetailsSuccessResponseSchema] = (
    (JsPath \ "processingDate").read[String] and
    (JsPath \ "taxPayerDisplayResponse").read[ResponseType](ResponseType.hipFormat)
  )(BusinessDetailsSuccessResponseSchema.apply _)

  val hipFormat: Format[BusinessDetailsSuccessResponseSchema] = Format(hipReads, successResponseSchemaJsonFormat.writes)

}

case class BusinessDetailsHipSuccessWrapper(success: BusinessDetailsSuccessResponseSchema)

object BusinessDetailsHipSuccessWrapper {

  val hipReads: Reads[BusinessDetailsHipSuccessWrapper] = Reads[BusinessDetailsHipSuccessWrapper] { json =>
    (json \ "success")
      .validate[BusinessDetailsSuccessResponseSchema](BusinessDetailsSuccessResponseSchema.hipFormat)
      .map(BusinessDetailsHipSuccessWrapper(_))
  }

  implicit val format: Format[BusinessDetailsHipSuccessWrapper] = Format(hipReads, Json.writes[BusinessDetailsHipSuccessWrapper])

}
