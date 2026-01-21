package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

object HmppsAuthHolder {
  val server: WireMockServer = WireMockServer(
    wireMockConfig()
      .dynamicPort()
      .usingFilesUnderClasspath("simulations/auth"),
  ).apply {
    start()
  }
}
