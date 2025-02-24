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

package data

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import java.time.temporal.ChronoUnit

trait TimeData {

  val testDate: LocalDate         = LocalDate.of(2024, 2, 17)
  val testDateTime: LocalDateTime = testDate.atTime(1, 0)
  val testInstant: Instant        = testDateTime.truncatedTo(ChronoUnit.SECONDS).toInstant(ZoneOffset.UTC)

  val currentTaxYear: LocalDate = LocalDate.now()

}
