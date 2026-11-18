package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.NDeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.util.UUID

interface NDeliusAppointmentRepository : JpaRepository<NDeliusAppointmentEntity, UUID> {
  fun findBySessionId(sessionId: UUID): List<NDeliusAppointmentEntity>
  fun deleteByReferral(referral: ReferralEntity)

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  fun findByNdeliusAppointmentId(nDeliusAppointmentId: UUID): List<NDeliusAppointmentEntity>
}
