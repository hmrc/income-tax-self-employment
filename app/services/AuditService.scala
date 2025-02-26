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

package services

import config.AppConfig
import play.api.Logging
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject() (
    appConfig: AppConfig,
    auditConnector: AuditConnector
)(implicit ec: ExecutionContext)
    extends Logging {

  def sendAuditEvent[T](
      auditType: String,
      detail: T
  )(implicit
      hc: HeaderCarrier,
      writes: Writes[T]
  ): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      ExtendedDataEvent(
        auditSource = appConfig.appName,
        auditType = auditType,
        detail = Json.toJson(detail),
        tags = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails()
      )
    ) map { auditResult: AuditResult =>
      auditResult match {
        case Failure(msg, _) =>
          logger.warn(
            s"The attempt to issue audit event $auditType failed with message : $msg"
          )
          auditResult
        case Disabled =>
          logger.warn(
            s"The attempt to issue audit event $auditType was unsuccessful, as auditing is currently disabled in config"
          ); auditResult
        case _ =>
          logger.info(s"Audit event $auditType issued successful.");
          auditResult
      }
    }
}
