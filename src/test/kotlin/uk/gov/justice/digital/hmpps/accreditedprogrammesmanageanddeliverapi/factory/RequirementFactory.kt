package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Manager
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.PduOfficeLocations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.ProbationPractitioner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Requirement
import java.time.ZonedDateTime

class RequirementFactory {
  private var id: Long = 1L
  private var mainCategory: CodeDescription? = CodeDescription("LAP", "Licence - Accredited Programmes")
  private var subCategory: CodeDescription? = CodeDescription("code", "description")
  private var manager: Manager = Manager(
    staff = ProbationPractitioner(
      name = FullName(forename = "Forname", surname = "Surname"),
      code = "Test Office Location",
      email = "test@example.com",
    ),
    team = CodeDescription("TEAM01", "Test Team"),
    probationDeliveryUnit = CodeDescription("PDU1", "Test PDU"),
    officeLocations = listOf(CodeDescription("OFFICE1", "Test Office Location")),
  )
  private var probationDeliveryUnits: List<PduOfficeLocations> = listOf(
    PduOfficeLocations(
      "PDU1",
      "Test PDU",
      officeLocations = listOf(CodeDescription("OFFICE1", "Test Office Location")),
    ),
  )
  private var eventNumber: String = "1"
  private var createdAt: ZonedDateTime = ZonedDateTime.now()

  fun withId(id: Long) = apply { this.id = id }
  fun withMainCategory(mainCategory: CodeDescription?) = apply { this.mainCategory = mainCategory }
  fun withSubCategory(subCategory: CodeDescription?) = apply { this.subCategory = subCategory }
  fun withManager(manager: Manager) = apply { this.manager = manager }
  fun withProbationDeliveryUnits(probationDeliveryUnits: List<PduOfficeLocations>) = apply { this.probationDeliveryUnits = probationDeliveryUnits }
  fun withEventNumber(eventNumber: String) = apply { this.eventNumber = eventNumber }
  fun withCreatedAt(createdAt: ZonedDateTime) = apply { this.createdAt = createdAt }

  fun produce() = Requirement(
    id = id,
    mainCategory = mainCategory,
    subCategory = subCategory,
    manager = manager,
    probationDeliveryUnits = probationDeliveryUnits,
    eventNumber = eventNumber,
    createdAt = createdAt,
  )
}
