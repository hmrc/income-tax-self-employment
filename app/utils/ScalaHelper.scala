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

package utils

import scala.concurrent.{ExecutionContext, Future}


object ScalaHelper {
   implicit class FutureEither[L, R](either: Either[L, Future[R]]) {
     
     def toFuture()(implicit ec: ExecutionContext): Future[Either[L, R]] = {
       Some(either).map {
         case Left(s) => Future.successful(Left(s))
         case Right(f) => f.map(Right(_))
       }.get
     }
   }
}
