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

package config

import com.typesafe.config.ConfigFactory
import models.common.{BusinessId, Nino, TaxYear}
import models.connector.ApiName
import models.connector.IntegrationContext.IntegrationHeaderCarrier
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig) extends Logging {
  val appName: String          = config.get[String]("appName")
  val authBaseUrl: String      = servicesConfig.baseUrl("auth")
  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  val graphiteHost: String     = config.get[String]("microservice.metrics.graphite.host")
  val testMode: List[String] =
    config.getOptional[String]("microservice.services.integration-framework.test-mode").map(_.split(",").toList).getOrElse(Nil)

  if (testMode.nonEmpty) {
    logger.warn("!! TEST MODE enabled in microservice.services.test-mode - YOU SHOULD NOT SEE THIS MESSAGE ON PROD or END TO END tests !!")
    logger.info(s"Test Scenarios activated: ${testMode.mkString(",")}")
  }

  val ifsEnvironment: String = config.get[String]("microservice.services.integration-framework.environment")
  val hipEnvironment: String = config.get[String]("microservice.services.hip-integration-framework.environment")

  // TODO This is not good. It means that the app will fail on missing config only on first request, not on bootstrap
  def ifsAuthorisationToken(api: String): String = config.get[String](s"microservice.services.integration-framework.authorisation-token.$api")
  def hipAuthorisationToken(api: String): String = config.get[String](s"microservice.services.hip-integration-framework.authorisation-token.$api")

  val ifsBaseUrl: String = servicesConfig.baseUrl("integration-framework")

  val hipBaseUrl: String = servicesConfig.baseUrl("hip-integration-framework")

  val ifsApi1171: String = servicesConfig.baseUrl("integration-framework-api1171")

  val api1507Url: BusinessId => String = businessId => s"$ifsBaseUrl/income-tax/claims-for-relief/${businessId.value}"

  def api1505Url(businessId: BusinessId): String = s"$ifsBaseUrl/income-tax/claims-for-relief/${businessId.value}"

  def api1506Url(businessId: BusinessId, claimId: String): String = s"$ifsBaseUrl/income-tax/claims-for-relief/${businessId.value}/$claimId"

  def api1508Url(businessId: BusinessId, claimId: String): String = s"$ifsBaseUrl/income-tax/claims-for-relief/${businessId.value}/$claimId"

  val api1867Url: (TaxYear, BusinessId) => String =
    (taxYear, businessId) => s"$ifsBaseUrl/income-tax/${TaxYear.asTys(taxYear)}/claims-for-relief/${businessId.value}"

  val citizenDetailsUrl: String = servicesConfig.baseUrl("citizen-details")

  val mongoTTL: Int = Duration(servicesConfig.getString("mongodb.timeToLive")).toDays.toInt

  val headerCarrierConfig: HeaderCarrier.Config = HeaderCarrier.Config.fromConfig(ConfigFactory.load())

  def mkMetadata(apiName: ApiName, url: String): IntegrationHeaderCarrier =
    IntegrationHeaderCarrier(headerCarrierConfig, this, apiName, url)

  // Feature switching
  def emaSupportingAgentsEnabled: Boolean = servicesConfig.getBoolean("feature-switch.ema-supporting-agents-enabled")

}
