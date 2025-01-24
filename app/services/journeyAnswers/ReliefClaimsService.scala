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

package services.journeyAnswers

import cats.data.EitherT
import connectors.ReliefClaimsConnector
import models.common.{JourneyContextWithNino, JourneyName}
import models.connector.api_1867.ReliefClaim
import models.domain.ApiResultT
import play.api.libs.json.Json
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait ReliefClaimsService {
  def cacheClaimIds(ctx: JourneyContextWithNino, taxYear: String)(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

@Singleton
class ReliefClaimsServiceImpl @Inject()(
                                         reliefClaimsConnector: ReliefClaimsConnector,
                                         repository: JourneyAnswersRepository
                                       )(implicit ec: ExecutionContext) extends ReliefClaimsService {

  override def cacheClaimIds(ctx: JourneyContextWithNino, taxYear: String)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val claimsFuture = if (taxYear >= "2025") {
      reliefClaimsConnector.getReliefClaims1867(taxYear, ctx.mtditid.toString)
    } else {
      reliefClaimsConnector.getReliefClaims1507(taxYear, ctx.mtditid.toString)
    }

    for {
      claims <- EitherT(claimsFuture)
      filteredIds = filterReliefClaims(claims, taxYear, ctx.businessId.value)
      _ <- repository.upsertAnswers(ctx.toJourneyContext(JourneyName.ProfitOrLoss), Json.obj("claimIds" -> filteredIds))
    } yield ()
  }

  private def filterReliefClaims(claims: List[ReliefClaim], taxYear: String, businessId: String): List[String] = {
    claims
      .filter(_.isSelfEmploymentClaim)
      .filter(_.taxYearClaimedFor == taxYear)
      .filter(_.incomeSourceId == businessId)
      .map(_.claimId)
  }
}

