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

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig) {
  val authBaseUrl: String      = servicesConfig.baseUrl("auth")
  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  val graphiteHost: String     = config.get[String]("microservice.metrics.graphite.host")

  val ifsEnvironment: String = config.get[String]("microservice.services.integration-framework.environment")

  // TODO This is not good. It means that the app will fail on missing config only on first request, not on bootstrap
  def ifsAuthorisationToken(api: String): String = config.get[String](s"microservice.services.integration-framework.authorisation-token.$api")

  val ifsBaseUrl: String = servicesConfig.baseUrl("integration-framework")

  val mongoTTL: Int = Duration(servicesConfig.getString("mongodb.timeToLive")).toDays.toInt
}
