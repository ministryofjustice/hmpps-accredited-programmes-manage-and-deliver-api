package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FacilitatorEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.FacilitatorRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.time.LocalDate
import java.time.LocalDateTime

object ReportingGroupSizeTestDataHelper {
  fun createReportingGroup(
    referralRepository: ReferralRepository,
    facilitatorRepository: FacilitatorRepository,
    programmeGroupRepository: ProgrammeGroupRepository,
    programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository,
    groupCode: String,
    facilitatorStaffCode: String,
    createdAt: LocalDateTime,
    earliestStartDate: LocalDate,
    sex: ProgrammeGroupSexEnum = ProgrammeGroupSexEnum.MIXED,
    cohort: OffenceCohort = OffenceCohort.SEXUAL_OFFENCE,
    isLdc: Boolean = true,
    regionName: String = "North East",
    pduName: String = "Leeds PDU",
    pduCode: String = "LDS01",
    locationName: String = "Leeds Office",
    locationCode: String = "LOC01",
  ): ProgrammeGroupEntity {
    val facilitator = facilitatorRepository.save(
      FacilitatorEntityFactory()
        .withNdeliusPersonCode(facilitatorStaffCode)
        .produce(),
    )

    val group = programmeGroupRepository.save(
      ProgrammeGroupFactory()
        .withCode(groupCode)
        .withCreatedAt(createdAt)
        .withSex(sex)
        .withCohort(cohort)
        .withIsLdc(isLdc)
        .withRegionName(regionName)
        .withProbationDeliveryUnit(pduName, pduCode)
        .withDeliveryLocation(locationName, locationCode)
        .withEarliestStartDate(earliestStartDate)
        .withTreatmentManager(facilitator)
        .produce(),
    )

    val referral = referralRepository.save(ReferralEntityFactory().produce())
    programmeGroupMembershipRepository.save(ProgrammeGroupMembershipFactory(referral, group).produce())

    return group
  }
}
