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

package connectors

import base.IntegrationBaseSpec
import cats.implicits.catsSyntaxEitherId
import connectors.data._
import helpers.WiremockSpec
import models.common.JourneyContextWithNino
import models.connector.api_2085.ListOfIncomeSources
import models.error.DownstreamError.GenericDownstreamError
import models.error.ServiceError
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status._

class HipConnectorISpec extends WiremockSpec with IntegrationBaseSpec {

  val connector                   = new HipConnectorImpl(httpClient, appConfig)
  val ctx: JourneyContextWithNino = JourneyContextWithNino(taxYear, businessId, mtditid, nino)

  "deleteBroughtForwardLoss" must {
    "return unit with NO_CONTENT status" in new Api1504Test {
      stubDelete(
        url = hipDownstreamUrl,
        expectedResponse = "",
        expectedStatus = NO_CONTENT
      )
      connector.deleteBroughtForwardLoss(nino, taxYear, lossId).value.futureValue shouldBe Right(())
    }

    "return failure when downstream fails with BadRequest" in new Api1504Test {
      stubDelete(
        url = hipDownstreamUrl,
        expectedResponse = "",
        expectedStatus = BAD_REQUEST
      )
      connector.deleteBroughtForwardLoss(nino, taxYear, lossId).value.futureValue shouldBe Left(GenericDownstreamError(BAD_REQUEST,
        s"Downstream error when calling DELETE http://localhost:11111$hipDownstreamUrl: status=$BAD_REQUEST, body:\n"))
    }

    "return failure when downstream fails with Server error" in new Api1504Test {
      stubDelete(
        url = hipDownstreamUrl,
        expectedResponse = "",
        expectedStatus = INTERNAL_SERVER_ERROR
      )
      connector.deleteBroughtForwardLoss(nino, taxYear, lossId).value.futureValue shouldBe Left(GenericDownstreamError(INTERNAL_SERVER_ERROR,
        s"Downstream error when calling DELETE http://localhost:11111$hipDownstreamUrl: status=$INTERNAL_SERVER_ERROR, body:\n"))

    }
  }

}
