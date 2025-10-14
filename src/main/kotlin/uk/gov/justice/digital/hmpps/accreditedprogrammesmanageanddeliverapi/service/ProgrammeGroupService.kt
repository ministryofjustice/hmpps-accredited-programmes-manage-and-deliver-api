package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository

@Service
@Transactional
class ProgrammeGroupService(
  private val programmeGroupRepository: ProgrammeGroupRepository,
) {
  fun createGroup(groupCode: String): ProgrammeGroupEntity? {
    programmeGroupRepository.findByCode(groupCode)?.let { throw ConflictException("Programme group with code $groupCode already exists") }

    return programmeGroupRepository.save(
      ProgrammeGroupEntity(
        code = groupCode,
      ),
    )
  }
}
