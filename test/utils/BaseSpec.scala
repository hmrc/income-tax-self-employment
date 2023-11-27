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

import models.common.{BusinessId, Nino, RequestData, TaxYear}
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import org.mockito.ArgumentMatchersSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, DefaultActionBuilder}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext

trait BaseSpec extends AnyWordSpec with MockitoSugar with ArgumentMatchersSugar with ScalaFutures {

  protected implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  protected implicit val hc: HeaderCarrier    = HeaderCarrier()

  protected val taxYear: TaxYear         = TaxYear(LocalDate.now().getYear)
  protected val businessId: BusinessId   = BusinessId("someBusinessId")
  protected val nino: Nino               = Nino("someNino")
  protected val requestData: RequestData = RequestData(taxYear, businessId, nino)

  protected val stubControllerComponents: ControllerComponents = Helpers.stubControllerComponents()

  protected val defaultActionBuilder: DefaultActionBuilder = DefaultActionBuilder(stubControllerComponents.parsers.default)

  protected val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("mtditid" -> "1234567890")

  protected val someDownstreamError: SingleDownstreamError =
    SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody("ERROR_CODE", "some reason"))

}
