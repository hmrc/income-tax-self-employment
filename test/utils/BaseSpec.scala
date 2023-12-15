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

package utils

import models.common._
import models.error.DownstreamError.{MultipleDownstreamErrors, SingleDownstreamError}
import models.error.DownstreamErrorBody.{MultipleDownstreamErrorBody, SingleDownstreamErrorBody}
import org.mockito.ArgumentMatchersSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.BAD_REQUEST
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, DefaultActionBuilder}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, ZoneOffset}
import scala.concurrent.ExecutionContext

trait BaseSpec extends AnyWordSpec with MockitoSugar with ArgumentMatchersSugar with ScalaFutures {

  protected implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  protected implicit val hc: HeaderCarrier    = HeaderCarrier()

  protected val taxYear: TaxYear       = TaxYear(LocalDate.now().getYear)
  protected val businessId: BusinessId = BusinessId("someBusinessId")
  protected val nino: Nino             = Nino("someNino")

  protected val stubControllerComponents: ControllerComponents = Helpers.stubControllerComponents()

  protected val defaultActionBuilder: DefaultActionBuilder = DefaultActionBuilder(stubControllerComponents.parsers.default)

  protected val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("mtditid" -> "1234567890")

  protected val singleDownstreamError: SingleDownstreamError =
    SingleDownstreamError(BAD_REQUEST, SingleDownstreamErrorBody.invalidNino)

  protected val multipleDownstreamErrors: MultipleDownstreamErrors =
    MultipleDownstreamErrors(
      BAD_REQUEST,
      MultipleDownstreamErrorBody(Seq(SingleDownstreamErrorBody.invalidNino, SingleDownstreamErrorBody.invalidMtdid))
    )
}

object BaseSpec {
  // static data
  val taxYear: TaxYear       = TaxYear(2023)
  val fromTaxYearStr: String = s"${taxYear.endYear - 1}-04-06"
  val toTaxYearStr: String   = s"${taxYear.endYear}-04-05"
  val businessId: BusinessId = BusinessId("someBusinessId")
  val nino: Nino             = Nino("nino")
  val mtditid: Mtditid       = Mtditid("1234567890")

  // dynamic & generated data
  val currTaxYear: TaxYear         = TaxYear(LocalDate.now().getYear)
  val sampleFromTaxYearStr: String = s"${currTaxYear.endYear - 1}-04-06"
  val sampleToTaxYearStr: String   = s"${currTaxYear.endYear}-04-05"

  // more complex data
  val journeyCtxWithNino: JourneyContextWithNino = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino)

  // operations
  def mkNow(): Instant                 = Instant.now().truncatedTo(ChronoUnit.SECONDS)
  def mkClock(now: Instant): TestClock = TestClock(now, ZoneOffset.UTC)

}
