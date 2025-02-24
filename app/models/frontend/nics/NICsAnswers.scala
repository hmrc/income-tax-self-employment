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
import models.connector.api_1639.SuccessResponseAPI1639
import models.connector.api_1803
import models.connector.api_1803.AnnualNonFinancialsType
import models.database.nics.NICsStorageAnswers
import models.frontend.nics.NICsClass4Answers._
import play.api.libs.json.{Format, Json}

case class NICsAnswers(class2Answers: Option[NICsClass2Answers], class4Answers: Option[NICsClass4Answers])

object NICsAnswers {
  implicit val formats: Format[NICsAnswers] = Json.format[NICsAnswers]

  def mkPriorData(maybeDisclosures: Option[SuccessResponseAPI1639],
                  maybeAnnualSummaries: List[(api_1803.SuccessResponseSchema, BusinessId)],
                  maybeDbAnswers: Option[NICsStorageAnswers]): Option[NICsAnswers] = {

    val class2Data: Option[Boolean]              = maybeDisclosures.flatMap(_.class2Nics.flatMap(_.class2VoluntaryContributions))
    val class2DatabaseData: Option[Boolean]      = maybeDbAnswers.flatMap(_.class2NICs)
    val class4Data: List[Class4ExemptionAnswers] = mkClass4ExemptionData(maybeAnnualSummaries)
    (class2Data, class2DatabaseData, class4Data) match {
      case (_, _, class4Data) if class4Data.nonEmpty => mkPriorClass4Data(class4Data, maybeDbAnswers)
      case (Some(true), _, _)                        => Some(NICsAnswers(class2Answers = NICsClass2Answers(true).some, None))
      case (None, Some(false), _)                    => Some(NICsAnswers(class2Answers = NICsClass2Answers(false).some, None))
      case _                                         => None
    }
  }

  def mkClass4ExemptionData(multipleAnnualSummaries: List[(api_1803.SuccessResponseSchema, BusinessId)]): List[Class4ExemptionAnswers] =
    multipleAnnualSummaries.flatMap { case (answers, businessId) =>
      val maybeClass4Exemption: Option[Boolean] = answers.annualNonFinancials.flatMap(_.exemptFromPayingClass4Nics)
      maybeClass4Exemption.map { class4Exempt =>
        val class4ExemptReason: Option[ExemptionReason] = {
          val reason: Option[AnnualNonFinancialsType.Class4NicsExemptionReason.Value] =
            answers.annualNonFinancials.flatMap(_.class4NicsExemptionReason)
          reason.flatMap(ExemptionReason.fromNonFinancialType)
        }
        Class4ExemptionAnswers(businessId, class4Exempt, class4ExemptReason)
      }
    }

  def mkPriorClass4Data(class4Answers: List[Class4ExemptionAnswers], maybeDbAnswers: Option[NICsStorageAnswers]): Option[NICsAnswers] = {
    val journeyIsYesButNoneAreExempt = maybeDbAnswers.exists(_.journeyIsYesButNoneAreExempt.contains(true))

    if (class4Answers.length > 1) buildMultipleBusinessAnswers(class4Answers, journeyIsYesButNoneAreExempt)
    else buildSingleBusinessAnswers(class4Answers)
  }

  private def buildSingleBusinessAnswers(class4Answers: List[Class4ExemptionAnswers]): Option[NICsAnswers] = class4Answers.headOption.map {
    singleExemption =>
      val singleClass4Answers = NICsClass4Answers(singleExemption.class4Exempt, singleExemption.exemptionReason, None, None)
      NICsAnswers(None, Some(singleClass4Answers))
  }

  private def buildMultipleBusinessAnswers(class4Answers: List[Class4ExemptionAnswers],
                                           journeyIsYesButNoneAreExempt: Boolean): Option[NICsAnswers] = {
    val class4NicsBoolean        = class4Answers.exists(_.class4Exempt)
    val class4DivingBusinessIds  = returnBusinessIdsForRelevantExemption(class4Answers, ExemptionReason.DiverDivingInstructor)
    val class4TrusteeBusinessIds = returnBusinessIdsForRelevantExemption(class4Answers, ExemptionReason.TrusteeExecutorAdmin)

    val multipleClass4Answers = (class4NicsBoolean, class4DivingBusinessIds, class4TrusteeBusinessIds, journeyIsYesButNoneAreExempt) match {
      case (false, None, None, true) =>
        // User submitted 'Yes' journey but with no exemptions so 'false' sent to APIs.
        //   'JIYBNAE' is gathered from DB so FE CYA will match user's submission, not API data.
        NICsClass4Answers(true, None, Some(List(classFourOtherExemption)), Some(List(classFourNoneExempt)))
      case (true, diving, _, _) if !diving.exists(_.nonEmpty) =>
        // If no diving exemptions, 'Exempt for other reason' message displayed in CYA row.
        NICsClass4Answers(class4NicsBoolean, None, Some(List(classFourOtherExemption)), class4TrusteeBusinessIds)
      case (true, _, nonDiving, _) if !nonDiving.exists(_.nonEmpty) =>
        // If no non-diving exemptions, that row will not appear on CYA at all.
        NICsClass4Answers(class4NicsBoolean, None, class4DivingBusinessIds, None)
      case _ => NICsClass4Answers(class4NicsBoolean, None, class4DivingBusinessIds, class4TrusteeBusinessIds)
    }

    Some(NICsAnswers(None, Some(multipleClass4Answers)))
  }

  private def returnBusinessIdsForRelevantExemption(exemptions: List[Class4ExemptionAnswers], reason: ExemptionReason): Option[List[BusinessId]] = {
    val filteredExemptions = exemptions.filter(_.exemptionReason.contains(reason))
    if (filteredExemptions.nonEmpty) Some(filteredExemptions.map(_.businessId)) else None
  }

}
