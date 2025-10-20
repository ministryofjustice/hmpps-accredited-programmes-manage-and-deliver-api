package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface GroupWaitlistItemViewRepository :
  JpaRepository<GroupWaitlistItemViewEntity, UUID>,
  JpaSpecificationExecutor<GroupWaitlistItemViewEntity>
