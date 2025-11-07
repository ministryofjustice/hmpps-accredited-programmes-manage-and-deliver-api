package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ldc.UpdateLdc
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralLdcHistoryRepository

class LdcControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralLdcHistoryRepository: ReferralLdcHistoryRepository

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `update the LDC status for a referral when valid request sent and no existing ldc status`() {
    val referralEntity = testReferralHelper.createReferral()
    val updateLdc = UpdateLdc(hasLdc = true)

    assertThat(referralLdcHistoryRepository.count()).isOne
    assertThat(referralLdcHistoryRepository.findTopByReferralIdOrderByCreatedAtDesc(referralEntity.id!!)!!.hasLdc).isFalse

    performRequestAndExpectStatus(
      httpMethod = HttpMethod.POST,
      uri = "/referral/${referralEntity.id}/update-ldc",
      body = updateLdc,
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    assertThat(referralLdcHistoryRepository.count()).isEqualTo(2)
    val savedHistory =
      referralLdcHistoryRepository.findTopByReferralIdOrderByCreatedAtDesc(referralEntity.id!!)!!
    assertThat(savedHistory.hasLdc).isTrue
    assertThat(savedHistory.referral.id).isEqualTo(referralEntity.id)
  }

  @Test
  fun `should return bad request if missing hasLdc property`() {
    val referralEntity = testReferralHelper.createReferral()

    assertThat(referralLdcHistoryRepository.count()).isOne

    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/referral/${referralEntity.id}/update-ldc",
      returnType = object : ParameterizedTypeReference<ErrorResponse>() {},
      body = """{}""",
      expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
    )

    assertThat(referralLdcHistoryRepository.count()).isOne
  }
}
