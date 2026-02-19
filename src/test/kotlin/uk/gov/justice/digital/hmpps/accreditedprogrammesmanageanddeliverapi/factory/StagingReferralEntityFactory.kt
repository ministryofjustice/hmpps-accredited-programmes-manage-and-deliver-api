package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity.StagingReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import java.time.LocalDate

class StagingReferralEntityFactory {
  private var sourceReferralId: String = "LS-REF-001"
  private var crn: String = "A123456B"
  private var firstName: String = "James"
  private var lastName: String = "Mitchell"
  private var createdAt: LocalDate = LocalDate.of(2024, 3, 15)
  private var sourcedFrom: ReferralEntitySourcedFrom = ReferralEntitySourcedFrom.REQUIREMENT
  private var sourcedFromId: String = "REQ-001"
  private var sex: String = "M"
  private var dateOfBirth: LocalDate = LocalDate.of(1985, 7, 22)

  fun withSourceReferralId(sourceReferralId: String) = apply { this.sourceReferralId = sourceReferralId }
  fun withCrn(crn: String) = apply { this.crn = crn }
  fun withFirstName(firstName: String) = apply { this.firstName = firstName }
  fun withLastName(lastName: String) = apply { this.lastName = lastName }
  fun withCreatedAt(createdAt: LocalDate) = apply { this.createdAt = createdAt }
  fun withSourcedFrom(sourcedFrom: ReferralEntitySourcedFrom) = apply { this.sourcedFrom = sourcedFrom }
  fun withSourcedFromId(sourcedFromId: String) = apply { this.sourcedFromId = sourcedFromId }
  fun withSex(sex: String) = apply { this.sex = sex }
  fun withDateOfBirth(dateOfBirth: LocalDate) = apply { this.dateOfBirth = dateOfBirth }

  fun produce() = StagingReferralEntity(
    sourceReferralId = this.sourceReferralId,
    crn = this.crn,
    firstName = this.firstName,
    lastName = this.lastName,
    createdAt = this.createdAt,
    sourcedFrom = this.sourcedFrom,
    sourcedFromId = this.sourcedFromId,
    sex = this.sex,
    dateOfBirth = this.dateOfBirth,
  )
}
