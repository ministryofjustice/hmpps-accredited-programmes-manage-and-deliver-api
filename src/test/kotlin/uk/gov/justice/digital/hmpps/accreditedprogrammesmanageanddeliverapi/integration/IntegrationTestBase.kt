package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.http.HttpHeader
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.DomainEventsQueueConfig
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.LocalStackHolder
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.WireMockConfig
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.WireMockHolder
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.ArnsApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.GovUkApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.NDeliusApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.wiremock.stubs.OasysApiStubs
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AccreditedProgrammeTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.TestGroupHelper
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.TestReferralHelper
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset
import com.github.tomakehurst.wiremock.http.HttpHeaders as WireMockHeaders

@Testcontainers
@ExtendWith(HmppsAuthApiExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(
  TestReferralHelper::class,
  DomainEventsQueueConfig::class,
  TestGroupHelper::class,
  WireMockConfig::class,
)
abstract class IntegrationTestBase {

  @Autowired
  lateinit var testDataCleaner: TestDataCleaner

  @Autowired
  lateinit var testDataGenerator: TestDataGenerator

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  lateinit var oasysApiStubs: OasysApiStubs

  @Autowired
  lateinit var nDeliusApiStubs: NDeliusApiStubs

  @Autowired
  lateinit var arnsApiStubs: ArnsApiStubs

  @Autowired
  lateinit var govUkApiStubs: GovUkApiStubs

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @Autowired
  lateinit var domainEventsQueueConfig: DomainEventsQueueConfig

  @Autowired
  lateinit var testReferralHelper: TestReferralHelper

  @Autowired
  lateinit var testGroupHelper: TestGroupHelper

  @Autowired
  lateinit var accreditedProgrammeTemplateRepository: AccreditedProgrammeTemplateRepository

  @Autowired
  lateinit var wiremock: WireMockServer

  /**
   * Used by tests that require a fixed clock without reloading Spring context
   */
  @MockitoSpyBean
  lateinit var clock: Clock

  @BeforeAll
  fun resetWireMockOnce() {
    wiremock.resetAll()
  }

  @BeforeEach
  fun beforeEach() {
    domainEventsQueueConfig.purgeAllQueues()
  }

  companion object {

    @Container
    @JvmStatic
    private val postgresContainer =
      PostgreSQLContainer<Nothing>("postgres:17").apply {
        withUsername("admin")
        withPassword("admin_password")
        withReuse(true)
      }

    init {
      postgresContainer.start()
    }

    @DynamicPropertySource
    @JvmStatic
    fun setUpProperties(registry: DynamicPropertyRegistry) {
      val baseUrl = "http://localhost:${WireMockHolder.server.port()}"
      val localStackContainer = LocalStackHolder.container

      registry.add("services.find-and-refer-intervention-api.base-url") { baseUrl }
      registry.add("services.ndelius-integration-api.base-url") { baseUrl }
      registry.add("services.oasys-api.base-url") { baseUrl }
      registry.add("services.assess-risks-and-needs-api.base-url") { baseUrl }
      registry.add("services.govuk-api.base-url") { baseUrl }
      registry.add("hmpps-auth.url") { "http://localhost:${WireMockHolder.server.port()}/auth" }

      registry.add("hmpps.sqs.localstackUrl") {
        localStackContainer.getEndpointOverride(Service.SNS).toString()
      }
      registry.add("hmpps.sqs.region") { localStackContainer.region }

      registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
      registry.add("spring.datasource.username", postgresContainer::getUsername)
      registry.add("spring.datasource.password", postgresContainer::getPassword)
    }
  }

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf("ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR"),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubFor(
      get("/auth/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) """{"status":"UP"}""" else """{"status":"DOWN"}""")
          .withStatus(status),
      ),
    )
  }

  fun stubAuthTokenEndpoint() {
    hmppsAuth.stubFor(
      post(urlEqualTo("/auth/oauth/token"))
        .willReturn(
          aResponse()
            .withHeaders(WireMockHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """
                {
                  "token_type": "bearer",
                  "access_token": "ABCDE",
                  "expires_in": ${LocalDateTime.now().plusHours(2).toEpochSecond(ZoneOffset.UTC)}
                }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun <T : Any> performRequestAndExpectOk(
    httpMethod: HttpMethod,
    uri: String,
    returnType: ParameterizedTypeReference<T>,
  ): T = performRequestAndExpectStatus(httpMethod, uri, returnType, HttpStatus.OK.value())

  fun <T : Any> performRequestAndExpectStatus(
    httpMethod: HttpMethod,
    uri: String,
    returnType: ParameterizedTypeReference<T>,
    expectedResponseStatus: Int,
  ): T = webTestClient
    .method(httpMethod)
    .uri(uri)
    .contentType(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isEqualTo(expectedResponseStatus)
    .expectBody(returnType)
    .returnResult().responseBody!!

  fun <T : Any> performRequestAndExpectStatusWithBody(
    httpMethod: HttpMethod,
    uri: String,
    returnType: ParameterizedTypeReference<T>,
    body: Any,
    expectedResponseStatus: Int,
  ): T = webTestClient
    .method(httpMethod)
    .uri(uri)
    .contentType(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation())
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(body)
    .exchange()
    .expectStatus().isEqualTo(expectedResponseStatus)
    .expectBody(returnType)
    .returnResult().responseBody!!

  fun performRequestAndExpectStatus(
    httpMethod: HttpMethod,
    uri: String,
    body: Any,
    expectedResponseStatus: Int,
  ) {
    webTestClient
      .method(httpMethod)
      .uri(uri)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation())
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .exchange()
      .expectStatus()
      .isEqualTo(expectedResponseStatus)
  }

  fun performRequestAndExpectStatusNoBody(
    httpMethod: HttpMethod,
    uri: String,
    expectedResponseStatus: Int,
  ) {
    webTestClient
      .method(httpMethod)
      .uri(uri)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isEqualTo(expectedResponseStatus)
  }

  fun performRequestAndExpectStatusAndReturnBody(
    httpMethod: HttpMethod,
    uri: String,
    body: Any = ' ',
    expectedResponseStatus: Int,
  ): WebTestClient.BodyContentSpec = webTestClient
    .method(httpMethod)
    .uri(uri)
    .contentType(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation())
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(body)
    .exchange()
    .expectStatus()
    .isEqualTo(expectedResponseStatus)
    .expectBody()
}
