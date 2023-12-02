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

import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.duration.FiniteDuration

class TestClock(private var instantValue: Instant, zoneId: ZoneId) extends Clock {
  override def instant(): Instant                      = instantValue
  override def withZone(zone: java.time.ZoneId): Clock = Clock.fixed(instant(), zone)
  override def getZone: java.time.ZoneId               = zoneId

  def advanceBy(duration: FiniteDuration): Unit =
    instantValue = instantValue.plus(duration.toMillis, java.time.temporal.ChronoUnit.MILLIS)

  def reset(now: Instant): Unit =
    instantValue = now
}

object TestClock {
  def apply(initialInstant: Instant, zoneId: ZoneId): TestClock = new TestClock(initialInstant, zoneId)
}
