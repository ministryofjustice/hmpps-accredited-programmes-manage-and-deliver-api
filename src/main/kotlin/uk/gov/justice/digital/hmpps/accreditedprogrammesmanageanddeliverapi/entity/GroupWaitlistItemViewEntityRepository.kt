package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GroupWaitlistItemViewEntityRepository : JpaRepository<GroupWaitlistItemViewEntity, UUID>
