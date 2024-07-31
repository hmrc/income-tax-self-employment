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

import com.google.inject.AbstractModule
import connectors.{GetBusinessDetailsConnector, GetBusinessDetailsConnectorImpl, SelfEmploymentConnector, SelfEmploymentConnectorImpl}
import repositories.{JourneyAnswersRepository, MongoJourneyAnswersRepository}
import services.journeyAnswers._
import services.{BusinessService, BusinessServiceImpl}

import java.time.{Clock, ZoneOffset}

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[AppConfig]).asEagerSingleton()
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))
    bind(classOf[PrepopAnswersService]).to(classOf[PrepopAnswersServiceImpl])
    bind(classOf[AbroadAnswersService]).to(classOf[AbroadAnswersServiceImpl])
    bind(classOf[IncomeAnswersService]).to(classOf[IncomeAnswersServiceImpl])
    bind(classOf[ExpensesAnswersService]).to(classOf[ExpensesAnswersServiceImpl])
    bind(classOf[CapitalAllowancesAnswersService]).to(classOf[CapitalAllowancesAnswersServiceImpl])
    bind(classOf[BusinessService]).to(classOf[BusinessServiceImpl])
    bind(classOf[JourneyAnswersRepository]).to(classOf[MongoJourneyAnswersRepository])
    bind(classOf[SelfEmploymentConnector]).to(classOf[SelfEmploymentConnectorImpl])
    bind(classOf[GetBusinessDetailsConnector]).to(classOf[GetBusinessDetailsConnectorImpl])
    bind(classOf[JourneyStatusService]).to(classOf[JourneyStatusServiceImpl])
    ()
  }

}
