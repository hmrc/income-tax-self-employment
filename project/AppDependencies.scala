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
import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "7.21.0"
  private val hmrcMongoVersion = "1.2.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-28" % bootstrapVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-test-play-28"   % hmrcMongoVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.14.2",
    "com.beachape"                 %% "enumeratum"                % "1.7.3",
    "com.beachape"                 %% "enumeratum-play-json"      % "1.7.3"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % hmrcMongoVersion,
    "com.typesafe.play"      %% "play-test"               % current,
    "org.scalatest"          %% "scalatest"               % "3.2.15",
    "org.scalatestplus"      %% "scalacheck-1-15"         % "3.2.10.0",
    "org.scalatestplus"      %% "mockito-3-4"             % "3.2.10.0",
    "org.mockito"            %% "mockito-scala"           % "1.16.42",
    "com.vladsch.flexmark"    % "flexmark-all"            % "0.64.6",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0",
    "com.github.tomakehurst"  % "wiremock-jre8"           % "2.35.0",
    "org.scalamock"          %% "scalamock"               % "5.2.0"
  ).map(_ % s"$Test, $IntegrationTest")

}
