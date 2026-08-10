package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.manageUsersApi.model.UserDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.UserDtoFactory

@TestComponent
class ManageUsersApiStubs {

  @Autowired
  private lateinit var wiremock: WireMockServer

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  fun clearAllStubs() {
    wiremock.resetRequests()
  }

  fun stubUserResponse(
    userDto: UserDto? = UserDtoFactory().produce(),
  ) {
    wiremock.stubFor(
      get(urlPathTemplate("/users/{username}"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(userDto)),
        ),
    )
  }

  fun verifyGetUser(count: Int, username: String) {
    val requestPattern = getRequestedFor(urlEqualTo("/users/$username"))
    wiremock.verify(count, requestPattern)
  }
}
