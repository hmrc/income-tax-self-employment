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

import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.13.0"
  private val hmrcMongoVersion = "2.6.0"

  val jacksonAndPlayExclusions: Seq[InclusionRule] = Seq(
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "com.fasterxml.jackson.datatype"),
    ExclusionRule(organization = "com.fasterxml.jackson.module"),
    ExclusionRule(organization = "com.fasterxml.jackson.core:jackson-annotations"),
    ExclusionRule(organization = "com.typesafe.play")
  )

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.19.0",
    "org.typelevel"                %% "cats-core"                 % "2.13.0",
    "com.beachape"                 %% "enumeratum"                % "1.9.0",
    "com.beachape"                 %% "enumeratum-play-json"      % "1.9.0" excludeAll (jacksonAndPlayExclusions *),
    "org.codehaus.janino"           % "janino"                    % "3.1.12" // it's required by logback for conditional logging
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"   % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30"  % hmrcMongoVersion,
    "org.playframework"      %% "play-test"                % current,
    "org.scalatest"          %% "scalatest"                % "3.2.19",
    "org.scalatestplus"      %% "scalacheck-1-15"          % "3.2.11.0",
    "org.mockito"            %% "mockito-scala-scalatest"  % "1.17.37",
    "org.typelevel"          %% "cats-core"                % "2.12.0",
    "com.vladsch.flexmark"    % "flexmark-all"             % "0.64.8",
    "org.scalatestplus.play" %% "scalatestplus-play"       % "7.0.1",
    "com.github.tomakehurst"  % "wiremock-jre8-standalone" % "3.0.1",
    "org.scalamock"          %% "scalamock"                % "6.0.0"
  ).map(_ % s"$Test, $IntegrationTest")

}
