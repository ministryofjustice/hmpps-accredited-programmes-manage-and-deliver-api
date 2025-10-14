package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupsEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.GroupsRepository

@Service
@Transactional
class GroupsService(
  private val groupsRepository: GroupsRepository,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun createGroup(groupCode: String): GroupsEntity? {
    groupsRepository.findByCode(groupCode)?.let { throw ConflictException("Group with code $groupCode already exists") }

    val group = groupsRepository.save(
      GroupsEntity(
        code = groupCode,
      ),
    )
    return group
  }
}
