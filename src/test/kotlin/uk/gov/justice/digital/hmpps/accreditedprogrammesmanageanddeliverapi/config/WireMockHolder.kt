package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration

object WireMockHolder {

  val server: WireMockServer = WireMockServer(
    WireMockConfiguration.wireMockConfig()
      .dynamicPort()
      .usingFilesUnderClasspath("simulations")
      .maxLoggedResponseSize(100_000),
  ).apply {
    start()
  }
}
