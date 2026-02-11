package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.AppointmentReference
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.DeleteAppointmentsRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheck
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheckResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnitWithOfficeLocations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ProbationPractitioner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementOrLicenceConditionManager
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementStaff
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.OffenceFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.OffencesFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.UpdateAppointmentsRequestFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import java.time.LocalDate

class NDeliusIntegrationApiClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var nDeliusIntegrationApiClient: NDeliusIntegrationApiClient

  @Test
  fun `should return offender identifiers for known CRN`() {
    stubAuthTokenEndpoint()
    val crn = "X123456"
    val identifiers = NDeliusPersonalDetails(
      crn = crn,
      name = FullName(forename = "John", middleNames = "William", surname = "Doe"),
      dateOfBirth = "1980-01-01",
      age = "45",
      sex = CodeDescription(code = "M", description = "Male"),
      ethnicity = CodeDescription(code = "W1", description = "White"),
      probationPractitioner = ProbationPractitioner(
        name = FullName("Jane", "A", "Doe"),
        code = "X321",
        email = "jane.doe@probation.gov.uk",
      ),
      probationDeliveryUnit = CodeDescription(code = "PDU123", description = "North London"),
      team = CodeDescription(code = "TEAM123", description = "Team Two"),
      region = CodeDescription(code = "REGION123", description = "London"),
    )

    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/personal-details"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(identifiers)),
        ),
    )

    when (val response = nDeliusIntegrationApiClient.getPersonalDetails(crn)) {
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val body = response.body as NDeliusPersonalDetails
        assertThat(body.crn).isEqualTo("X123456")
        assertThat(body.name.forename).isEqualTo("John")
        assertThat(body.name.middleNames).isEqualTo("William")
        assertThat(body.name.surname).isEqualTo("Doe")
        assertThat(body.dateOfBirth).isEqualTo("1980-01-01")
        assertThat(body.sex.description).isEqualTo("Male")
        assertThat(body.ethnicity!!.description).isEqualTo("White")
        assertThat(body.probationDeliveryUnit.code).isEqualTo("PDU123")
        assertThat(body.probationDeliveryUnit.description).isEqualTo("North London")
      }

      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return NOT FOUND for unknown CRN`() {
    stubAuthTokenEndpoint()
    val crn = "UNKNOWN123"

    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/personal-details"))
        .willReturn(aResponse().withStatus(404)),
    )

    when (val response = nDeliusIntegrationApiClient.getPersonalDetails(crn)) {
      is ClientResult.Failure.StatusCode<*> -> assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND)
      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return access check success for valid user and CRN`() {
    stubAuthTokenEndpoint()
    val username = "jane.doe"
    val crn = "X654321"
    val accessCheck = LimitedAccessOffenderCheck(
      crn = crn,
      userExcluded = false,
      userRestricted = false,
      exclusionMessage = null,
      restrictionMessage = null,
    )

    wiremock.stubFor(
      post(urlEqualTo("/user/$username/access"))
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(listOf(crn))))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              objectMapper.writeValueAsString(
                LimitedAccessOffenderCheckResponse(listOf(accessCheck)),
              ),
            ),
        ),
    )

    when (val response = nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(crn))) {
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val body = (response.body as LimitedAccessOffenderCheckResponse).access.first()
        assertThat(body.crn).isEqualTo(crn)
        assertThat(body.userExcluded).isFalse()
        assertThat(body.userRestricted).isFalse()
        assertThat(body.exclusionMessage).isNull()
        assertThat(body.restrictionMessage).isNull()
      }

      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return FORBIDDEN when user does not have access`() {
    stubAuthTokenEndpoint()
    val username = "john.doe"
    val crn = "X987654"

    wiremock.stubFor(
      post(urlEqualTo("/user/$username/access"))
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(listOf(crn))))
        .willReturn(aResponse().withStatus(403)),
    )

    when (val response = nDeliusIntegrationApiClient.verifyLimitedAccessOffenderCheck(username, listOf(crn))) {
      is ClientResult.Failure.StatusCode<*> -> assertThat(response.status).isEqualTo(HttpStatus.FORBIDDEN)
      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return offences for known CRN and event number`() {
    stubAuthTokenEndpoint()
    val crn = "X123456"
    val eventNumber = 1

    val mainOffenceDate = LocalDate.of(2022, 5, 15)

    val mainOffence = OffenceFactory()
      .withDate(mainOffenceDate)
      .withMainCategoryCode("63")
      .withMainCategoryDescription("Theft from the person of another")
      .withSubCategoryCode("01")
      .withSubCategoryDescription("Theft from the person of another")
      .produce()

    val additionalOffenceDate1 = LocalDate.of(2021, 7, 23)
    val additionalOffence1 = OffenceFactory()
      .withDate(additionalOffenceDate1)
      .withMainCategoryCode("05")
      .withMainCategoryDescription("Criminal damage")
      .withSubCategoryCode("10")
      .withSubCategoryDescription("Criminal damage - value under £5000")
      .produce()

    val additionalOffenceDate2 = LocalDate.of(2021, 9, 5)
    val additionalOffence2 = OffenceFactory()
      .withDate(additionalOffenceDate2)
      .withMainCategoryCode("04")
      .withMainCategoryDescription("Assault")
      .withSubCategoryCode("01")
      .withSubCategoryDescription("Common assault and battery")
      .produce()

    val offences = OffencesFactory()
      .withMainOffence(mainOffence)
      .withAdditionalOffences(listOf(additionalOffence1, additionalOffence2))
      .produce()

    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/sentence/$eventNumber/offences"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(offences)),
        ),
    )

    when (val response = nDeliusIntegrationApiClient.getOffences(crn, eventNumber)) {
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val body = response.body as Offences

        // Verify main offence
        assertThat(body.mainOffence.date).isEqualTo(mainOffenceDate)
        assertThat(body.mainOffence.mainCategoryCode).isEqualTo("63")
        assertThat(body.mainOffence.mainCategoryDescription).isEqualTo("Theft from the person of another")
        assertThat(body.mainOffence.subCategoryCode).isEqualTo("01")
        assertThat(body.mainOffence.subCategoryDescription).isEqualTo("Theft from the person of another")

        // Verify additional offences
        assertThat(body.additionalOffences).hasSize(2)

        // First additional offence
        assertThat(body.additionalOffences[0].date).isEqualTo(additionalOffenceDate1)
        assertThat(body.additionalOffences[0].mainCategoryCode).isEqualTo("05")
        assertThat(body.additionalOffences[0].mainCategoryDescription).isEqualTo("Criminal damage")
        assertThat(body.additionalOffences[0].subCategoryCode).isEqualTo("10")
        assertThat(body.additionalOffences[0].subCategoryDescription).isEqualTo("Criminal damage - value under £5000")

        // Second additional offence
        assertThat(body.additionalOffences[1].date).isEqualTo(additionalOffenceDate2)
        assertThat(body.additionalOffences[1].mainCategoryCode).isEqualTo("04")
        assertThat(body.additionalOffences[1].mainCategoryDescription).isEqualTo("Assault")
        assertThat(body.additionalOffences[1].subCategoryCode).isEqualTo("01")
        assertThat(body.additionalOffences[1].subCategoryDescription).isEqualTo("Common assault and battery")
      }

      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return NOT FOUND for unknown CRN or event number when getting offences`() {
    stubAuthTokenEndpoint()
    val crn = "X123456"
    val eventNumber = 999

    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/sentence/$eventNumber/offences"))
        .willReturn(aResponse().withStatus(404)),
    )

    when (val response = nDeliusIntegrationApiClient.getOffences(crn, eventNumber)) {
      is ClientResult.Failure.StatusCode<*> -> assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND)
      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return requirement manager details for valid CRN and requirement ID`() {
    //    Given
    stubAuthTokenEndpoint()
    val crn = "X123456"
    val requirementId = "REQ001"

    val requirementResponse = NDeliusCaseRequirementOrLicenceConditionResponse(
      manager = RequirementOrLicenceConditionManager(
        staff = RequirementStaff(
          code = "STAFF001",
          name = FullName(forename = "StaffForename", surname = "StaffSurname"),
        ),
        team = CodeDescription(code = "TEAM001", description = "TeamDescription"),
        probationDeliveryUnit = NDeliusApiProbationDeliveryUnit(code = "PDU001", description = "North London PDU"),
        officeLocations = listOf(
          CodeDescription(code = "OFF001", description = "OfficeOne"),
          CodeDescription(code = "OFF002", description = "OfficeTwo"),
        ),
      ),
      probationDeliveryUnits = listOf(
        NDeliusApiProbationDeliveryUnitWithOfficeLocations(
          code = "NON-PRIMARY-PDU-CODE",
          description = "Non-Primary PDU Description",
          officeLocations = listOf(
            CodeDescription(code = "LOC001", description = "Location 001"),
            CodeDescription(code = "LOC002", description = "Location 002"),
          ),
        ),
        NDeliusApiProbationDeliveryUnitWithOfficeLocations(
          code = "PDU002",
          description = "NON-PRIMARY-PDU-CODE-2",
          officeLocations = listOf(
            CodeDescription(code = "LOC003", description = "Location 003"),
          ),
        ),
      ),
    )

    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/requirement/$requirementId"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(requirementResponse)),
        ),
    )

    //  When
    when (val response = nDeliusIntegrationApiClient.getRequirementManagerDetails(crn, requirementId)) {
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val body = response.body as NDeliusCaseRequirementOrLicenceConditionResponse

        // Then
        assertThat(body.manager.staff.code).isEqualTo("STAFF001")
        assertThat(body.manager.staff.name.forename).isEqualTo("StaffForename")
        assertThat(body.manager.staff.name.surname).isEqualTo("StaffSurname")

        assertThat(body.manager.team.code).isEqualTo("TEAM001")
        assertThat(body.manager.team.description).isEqualTo("TeamDescription")

        assertThat(body.manager.probationDeliveryUnit.code).isEqualTo("PDU001")
        assertThat(body.manager.probationDeliveryUnit.description).isEqualTo("North London PDU")

        assertThat(body.manager.officeLocations).hasSize(2)
        assertThat(body.manager.officeLocations[0].code).isEqualTo("OFF001")
        assertThat(body.manager.officeLocations[0].description).isEqualTo("OfficeOne")
        assertThat(body.manager.officeLocations[1].code).isEqualTo("OFF002")
        assertThat(body.manager.officeLocations[1].description).isEqualTo("OfficeTwo")

        assertThat(body.probationDeliveryUnits).hasSize(2)
        assertThat(body.probationDeliveryUnits[0].code).isEqualTo("NON-PRIMARY-PDU-CODE")
        assertThat(body.probationDeliveryUnits[0].description).isEqualTo("Non-Primary PDU Description")
        assertThat(body.probationDeliveryUnits[0].officeLocations).hasSize(2)
        assertThat(body.probationDeliveryUnits[0].officeLocations[0].code).isEqualTo("LOC001")
        assertThat(body.probationDeliveryUnits[0].officeLocations[0].description).isEqualTo("Location 001")
        assertThat(body.probationDeliveryUnits[0].officeLocations[1].code).isEqualTo("LOC002")
        assertThat(body.probationDeliveryUnits[0].officeLocations[1].description).isEqualTo("Location 002")

        assertThat(body.probationDeliveryUnits[1].code).isEqualTo("PDU002")
        assertThat(body.probationDeliveryUnits[1].description).isEqualTo("NON-PRIMARY-PDU-CODE-2")
        assertThat(body.probationDeliveryUnits[1].officeLocations).hasSize(1)
        assertThat(body.probationDeliveryUnits[1].officeLocations[0].code).isEqualTo("LOC003")
        assertThat(body.probationDeliveryUnits[1].officeLocations[0].description).isEqualTo("Location 003")
      }

      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return NOT FOUND for unknown CRN or requirement ID when getting requirement manager details`() {
    stubAuthTokenEndpoint()
    val crn = "UNKNOWN123"
    val requirementId = "UNKNOWN_REQ"

    wiremock.stubFor(
      get(urlEqualTo("/case/$crn/requirement/$requirementId"))
        .willReturn(aResponse().withStatus(404)),
    )

    when (val response = nDeliusIntegrationApiClient.getRequirementManagerDetails(crn, requirementId)) {
      is ClientResult.Failure.StatusCode<*> -> assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND)
      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should delete appointments in Delius`() {
    stubAuthTokenEndpoint()
    val appointments = DeleteAppointmentsRequest(
      appointments = listOf(
        AppointmentReference(java.util.UUID.randomUUID()),
        AppointmentReference(java.util.UUID.randomUUID()),
      ),
    )

    wiremock.stubFor(
      delete(urlEqualTo("/appointments"))
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(appointments)))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{}"),
        ),
    )

    when (val response = nDeliusIntegrationApiClient.deleteAppointmentsInDelius(appointments)) {
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.OK)
      }

      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return error when deleting appointments in Delius fails`() {
    stubAuthTokenEndpoint()
    val appointments = DeleteAppointmentsRequest(
      appointments = listOf(
        AppointmentReference(java.util.UUID.randomUUID()),
      ),
    )

    wiremock.stubFor(
      delete(urlEqualTo("/appointments"))
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(appointments)))
        .willReturn(aResponse().withStatus(400)),
    )

    when (val response = nDeliusIntegrationApiClient.deleteAppointmentsInDelius(appointments)) {
      is ClientResult.Failure.StatusCode<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.BAD_REQUEST)
      }

      else -> fail("Unexpected result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should update appointments in Delius`() {
    stubAuthTokenEndpoint()
    val appointments = UpdateAppointmentsRequestFactory().produce()

    nDeliusApiStubs.stubSuccessfulPutAppointmentsResponse(appointments)

    when (val response = nDeliusIntegrationApiClient.updateAppointmentsInDelius(appointments)) {
      is ClientResult.Success<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.NO_CONTENT)
      }

      else -> {
        fail("Unexpected result: ${response::class.simpleName}")
      }
    }
  }
}
