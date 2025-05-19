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

package mocks.connectors

import cats.data.EitherT
import connectors.HIP.IncomeSourcesConnector
import models.common.Nino
import models.connector.ApiResponse
import models.connector.api_2085.ListOfIncomeSources
import models.domain.ApiResultT
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.when
import org.mockito.stubbing.ScalaOngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

object MockIncomeSourcesConnector {

  val mockInstance: IncomeSourcesConnector = mock[IncomeSourcesConnector]

  def getIncomeSources(nino: Nino)
                      (returnValue: ApiResponse[ListOfIncomeSources]): ScalaOngoingStubbing[ApiResultT[ListOfIncomeSources]] =
    when(mockInstance.getIncomeSources(eqTo(nino))(any[HeaderCarrier]))
      .thenReturn(EitherT(Future.successful(returnValue)))

}
