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

package utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class ZonedDateTimeMachineSpec extends AnyFlatSpec with Matchers with MockTimeMachine {

  "ZonedDateTimeMachine" should
    "return the mocked current time" in {
      val fixedTime = OffsetDateTime.parse("2024-04-30T15:00:00+01:00")
      mockNow(fixedTime)

      val result = mockTimeMachine.now
      result shouldEqual fixedTime.toZonedDateTime.truncatedTo(ChronoUnit.SECONDS)
    }
}
