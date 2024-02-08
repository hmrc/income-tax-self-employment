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

package stubs.services

import models.common.{BusinessId, JourneyName, Mtditid, TaxYear}
import models.domain.ApiResultT
import play.api.libs.json.Writes
import services.journeyAnswers.CapitalAllowancesAnswersService
import stubs.serviceUnitT

case class StubCapitalAllowancesAnswersAnswersService(persistCapitalAllowancesTailoring: ApiResultT[Unit] = serviceUnitT)
    extends CapitalAllowancesAnswersService {

  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
      writes: Writes[A]): ApiResultT[Unit] = persistCapitalAllowancesTailoring
}
