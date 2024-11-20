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

package models.error

import models.error.ErrorType.{DomainErrorCode, DownstreamErrorCode}
import play.api.libs.json._

sealed trait DownstreamErrorBody extends ServiceError {
  val errorMessage: String = ""
}

object DownstreamErrorBody {

  implicit val formats: Format[DownstreamErrorBody] = Format(
    Reads { jsValue =>
      SingleDownstreamErrorBody.formats.reads(jsValue) orElse MultipleDownstreamErrorBody.formats.reads(jsValue)
    },
    Writes {
      case ape: SingleDownstreamErrorBody   => SingleDownstreamErrorBody.formats.writes(ape)
      case ape: MultipleDownstreamErrorBody => MultipleDownstreamErrorBody.formats.writes(ape)
    }
  )

  // This response model does not seem to align with the error response schema we get from IFS.
  case class SingleDownstreamErrorBody(code: String, reason: String, errorType: ErrorType = DownstreamErrorCode) extends DownstreamErrorBody {

    def toDomain: SingleDownstreamErrorBody =
      if (errorType == DomainErrorCode) {
        this
      } else {
        val domainCode = code match {
          case "INVALID_NINO"         => "FORMAT_NINO"
          case "UNMATCHED_STUB_ERROR" => "RULE_INCORRECT_GOV_TEST_SCENARIO"
          case "NOT_FOUND"            => "MATCHING_RESOURCE_NOT_FOUND"
          case _                      => "INTERNAL_SERVER_ERROR"
        }
        SingleDownstreamErrorBody(domainCode, reason, errorType = DomainErrorCode)
      }

  }

  object SingleDownstreamErrorBody {
    implicit val formats: OFormat[SingleDownstreamErrorBody] = Json.format[SingleDownstreamErrorBody]

    val parsingError: SingleDownstreamErrorBody = SingleDownstreamErrorBody("PARSING_ERROR", "Error parsing response from API")

    val invalidTaxableEntityId: SingleDownstreamErrorBody =
      SingleDownstreamErrorBody("INVALID_TAXABLE_ENTITY_ID", "Submission has not passed validation. Invalid parameter taxableEntityId.")

    val invalidPayload: SingleDownstreamErrorBody =
      SingleDownstreamErrorBody("INVALID_PAYLOAD", "Submission has not passed validation. Invalid payload.")

    val invalidCorrelationId: SingleDownstreamErrorBody =
      SingleDownstreamErrorBody("INVALID_CORRELATIONID", "Submission has not passed validation. Invalid Header parameter CorrelationId.")

    val accountingPeriodNotEnded: SingleDownstreamErrorBody =
      SingleDownstreamErrorBody("ACCOUNTING_PERIOD_NOT_ENDED", "The remote endpoint has indicated that <Message received from backend ITSD>.")

    val invalidClaimType: SingleDownstreamErrorBody =
      SingleDownstreamErrorBody("INVALID_CLAIM_TYPE", "The remote endpoint has indicated that the claim type not valid for income source.")

    val noAccountingPeriod: SingleDownstreamErrorBody =
      SingleDownstreamErrorBody("NO_ACCOUNTING_PERIOD", "The remote endpoint has indicated that no accounting period for the year of the claim.")

    val taxYearNotSupported: SingleDownstreamErrorBody =
      SingleDownstreamErrorBody(
        "TAX_YEAR_NOT_SUPPORTED",
        "The remote endpoint has indicated that the brought forward losses and loss claims are not supported for the specified tax year."
      )

    val notFound: SingleDownstreamErrorBody = SingleDownstreamErrorBody("NOT_FOUND", "The remote endpoint has indicated that no data can be found.")

    val incomeSourceNotFound: SingleDownstreamErrorBody =
      SingleDownstreamErrorBody("INCOME_SOURCE_NOT_FOUND", "The remote endpoint has indicated that the income source cannot be found.")

    val duplicate: SingleDownstreamErrorBody =
      SingleDownstreamErrorBody("DUPLICATE", "The remote endpoint has indicated that the claim for relief already exists.")

    val serverError: SingleDownstreamErrorBody =
      SingleDownstreamErrorBody("SERVER_ERROR", "IFS is currently experiencing problems that require live service intervention.")

    val serviceUnavailable: SingleDownstreamErrorBody =
      SingleDownstreamErrorBody("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")
  }

  case class MultipleDownstreamErrorBody(failures: Seq[SingleDownstreamErrorBody]) extends DownstreamErrorBody

  object MultipleDownstreamErrorBody {
    implicit val formats: OFormat[MultipleDownstreamErrorBody] = Json.format[MultipleDownstreamErrorBody]
  }

}
