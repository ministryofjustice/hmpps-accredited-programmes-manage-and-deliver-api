package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AccreditedProgrammeTemplateEntity
import java.util.UUID

interface AccreditedProgrammeTemplateRepository : JpaRepository<AccreditedProgrammeTemplateEntity, UUID> {
  fun findFirstByName(name: String): AccreditedProgrammeTemplateEntity?
}
