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

package models.frontend.capitalAllowances.zeroEmissionGoodsVehicle

import models.connector.api_1803.{AnnualAllowancesType, SuccessResponseSchema}
import models.database.capitalAllowances.ZeroEmissionGoodsVehicleDb
import org.scalatest.wordspec.AnyWordSpecLike

class ZeroEmissionGoodsVehicleAnswersSpec extends AnyWordSpecLike {
  val zegv            = ZeroEmissionGoodsVehicleDb(false, None, None, None, None, None, None)
  val annualSummaries = SuccessResponseSchema(None, None, None)

  "apply" should {
    "return None for claim amount when zero emission goods vehicle is false" in {
      val result = ZeroEmissionGoodsVehicleAnswers.apply(zegv, annualSummaries)
      assert(result === ZeroEmissionGoodsVehicleAnswers(false, None, None, None, None, None, None, None))
    }

    "return None for claim amount when zero emission goods vehicle allowance is false" in {
      val result = ZeroEmissionGoodsVehicleAnswers.apply(zegv.copy(zeroEmissionGoodsVehicle = true, zegvAllowance = Some(false)), annualSummaries)
      assert(result === ZeroEmissionGoodsVehicleAnswers(true, Some(false), None, None, None, None, None, None))
    }

    "return a claim amount when zero emission goods and claims are true" in {
      val result = ZeroEmissionGoodsVehicleAnswers.apply(
        zegv.copy(zeroEmissionGoodsVehicle = true, zegvAllowance = Some(true)),
        annualSummaries.copy(annualAllowances = Some(
          AnnualAllowancesType.emptyAnnualAllowancesType.copy(
            zeroEmissionGoodsVehicleAllowance = Some(1000)
          )))
      )
      assert(result === ZeroEmissionGoodsVehicleAnswers(true, Some(true), None, None, None, None, None, Some(1000)))
    }

  }
}
