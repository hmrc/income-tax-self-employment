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

package repositories

import helpers.JourneyAnswersHelper
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.DefaultAwaitTimeout
import testdata.{CommonTestData, IntegrationTimeData}
import uk.gov.hmrc.mongo.test.MongoSupport

trait MongoSpec
    extends AnyWordSpec
    with Matchers
    with MongoSupport
    with BeforeAndAfterEach
    with GuiceOneAppPerSuite
    with OptionValues
    with DefaultAwaitTimeout
    with CommonTestData
    with IntegrationTimeData
    with JourneyAnswersHelper {

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = Span(5, Seconds),
    interval = Span(500, Millis)
  )

}
