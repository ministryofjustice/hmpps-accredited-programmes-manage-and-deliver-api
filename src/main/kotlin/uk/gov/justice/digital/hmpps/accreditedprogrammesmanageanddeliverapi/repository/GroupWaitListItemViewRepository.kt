package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitListItemViewEntity
import java.util.UUID

@Repository
interface GroupWaitListItemViewRepository :
  JpaRepository<GroupWaitListItemViewEntity, UUID>,
  JpaSpecificationExecutor<GroupWaitListItemViewEntity>
