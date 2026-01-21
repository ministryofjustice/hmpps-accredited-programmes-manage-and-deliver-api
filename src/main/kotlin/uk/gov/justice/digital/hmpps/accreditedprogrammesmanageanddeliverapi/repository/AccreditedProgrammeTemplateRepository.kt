package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AccreditedProgrammeTemplateEntity
import java.util.UUID

@Repository
interface AccreditedProgrammeTemplateRepository : JpaRepository<AccreditedProgrammeTemplateEntity, UUID> {
  fun findFirstByName(name: String): AccreditedProgrammeTemplateEntity?

  @Query("SELECT e FROM AccreditedProgrammeTemplateEntity e WHERE e.name = 'Building Choices'")
  fun getBuildingChoicesTemplate(): AccreditedProgrammeTemplateEntity
}
