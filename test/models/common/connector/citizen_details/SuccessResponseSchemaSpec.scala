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

package models.common.connector.citizen_details

import bulders.BusinessDataBuilder.getCitizenDetailsResponse
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate

class SuccessResponseSchemaSpec extends AnyWordSpec with Matchers {

  "parseDoBToLocalDate" should {
    "return the user's date of birth as a LocalDate" in {
      val citizenDetails = getCitizenDetailsResponse.copy(dateOfBirth = "30071997")
      val expectedResult = Right(LocalDate.of(1997, 7, 30))

      citizenDetails.parseDoBToLocalDate shouldBe expectedResult
    }
    "return a ServiceError if the date of birth cannot be parsed as a LocalDate" in {
      val citizenDetails       = getCitizenDetailsResponse.copy(dateOfBirth = "99999999")
      val expectedErrorMessage = "Cannot parse JSON: Text '9999-99-99' could not be parsed: Invalid value for MonthOfYear (valid values 1 - 12): 99"

      // noinspection ScalaDeprecation
      val result = citizenDetails.parseDoBToLocalDate.left.get.errorMessage
      result shouldBe expectedErrorMessage
    }
  }

}
