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

package mocks

import cats.data.EitherT
import cats.implicits.catsStdInstancesForFuture
import models.common.JourneyContextWithNino
import models.domain.ApiResultT
import models.error.ServiceError
import org.mockito.ArgumentMatchers
import org.mockito.MockitoSugar.when
import org.mockito.stubbing.ScalaOngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import services.journeyAnswers.ReliefClaimsService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MockReliefClaimsService {
  val mockInstance: ReliefClaimsService = mock[ReliefClaimsService]

  def cacheClaimIds(ctx: JourneyContextWithNino, taxYear: String): ScalaOngoingStubbing[ApiResultT[Unit]] =
    when(mockInstance.cacheReliefClaims(ArgumentMatchers.eq(ctx), ArgumentMatchers.eq(taxYear))(ArgumentMatchers.any[HeaderCarrier]()))
      .thenReturn(EitherT.right[ServiceError](Future.successful(())))

}
