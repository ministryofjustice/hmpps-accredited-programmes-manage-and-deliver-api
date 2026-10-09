package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralDetails
import java.time.LocalDate
import java.util.UUID

class ReferralDetailsFactory {
  private var id: UUID = UUID.randomUUID()
  private var crn: String = "X123456"
  private var personName: String = "John Doe"
  private var personForename: String? = "John"
  private var personSurname: String? = "Doe"
  private var personMiddleNames: String? = null
  private var interventionName: String = "Building Choices"
  private var createdAt: LocalDate = LocalDate.now()
  private var dateOfBirth: LocalDate = LocalDate.of(1980, 1, 1)
  private var probationPractitionerName: String? = "Tom Saunders"
  private var probationPractitionerEmail: String? = null
  private var cohort: OffenceCohort = OffenceCohort.GENERAL_OFFENCE
  private var hasLdc: Boolean = false
  private var hasLdcDisplayText: String = "No"
  private var hasLdcSuccessMessageText: String = "Success"
  private var currentStatusDescription: String = "Awaiting assessment"
  private var currentlyAllocatedGroupCode: String? = null
  private var currentlyAllocatedGroupId: UUID? = null
  private var pdu: String = "North London PDU"
  private var reportingTeam: String = "Team A"
  private var isLAO: Boolean = false

  fun withId(id: UUID) = apply { this.id = id }
  fun withCrn(crn: String) = apply { this.crn = crn }
  fun withPersonName(personName: String) = apply { this.personName = personName }
  fun withPersonForename(personForename: String?) = apply { this.personForename = personForename }
  fun withPersonSurname(personSurname: String?) = apply { this.personSurname = personSurname }
  fun withPersonMiddleNames(personMiddleNames: String?) = apply { this.personMiddleNames = personMiddleNames }
  fun withInterventionName(interventionName: String) = apply { this.interventionName = interventionName }
  fun withCreatedAt(createdAt: LocalDate) = apply { this.createdAt = createdAt }
  fun withDateOfBirth(dateOfBirth: LocalDate) = apply { this.dateOfBirth = dateOfBirth }
  fun withProbationPractitionerName(probationPractitionerName: String?) = apply { this.probationPractitionerName = probationPractitionerName }

  fun withProbationPractitionerEmail(probationPractitionerEmail: String?) = apply { this.probationPractitionerEmail = probationPractitionerEmail }

  fun withCohort(cohort: OffenceCohort) = apply { this.cohort = cohort }
  fun withHasLdc(hasLdc: Boolean) = apply { this.hasLdc = hasLdc }
  fun withCurrentStatusDescription(currentStatusDescription: String) = apply { this.currentStatusDescription = currentStatusDescription }

  fun withPdu(pdu: String) = apply { this.pdu = pdu }
  fun withReportingTeam(reportingTeam: String) = apply { this.reportingTeam = reportingTeam }
  fun withIsLAO(isLAO: Boolean) = apply { this.isLAO = isLAO }

  fun produce() = ReferralDetails(
    id = id,
    crn = crn,
    _personName = personName,
    personForename = personForename,
    personSurname = personSurname,
    personMiddleNames = personMiddleNames,
    interventionName = interventionName,
    createdAt = createdAt,
    dateOfBirth = dateOfBirth,
    probationPractitionerName = probationPractitionerName,
    probationPractitionerEmail = probationPractitionerEmail,
    cohort = cohort,
    hasLdc = hasLdc,
    hasLdcDisplayText = hasLdcDisplayText,
    hasLdcSuccessMessageText = hasLdcSuccessMessageText,
    currentStatusDescription = currentStatusDescription,
    currentlyAllocatedGroupCode = currentlyAllocatedGroupCode,
    currentlyAllocatedGroupId = currentlyAllocatedGroupId,
    pdu = pdu,
    reportingTeam = reportingTeam,
    isLAO = isLAO,
  )
}
