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

package mocks.services

import cats.data.EitherT
import cats.implicits.catsStdInstancesForFuture
import models.common.JourneyContextWithNino
import models.connector.api_1505.ClaimId
import models.connector.common.ReliefClaim
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.adjustments.WhatDoYouWantToDoWithLoss
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.when
import org.mockito.stubbing.ScalaOngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import services.journeyAnswers.{ReliefClaimsService, UpdateReliefClaimsResponse}

import scala.concurrent.ExecutionContext.Implicits.global

object MockReliefClaimsService {
  val mockInstance: ReliefClaimsService = mock[ReliefClaimsService]

  def getAllReliefClaims(ctx: JourneyContextWithNino)(returnValue: List[ReliefClaim] = Nil): ScalaOngoingStubbing[ApiResultT[List[ReliefClaim]]] =
    when(mockInstance.getAllReliefClaims(eqTo(ctx))(any()))
      .thenReturn(EitherT.pure(returnValue))

  def getAllReliefClaimsFailure(ctx: JourneyContextWithNino)(returnValue: ServiceError): ScalaOngoingStubbing[ApiResultT[List[ReliefClaim]]] =
    when(mockInstance.getAllReliefClaims(eqTo(ctx))(any()))
      .thenReturn(EitherT.leftT(returnValue))

  def createReliefClaims(ctx: JourneyContextWithNino, answers: WhatDoYouWantToDoWithLoss*)(
      returnValue: List[ClaimId] = Nil): ScalaOngoingStubbing[ApiResultT[List[ClaimId]]] =
    when(mockInstance.createReliefClaims(eqTo(ctx), any())(any(), any()))
      .thenReturn(EitherT.pure(returnValue))

  def updateReliefClaims(ctx: JourneyContextWithNino, oldAnswers: List[ReliefClaim], newAnswers: WhatDoYouWantToDoWithLoss*)(
      returnValue: UpdateReliefClaimsResponse): ScalaOngoingStubbing[ApiResultT[UpdateReliefClaimsResponse]] =
    when(mockInstance.updateReliefClaims(eqTo(ctx), eqTo(oldAnswers), eqTo(newAnswers))(any()))
      .thenReturn(EitherT.pure(returnValue))

  def deleteReliefClaims(ctx: JourneyContextWithNino, answersToDelete: Seq[ReliefClaim]): ScalaOngoingStubbing[ApiResultT[Unit]] =
    when(mockInstance.deleteReliefClaims(eqTo(ctx), eqTo(answersToDelete))(any()))
      .thenReturn(EitherT.pure(()))

}
