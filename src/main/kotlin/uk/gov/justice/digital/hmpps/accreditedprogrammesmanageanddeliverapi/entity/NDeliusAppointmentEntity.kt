package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.jetbrains.annotations.NotNull
import java.util.UUID

@Entity
@Table(name = "ndelius_appointment")
class NDeliusAppointmentEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @Column("ndelius_appointment_id")
  var ndeliusAppointmentId: UUID,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id")
  var session: SessionEntity,

  @NotNull
  @JoinColumn(name = "referral_id")
  @ManyToOne(fetch = FetchType.LAZY)
  var referral: ReferralEntity,
)

fun AttendeeEntity.toNdeliusAppointmentEntity(ndeliusAppointmentId: UUID) = NDeliusAppointmentEntity(
  ndeliusAppointmentId = ndeliusAppointmentId,
  session = session,
  referral = referral,
)
