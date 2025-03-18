/*
 * Copyright 2024 HM Revenue & Customs
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

package stubs.controllers.actions

import controllers.actions.AuthorisedAction
import controllers.actions.AuthorisedAction.User
import play.api.mvc.{Action, AnyContent, Request, Result}
import utils.BaseSpec.mtditid
import utils.TestUtils._

import scala.concurrent.Future

case class StubAuthorisedAction() extends AuthorisedAction()(mockAuthConnector, defaultActionBuilder, stubControllerComponents) {

  override def async(block: AuthorisedAction.User[AnyContent] => Future[Result]): Action[AnyContent] = defaultActionBuilder.async {
    implicit request: Request[AnyContent] =>
      val user = User(mtditid.value, None)
      block(user)
  }
}
