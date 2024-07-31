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

package stubs.services

import cats.data.EitherT
import models.common.{BusinessId, Nino}
import models.domain.{ApiResultT, Business}
import models.error.ServiceError
import services.BusinessService
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec.businessId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final case class StubBusinessService(
    getBusinessesResult: Either[ServiceError, List[Business]] = Right(Nil),
    getBusinessResult: Either[ServiceError, Business] = Left(ServiceError.BusinessNotFoundError(businessId))
) extends BusinessService {

  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[List[Business]] =
    EitherT.fromEither[Future](getBusinessesResult)

  def getBusiness(nino: Nino, businessId: BusinessId)(implicit hc: HeaderCarrier): ApiResultT[Business] =
    EitherT.fromEither[Future](getBusinessResult)

}
