package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupsEntity
import java.util.UUID

interface GroupsRepository : JpaRepository<GroupsEntity, UUID> {

  fun findByCode(code: String): GroupsEntity?
}
