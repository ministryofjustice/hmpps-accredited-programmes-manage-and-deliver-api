package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Type
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.toPniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniResponseFactory

@ExtendWith(MockitoExtension::class)
class PniServiceTest {

  @Mock
  private lateinit var oasysApiClient: OasysApiClient

  @InjectMocks
  private lateinit var pniService: PniService

  @Test
  fun `when getPniScore has pniCalculation in oasys return pniScore `() {
    val crn = randomCrn()
    val pniResponse = PniResponseFactory().produce()

    `when`(oasysApiClient.getPniCalculation(crn)).thenReturn(
      ClientResult.Success(
        status = HttpStatus.OK,
        body = pniResponse,
      ),
    )

    val result = pniService.getPniCalculation(crn).toPniScore()
    assertThat(result).isNotNull
    assertThat(result.overallIntensity).isEqualTo(Type.toIntensity(pniResponse.pniCalculation?.pni))
  }

  @Test
  fun `when getPniScore has no pniCalculation in oasys throw exception`() {
    val crn = randomCrn()

    `when`(oasysApiClient.getPniCalculation(crn)).thenReturn(
      ClientResult.Failure.StatusCode(
        HttpMethod.GET,
        "/assessments/pni/$crn?community=true",
        HttpStatus.NOT_FOUND,
        "",
      ),
    )

    assertThrows<NotFoundException> { pniService.getPniCalculation(crn) }
  }
}
