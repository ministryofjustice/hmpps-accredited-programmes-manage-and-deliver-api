package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnitWithOfficeLocations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementOrLicenceConditionManager
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementStaff

class NDeliusCaseRequirementOrLicenceConditionResponseFactory {
  private var eventNumber: Int = 1
  private var staffCode: String = "STAFF001"
  private var staffName: FullName = FullName(forename = "Test", middleNames = null, surname = "Staff")
  private var teamCode: String = "TEAM001"
  private var teamDescription: String = "Test Team"
  private var pduCode: String = "PDU001"
  private var pduDescription: String = "Test PDU"
  private var officeLocations: List<CodeDescription> = emptyList()
  private var probationDeliveryUnits: List<NDeliusApiProbationDeliveryUnitWithOfficeLocations> = emptyList()

  fun withEventNumber(eventNumber: Int) = apply { this.eventNumber = eventNumber }
  fun withStaffCode(staffCode: String) = apply { this.staffCode = staffCode }
  fun withStaffName(staffName: FullName) = apply { this.staffName = staffName }
  fun withTeam(code: String, description: String) = apply {
    this.teamCode = code
    this.teamDescription = description
  }
  fun withPdu(code: String, description: String) = apply {
    this.pduCode = code
    this.pduDescription = description
  }
  fun withOfficeLocations(officeLocations: List<CodeDescription>) = apply { this.officeLocations = officeLocations }

  fun produce() = NDeliusCaseRequirementOrLicenceConditionResponse(
    eventNumber = eventNumber,
    manager = RequirementOrLicenceConditionManager(
      staff = RequirementStaff(code = staffCode, name = staffName),
      team = CodeDescription(teamCode, teamDescription),
      probationDeliveryUnit = NDeliusApiProbationDeliveryUnit(pduCode, pduDescription),
      officeLocations = officeLocations,
    ),
    probationDeliveryUnits = probationDeliveryUnits,
  )
}
