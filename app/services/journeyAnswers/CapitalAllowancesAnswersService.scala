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

package services.journeyAnswers

import cats.data.EitherT
import cats.implicits.catsSyntaxOptionId
import connectors.SelfEmploymentConnector
import models.common.JourneyName.CapitalAllowancesTailoring
import models.common.TaxYear.{endDate, startDate}
import models.common._
import models.connector.Api1894DeductionsBuilder
import models.connector.api_1802.request.{AnnualAllowances, CreateAmendSEAnnualSubmissionRequestBody, CreateAmendSEAnnualSubmissionRequestData}
import models.connector.api_1894.request.{CreateSEPeriodSummaryRequestBody, CreateSEPeriodSummaryRequestData, FinancialsType}
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.capitalAllowances.CapitalAllowancesTailoringAnswers
import models.frontend.capitalAllowances.zeroEmissionCars.{ZeroEmissionCarsAnswers, ZeroEmissionCarsJourneyAnswers}
import play.api.libs.json.{Json, Writes}
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait CapitalAllowancesAnswersService {
  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
      writes: Writes[A]): ApiResultT[Unit]
  def getCapitalAllowancesTailoring(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[CapitalAllowancesTailoringAnswers]]
  def saveZecAnswers[A: Api1894DeductionsBuilder: Writes](ctx: JourneyContextWithNino, answers: A)(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

@Singleton
class CapitalAllowancesAnswersServiceImpl @Inject() (connector: SelfEmploymentConnector, repository: JourneyAnswersRepository)(implicit
    ec: ExecutionContext)
    extends CapitalAllowancesAnswersService {

  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
      writes: Writes[A]): ApiResultT[Unit] =
    repository.upsertAnswers(JourneyContext(taxYear, businessId, mtditid, journey), Json.toJson(answers))

  def getCapitalAllowancesTailoring(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[CapitalAllowancesTailoringAnswers]] =
    for {
      maybeDbAnswers           <- repository.get(ctx.toJourneyContext(CapitalAllowancesTailoring))
      existingTailoringAnswers <- getPersistedAnswers[CapitalAllowancesTailoringAnswers](maybeDbAnswers)
    } yield existingTailoringAnswers

  def saveZecAnswers(ctx: JourneyContextWithNino, answers: ZeroEmissionCarsJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val existingSEAnnual: CreateAmendSEAnnualSubmissionRequestBody = connector.getAnnualSummaries()
    val existingAnnualAllowances: AnnualAllowances                 = existingSEAnnual.annualAllowances.getOrElse(AnnualAllowances.empty)
    val updatedAnnualAllowances = existingAnnualAllowances.copy(zeroEmissionsCarAllowance = Some(answers.zeroEmissionsCarAllowance))
    val body                    = existingSEAnnual.copy(annualAllowances = Some(updatedAnnualAllowances))
    val requestData             = CreateAmendSEAnnualSubmissionRequestData(ctx.taxYear, ctx.nino, ctx.businessId, body)

    val result = connector.createAmendSEAnnualSubmission(requestData).map(_ => ())

    EitherT.right[ServiceError](result)
  }

}
