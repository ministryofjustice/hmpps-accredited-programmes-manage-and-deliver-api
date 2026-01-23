package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.NDeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import java.util.UUID

class NDeliusAppointmentEntityFactory {
  private var id: UUID? = null
  private var ndeliusAppointmentId: UUID = UUID.randomUUID()
  private var session: SessionEntity? = null
  private var referral: ReferralEntity? = null

  fun withId(id: UUID?) = apply { this.id = id }
  fun withNdeliusAppointmentId(ndeliusAppointmentId: UUID) = apply { this.ndeliusAppointmentId = ndeliusAppointmentId }
  fun withSession(session: SessionEntity) = apply { this.session = session }
  fun withReferral(referral: ReferralEntity) = apply { this.referral = referral }

  fun produce() = NDeliusAppointmentEntity(
    id = this.id,
    ndeliusAppointmentId = this.ndeliusAppointmentId,
    session = this.session ?: SessionFactory().produce(),
    referral = this.referral ?: ReferralEntityFactory().produce(),
  )
}
