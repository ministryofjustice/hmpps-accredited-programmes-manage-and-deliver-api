package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SQSMessageHistoryEntity
import java.util.UUID

interface SQSMessageHistoryRepository : JpaRepository<SQSMessageHistoryEntity, UUID>
