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

package models.common

import play.api.libs.json.{Format, Json}
import play.api.mvc.PathBindable

final case class BusinessId(value: String) extends AnyVal {
  override def toString: String = value
}

object BusinessId {

  /** Special Business Id for trade details which is not business specific */
  val nationalInsuranceContributions: BusinessId = BusinessId("national-insurance-contributions")

  implicit def pathBindable(implicit strBinder: PathBindable[String]): PathBindable[BusinessId] = new PathBindable[BusinessId] {

    override def bind(key: String, value: String): Either[String, BusinessId] =
      strBinder.bind(key, value).map(BusinessId.apply)

    override def unbind(key: String, businessId: BusinessId): String =
      strBinder.unbind(key, businessId.value)
  }

  implicit val format: Format[BusinessId] = Json.valueFormat[BusinessId]

}
