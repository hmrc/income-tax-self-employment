
package mocks.repositories

import data.CommonTestData
import models.common.{JourneyContext, JourneyContextWithNino, JourneyName}
import models.database.JourneyAnswers
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar._
import repositories.JourneyAnswersRepository

object MockJourneyAnswersRepository {
  testData: CommonTestData =>

  val mockInstance: JourneyAnswersRepository = mock[JourneyAnswersRepository]

  def get[T](context: JourneyContext)(returnValue: Option[T]): Unit =
    when(mockInstance.get(context)).thenReturn(
      returnValue.map(value =>
        JourneyAnswers(testData.testMtdId, testData.testBusinessId, JourneyName.ProfitOrLoss, value))
    )

}
