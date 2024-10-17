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

import cats.implicits.catsSyntaxOptionId
import models.common.BusinessId
import models.connector.api_1802.request.AnnualNonFinancials
import models.frontend.FrontendAnswers
import models.frontend.nics.NICsClass4Answers.{Class4ExemptionAnswers, classFourNoneExempt, classFourOtherExemption}
import play.api.libs.json.{Format, Json}

case class NICsClass4Answers(class4NICs: Boolean,
                             class4ExemptionReason: Option[ExemptionReason],
                             class4DivingExempt: Option[List[BusinessId]],
                             class4NonDivingExempt: Option[List[BusinessId]]) {

  def userHasSingleBusinessExemption: Boolean = class4ExemptionReason.isDefined

  def journeyIsYesButNoneAreExempt: Boolean =
    class4DivingExempt.contains(classFourOtherExemption) && class4NonDivingExempt.contains(classFourNoneExempt)

  def cleanUpExemptionListsFromFE: NICsClass4Answers = {
    val diving      = class4DivingExempt.map(_.filterNot(_ == classFourOtherExemption)).filter(_.nonEmpty)
    val trustee     = class4NonDivingExempt.map(_.filterNot(_ == classFourNoneExempt)).filter(_.nonEmpty)
    val class4YesNo = if (diving.isEmpty && trustee.isEmpty) false else class4NICs
    NICsClass4Answers(
      class4YesNo,
      class4ExemptionReason,
      diving,
      trustee
    )
  }

  def toMultipleBusinessesAnswers: List[Class4ExemptionAnswers] = {
    val divers =
      class4DivingExempt.fold(List.empty[Class4ExemptionAnswers])(_.map(id =>
        Class4ExemptionAnswers(id, true, ExemptionReason.DiverDivingInstructor.some)))
    val trustees =
      class4NonDivingExempt.fold(List.empty[Class4ExemptionAnswers])(_.map(id =>
        Class4ExemptionAnswers(id, true, ExemptionReason.TrusteeExecutorAdmin.some)))
    divers ++ trustees
  }

}

object NICsClass4Answers {

  val classFourOtherExemption: BusinessId = BusinessId("class-four-other-exemption")
  val classFourNoneExempt: BusinessId     = BusinessId("class-four-none-exempt")

  implicit val formats: Format[NICsClass4Answers] = Json.format[NICsClass4Answers]

  case class Class4ExemptionAnswers(businessId: BusinessId, class4Exempt: Boolean, exemptionReason: Option[ExemptionReason])
      extends FrontendAnswers[Unit] {
    override def toDbModel: Option[Unit] = None

    override def toDownStreamAnnualNonFinancials(current: Option[AnnualNonFinancials]): Option[AnnualNonFinancials] =
      AnnualNonFinancials(class4Exempt, exemptionReason.map(_.exemptionCode)).some
  }
}
