package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import java.util.UUID

interface ProgrammeGroupMembershipRepository : JpaRepository<ProgrammeGroupMembershipEntity, UUID>
