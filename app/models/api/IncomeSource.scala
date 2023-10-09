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

package models.api

import models.mdtp.Business.LatencyDetails

trait IncomeSource {
  val incomeSourceId: String
  val accountingPeriodStartDate: String
  val accountingPeriodEndDate: String
  val firstAccountingPeriodStartDate: Option[String]
  val firstAccountingPeriodEndDate: Option[String]
  val latencyDetails: Option[LatencyDetails]
  val tradingStartDate: Option[String]
  val cashOrAccruals: Option[Boolean]
  val cessationDate: Option[String]
}
