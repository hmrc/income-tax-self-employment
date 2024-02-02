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

import cats.implicits.catsSyntaxEitherId
import gens.CapitalAllowancesTailoringAnswersGen.capitalAllowancesTailoringAnswersGen
import gens.genOne
import models.common.JourneyName.CapitalAllowancesTailoring
import org.scalatest.wordspec.AnyWordSpecLike
import stubs.repositories.StubJourneyAnswersRepository
import utils.BaseSpec._
import utils.EitherTTestOps._

class CapitalAllowancesServiceImplSpec extends AnyWordSpecLike {
  val service = new CapitalAllowancesServiceImpl(StubJourneyAnswersRepository())

  "persistAnswers" should {
    "persist answers successfully" in {
      val answers = genOne(capitalAllowancesTailoringAnswersGen)
      val result  = service.persistAnswers(businessId, currTaxYear, mtditid, CapitalAllowancesTailoring, answers).value.futureValue
      assert(result === ().asRight)
    }
  }
}
