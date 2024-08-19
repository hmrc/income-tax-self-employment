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

package models.frontend.nics

import models.common.BusinessId
import models.connector.api_1639.SuccessResponseAPI1639
import models.database.nics.NICsStorageAnswers
import play.api.libs.json.{Format, Json}

case class NICsAnswers(class2NICs: Option[Boolean],
                       class4NICs: Option[Boolean],
                       class4ExemptionReason: Option[ExemptionReason],
                       class4DivingExempt: Option[List[BusinessId]],
                       class4NonDivingExempt: Option[List[BusinessId]]) {
  def isClass2: Boolean = class2NICs.isDefined
  def isClass4SingleBusiness(businessId: BusinessId): Boolean =
    class4ExemptionReason.isDefined && businessId != BusinessId.nationalInsuranceContributions
}

object NICsAnswers {
  implicit val formats: Format[NICsAnswers] = Json.format[NICsAnswers]

  val empty: NICsAnswers = NICsAnswers(None, None, None, None, None)

  def mkPriorData(maybeApiAnswers: Option[SuccessResponseAPI1639], maybeDbAnswers: Option[NICsStorageAnswers]): Option[NICsAnswers] = {
    val existingClass2Nics = for {
      answers     <- maybeApiAnswers
      nicsAnswers <- answers.class2Nics
      class2Nics  <- nicsAnswers.class2VoluntaryContributions
    } yield class2Nics

    val maybeNics = existingClass2Nics.map(NICsAnswers(_))
    maybeNics.orElse(maybeDbAnswers.flatMap(_.class2NICs.map(NICsAnswers(_))))
  }

}
