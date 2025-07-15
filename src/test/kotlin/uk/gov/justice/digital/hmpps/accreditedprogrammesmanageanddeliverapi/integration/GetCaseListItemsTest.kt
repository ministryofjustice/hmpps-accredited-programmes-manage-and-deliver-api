package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.datasource.init.ScriptUtils
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PagedReferralCaseListItem
import javax.sql.DataSource

class GetCaseListItemsTest : IntegrationTestBase() {

  @Autowired
  private lateinit var dataSource: DataSource

  @Autowired
  private lateinit var resourceLoader: ResourceLoader

  @BeforeEach
  override fun beforeEach() {
    dataSource.connection.use {
      val r = resourceLoader.getResource("classpath:db/testData/setup.sql")
      ScriptUtils.executeSqlScript(it, r)
    }
  }

  @AfterEach
  fun afterEach() {
    dataSource.connection.use {
      val r = resourceLoader.getResource("classpath:db/testData/teardown.sql")
      ScriptUtils.executeSqlScript(it, r)
    }
  }

  // The OPEN and CLOSED referrals tests currently return the same values as we don't yet know the statuses which will map to OPEN/CLOSED
  @Test
  fun `getCaseListItems for OPEN referrals return 200 and paged list of referral case list items`() {
    val response = performRequestAndExpectOk(
      HttpMethod.GET,
      "/pages/caselist/open",
      object : ParameterizedTypeReference<PagedReferralCaseListItem>() {},
    )
    val referralCaseListItems = response.content!!

    assertThat(response).isNotNull
    assertThat(response.totalElements).isEqualTo(5)
    assertThat(referralCaseListItems.first().crn).isEqualTo("CRN-123456")

    referralCaseListItems.forEach { item ->
      assertThat(item).hasFieldOrProperty("crn")
      assertThat(item).hasFieldOrProperty("personName")
      assertThat(item).hasFieldOrProperty("referralStatus")
    }
  }

  @Test
  fun `getCaseListItems for CLOSED referrals return 200 and paged list of referral case list items`() {
    val response = performRequestAndExpectOk(
      HttpMethod.GET,
      "/pages/caselist/open",
      object : ParameterizedTypeReference<PagedReferralCaseListItem>() {},
    )
    val referralCaseListItems = response.content!!

    assertThat(response).isNotNull
    assertThat(response.totalElements).isEqualTo(5)
    assertThat(referralCaseListItems.first().crn).isEqualTo("CRN-123456")

    referralCaseListItems.forEach { item ->
      assertThat(item).hasFieldOrProperty("crn")
      assertThat(item).hasFieldOrProperty("personName")
      assertThat(item).hasFieldOrProperty("referralStatus")
    }
  }

  @Test
  fun `getCaseListItems for invalid ENUM type returns 400 BAD REQUEST`() {
    val response = performRequestAndExpectStatus(
      HttpMethod.GET,
      "/pages/caselist/INVALID_ENUM",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.BAD_REQUEST.value(),
    )

    assertThat(response.userMessage).isEqualTo("Invalid value for parameter openOrClosed")
  }

  @Test
  fun `should return HTTP 403 Forbidden when retrieving a referral without the appropriate role`() {
    webTestClient
      .method(HttpMethod.GET)
      .uri("/pages/caselist/closed")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
      .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
      .returnResult().responseBody!!
  }
}
