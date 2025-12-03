package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewEntity
import java.util.UUID

interface GroupWaitlistItemViewRepository :
  JpaRepository<GroupWaitlistItemViewEntity, UUID>,
  JpaSpecificationExecutor<GroupWaitlistItemViewEntity>
