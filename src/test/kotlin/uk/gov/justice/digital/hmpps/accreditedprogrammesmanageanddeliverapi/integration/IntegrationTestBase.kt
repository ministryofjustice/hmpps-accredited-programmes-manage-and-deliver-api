package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.cache.CacheManager
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
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.DomainEventsQueueConfig
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

@Testcontainers
@ExtendWith(HmppsAuthApiExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestReferralHelper::class, DomainEventsQueueConfig::class, TestGroupHelper::class)
@AutoConfigureWebTestClient
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var testDataCleaner: TestDataCleaner

  @Autowired
  protected lateinit var testDataGenerator: TestDataGenerator

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  lateinit var oasysApiStubs: OasysApiStubs

  @Autowired
  lateinit var nDeliusApiStubs: NDeliusApiStubs

  @Autowired
  lateinit var arnsApiStubs: ArnsApiStubs

  @Autowired
  lateinit var govUkApiStubs: GovUkApiStubs

  @Autowired
  lateinit var wiremock: WireMockServer

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

  /**
   * This is used in some tests to provide a fixed clock when specific dates and times matter.
   * It is placed in this class so context is not reloaded by spring when mocking the bean
   * as this causes context loading errors across the tests.
   */
  @MockitoSpyBean
  lateinit var clock: Clock

  @Autowired
  private lateinit var cacheManager: CacheManager

  @BeforeEach
  fun beforeEach() {
    domainEventsQueueConfig.purgeAllQueues()
    cacheManager.getCache("bank-holidays")?.clear()
  }

  companion object {

    @JvmStatic
    private val localStackContainer: LocalStackContainer =
      LocalStackContainer(DockerImageName.parse("localstack/localstack"))
        .apply {
          withEnv("DEFAULT_REGION", "eu-west-2")
          withServices(Service.SNS, Service.SQS)
          withReuse(true)
        }

    @JvmStatic
    private val postgresContainer = PostgreSQLContainer<Nothing>("postgres:17")
      .apply {
        withUsername("admin")
        withPassword("admin_password")
        withReuse(true)
      }

    @BeforeAll
    @JvmStatic
    fun startContainers() {
      localStackContainer.start()
      postgresContainer.start()
    }

    @DynamicPropertySource
    @JvmStatic
    fun setUpProperties(registry: DynamicPropertyRegistry) {
      registry.add("hmpps.sqs.localstackUrl") { localStackContainer.getEndpointOverride(Service.SNS).toString() }
      registry.add("hmpps.sqs.region") { localStackContainer.region }
      registry.add("spring.datasource.url") { postgresContainer.jdbcUrl }
      registry.add("spring.datasource.username") { postgresContainer.username }
      registry.add("spring.datasource.password") { postgresContainer.password }
    }
  }

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf("ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR"),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }

  fun stubAuthTokenEndpoint() {
    hmppsAuth.stubGrantToken()
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
    roles: List<String> = listOf("ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR"),
  ) {
    webTestClient
      .method(httpMethod)
      .uri(uri)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = roles))
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
