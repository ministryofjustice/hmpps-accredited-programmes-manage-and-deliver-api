package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Transient
import java.util.UUID

@Entity
@Table(name = "attendee")
class AttendeeEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id")
  var referral: ReferralEntity,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id")
  var session: SessionEntity,
) {
  @get:Transient
  val personName: String
    get() = referral.personName
}
