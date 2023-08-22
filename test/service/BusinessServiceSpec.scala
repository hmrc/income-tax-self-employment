
package service

import connectors.BusinessConnector
import connectors.BusinessConnector.{IdType, Nino}
import connectors.GetBusinessConnectorISpec.expectedResponseBody
import connectors.httpParsers.GetBusinessesHttpParser.GetBusinessesResponse
import models.api.BusinessData.GetBusinessDataRequest
import org.scalamock.handlers.CallHandler3
import play.api.libs.json.Json
import service.BusinessServiceSpec.expectedResponseBody
import services.BusinessService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class BusinessServiceSpec extends TestUtils {
  val mockBusinessConnector = mock[BusinessConnector]
  
  val service = new BusinessService(mockBusinessConnector)
  val nino = "123456789"
  val businessId = "XAIS123456789012"
  
  val expectedResult: GetBusinessesResponse = Right(Some(Json.parse(expectedResponseBody).as[GetBusinessDataRequest]))

  def stubBusinessConnector(expectedResult: GetBusinessesResponse): CallHandler3[IdType, String, HeaderCarrier, Future[GetBusinessesResponse]] =
    (mockBusinessConnector.getBusinesses(_: IdType, _: String)(_: HeaderCarrier))
    .expects(Nino, nino, *)
    .returning(Future.successful(expectedResult))
  
  "getBusinesses" should {
    
    "return a Right with GetBusinessDataRequest model" in {
      
    }
    
    "return a Left when connector returns None" in {
      stubBusinessConnector(Right(None))
      val result = await(service.getBusinesses(nino, businessId))
       result mustBe Left()
      
    }
  }
  
}


