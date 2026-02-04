package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Transient
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.FacilitatorType
import java.io.Serializable

@Entity
@Table(name = "session_facilitator")
class SessionFacilitatorEntity(
  @EmbeddedId
  var id: SessionFacilitatorId,

  @NotNull
  @Column("facilitator_type")
  @Enumerated(EnumType.STRING)
  var facilitatorType: FacilitatorType,
) {
  constructor(
    facilitator: FacilitatorEntity,
    session: SessionEntity,
    facilitatorType: FacilitatorType,
  ) : this(
    id = SessionFacilitatorId(facilitator = facilitator, session = session),
    facilitatorType = facilitatorType,
  )

  val facilitator: FacilitatorEntity
    get() = id.facilitator

  val session: SessionEntity
    get() = id.session

  @get:Transient
  val facilitatorName: String
    get() = facilitator.personName

  @get:Transient
  val facilitatorCode: String
    get() = facilitator.ndeliusPersonCode

  @get:Transient
  val teamName: String
    get() = facilitator.ndeliusTeamName

  @get:Transient
  val teamCode: String
    get() = facilitator.ndeliusTeamCode
}

@Embeddable
data class SessionFacilitatorId(
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facilitator_id")
  var facilitator: FacilitatorEntity,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id")
  var session: SessionEntity,
) : Serializable
