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

package models

import cats.implicits._
import models.error.ServiceError.InvalidJsonFormatError
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{Json, JsonValidationError, OFormat, __}
import modelsSpec._

class modelsSpec extends AnyWordSpecLike with Matchers {

  "jsonAs" should {
    "read json successfully" in {
      jsonAs[Foo](Json.obj("foo" -> "bar")) shouldBe Foo("bar").asRight
    }

    "return an error when json is invalid" in {
      jsonAs[Foo](Json.obj("foo" -> 1)) shouldBe InvalidJsonFormatError(
        "models.modelsSpec$Foo",
        """{"foo":1}""",
        List((__ \ "foo", List(JsonValidationError(List("error.expected.jsstring")))))).asLeft
    }
  }
}

object modelsSpec {
  case class Foo(foo: String)

  object Foo {
    implicit val format: OFormat[Foo] = Json.format[Foo]

  }
}
