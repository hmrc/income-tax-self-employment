package helpers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

trait WiremockHelper extends WiremockStubHelpers {

  val wireMockPort                   = 11111
  val wireMockServer: WireMockServer = new WireMockServer(wireMockConfig().port(wireMockPort))

  def startWiremock(): Unit = {
    wireMockServer.start()
    WireMock.configureFor("localhost", wireMockPort)
  }

}
