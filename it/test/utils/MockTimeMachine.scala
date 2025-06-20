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

import org.mockito.MockitoSugar
import org.mockito.stubbing.ScalaOngoingStubbing

import java.time.temporal.ChronoUnit
import java.time.{OffsetDateTime, ZonedDateTime}

trait MockTimeMachine extends MockitoSugar {

  val mockTimeMachine: TimeMachine = mock[TimeMachine]

  def mockNow(setNow: OffsetDateTime): ScalaOngoingStubbing[ZonedDateTime] =
    when(mockTimeMachine.now).thenReturn(setNow.toZonedDateTime.truncatedTo(ChronoUnit.SECONDS))
}
