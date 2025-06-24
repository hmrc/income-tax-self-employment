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

package controllers.testonly

import cats.data.EitherT
import cats.implicits.toTraverseOps
import config.AppConfig
import controllers.handleApiUnitResultT
import models.connector.{ApiResponse, commonDeleteReads}
import models.domain.ApiResultT
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging

import javax.inject.Inject
import scala.concurrent.ExecutionContext

/** Contains operations used only by tests and environment with stubs.
  */
class TestOnlyController @Inject() (httpClient: HttpClientV2,
                                    journeyAnswersRepository: JourneyAnswersRepository,
                                    cc: MessagesControllerComponents,
                                    appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {
  private val testNinos = List(
    "AA000001C",
    "AA100001C",
    "AA200001C",
    "BB000001A",
    "BB000001C",
    "BB000002A",
    "BB000002C"
  )

  def isTestEnabled: Action[AnyContent] = Action {
    Ok("true")
  }

  def clearAllData(): Action[AnyContent] = Action.async { implicit request =>
    val res = for {
      _ <- testNinos.traverse(testClearIFSData)
      _ <- journeyAnswersRepository.testOnlyClearAllData()
    } yield ()

    handleApiUnitResultT(res)
  }

  def clearAllBEAndStubData(nino: String): Action[AnyContent] = Action.async { implicit request =>
    val res = for {
      _ <- testClearIFSData(nino)
      _ <- journeyAnswersRepository.testOnlyClearAllData()
    } yield ()

    handleApiUnitResultT(res)
  }

  private def testClearIFSData(nino: String)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val url                                          = url"${appConfig.ifsBaseUrl}/nino/$nino"
    implicit val reads: HttpReads[ApiResponse[Unit]] = commonDeleteReads
    val res                                          = httpClient.delete(url).execute(reads, ec)

    EitherT(res)
  }
}
