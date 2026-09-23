package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ndelius.MemberDtoFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ndelius.RegionDtoFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ndelius.TeamDtoFactory

class RegionServiceTest {
  private val nDeliusApiIntegrationApiClient = mockk<NDeliusIntegrationApiClient>()
  private val telemetryService = mockk<TelemetryService>(relaxed = true)
  private val regionService = RegionService(nDeliusApiIntegrationApiClient, telemetryService)

  @Test
  fun `getTeamMembersByRegionCode returns a list of members when the API call is successful`() {
    // Given
    val regionCode = "REG1"
    val member1 = MemberDtoFactory().produce(code = "M1", name = FullName(forename = "John", surname = "Doe"))
    val member2 = MemberDtoFactory().produce(code = "M2", name = FullName(forename = "Jane", surname = "Smith"))
    val team1 = TeamDtoFactory().produce(code = "T1", description = "Team 1", members = listOf(member1))
    val team2 = TeamDtoFactory().produce(code = "T2", description = "Team 2", members = listOf(member2))
    val regionDto = RegionDtoFactory().produce(teams = listOf(team1, team2))

    every { nDeliusApiIntegrationApiClient.getAccreditedProgrammesMembersByRegionCode(regionCode) } returns ClientResult.Success(
      HttpStatus.OK,
      regionDto,
    )

    // When
    val result = regionService.getTeamMembersByRegionCode(regionCode)

    // Then
    assertThat(result).hasSize(2)
    assertThat(result[0].personCode).isEqualTo("M1")
    assertThat(result[0].personName).isEqualTo("John Doe")
    assertThat(result[0].teamCode).isEqualTo("T1")
    assertThat(result[0].teamName).isEqualTo("Team 1")
    assertThat(result[1].personCode).isEqualTo("M2")
    assertThat(result[1].personName).isEqualTo("Jane Smith")
    assertThat(result[1].teamCode).isEqualTo("T2")
    assertThat(result[1].teamName).isEqualTo("Team 2")

    verify {
      telemetryService.logToAppInsights(
        "Region.get-accredited-programmes-members-nDelius.success",
        "GET_REGION_ACCREDITED_PROGRAMMES_MEMBERS_N_DELIUS",
        "success",
      )
    }
  }

  @Test
  fun `getTeamMembersByRegionCode filters out duplicate members`() {
    // Given
    val regionCode = "REG1"
    val member1 = MemberDtoFactory().produce(code = "M1", name = FullName(forename = "John", surname = "Doe"))
    val team1 = TeamDtoFactory().produce(code = "T1", description = "Team 1", members = listOf(member1))
    val team2 = TeamDtoFactory().produce(code = "T2", description = "Team 2", members = listOf(member1))
    val regionDto = RegionDtoFactory().produce(teams = listOf(team1, team2))

    every { nDeliusApiIntegrationApiClient.getAccreditedProgrammesMembersByRegionCode(regionCode) } returns ClientResult.Success(
      HttpStatus.OK,
      regionDto,
    )

    // When
    val result = regionService.getTeamMembersByRegionCode(regionCode)

    // Then
    assertThat(result).hasSize(1)
    assertThat(result[0].personCode).isEqualTo("M1")
    assertThat(result[0].personName).isEqualTo("John Doe")

    verify {
      telemetryService.logToAppInsights(
        "Region.get-accredited-programmes-members-nDelius.success",
        "GET_REGION_ACCREDITED_PROGRAMMES_MEMBERS_N_DELIUS",
        "success",
      )
    }
  }

  @Test
  fun `getTeamMembersByRegionCode returns empty list when no teams found`() {
    // Given
    val regionCode = "REG1"
    val regionDto = RegionDtoFactory().produce(teams = emptyList())

    every { nDeliusApiIntegrationApiClient.getAccreditedProgrammesMembersByRegionCode(regionCode) } returns ClientResult.Success(
      HttpStatus.OK,
      regionDto,
    )

    // When
    val result = regionService.getTeamMembersByRegionCode(regionCode)

    // Then
    assertThat(result).isEmpty()
    verify {
      telemetryService.logToAppInsights(
        "Region.get-accredited-programmes-members-nDelius.success",
        "GET_REGION_ACCREDITED_PROGRAMMES_MEMBERS_N_DELIUS",
        "success",
      )
    }
  }

  @Test
  fun `getTeamMembersByRegionCode returns empty list when API call fails`() {
    // Given
    val regionCode = "REG1"
    every { nDeliusApiIntegrationApiClient.getAccreditedProgrammesMembersByRegionCode(regionCode) } returns ClientResult.Failure.StatusCode(
      HttpMethod.GET,
      "/regions/$regionCode/local-admin-units/accredited-programmes/members",
      HttpStatus.INTERNAL_SERVER_ERROR,
      "Error",
    )

    // When
    val result = regionService.getTeamMembersByRegionCode(regionCode)

    // Then
    assertThat(result).isEmpty()
    verify {
      telemetryService.logToAppInsights(
        "Region.get-accredited-programmes-members-nDelius.failure",
        "GET_REGION_ACCREDITED_PROGRAMMES_MEMBERS_N_DELIUS",
        "failure",
      )
    }
  }
}
