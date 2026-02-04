package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Transient
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "session")
@EntityListeners(AuditingEntityListener::class)
class SessionEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "programme_group_id")
  var programmeGroup: ProgrammeGroupEntity,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "module_session_template_id")
  var moduleSessionTemplate: ModuleSessionTemplateEntity,

  @NotNull
  @Column(name = "is_catchup")
  var isCatchup: Boolean = false,

  @Column(name = "location_name")
  var locationName: String? = null,

  @NotNull
  @Column(name = "starts_at")
  var startsAt: LocalDateTime,

  @NotNull
  @Column(name = "ends_at")
  var endsAt: LocalDateTime,

  @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "id.session")
  var sessionFacilitators: MutableSet<SessionFacilitatorEntity> = mutableSetOf(),

  @NotNull
  @Column(name = "created_at")
  @CreatedDate
  var createdAt: LocalDateTime? = LocalDateTime.now(),

  @Column(name = "created_by_username")
  @CreatedBy
  var createdByUsername: String? = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",

  @OneToMany(
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    mappedBy = "session",
  )
  var attendances: MutableSet<SessionAttendanceEntity> = mutableSetOf(),

  @OneToMany(
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
    mappedBy = "session",
  )
  var attendees: MutableList<AttendeeEntity> = mutableListOf(),

  @NotNull
  @Column(name = "is_placeholder")
  var isPlaceholder: Boolean,

  @NotNull
  @OneToMany(mappedBy = "session", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
  var ndeliusAppointments: MutableSet<NDeliusAppointmentEntity> = mutableSetOf(),

) : Comparable<SessionEntity> {
  // Compute these values rather than have them duplicated in the db tables
  @get:Transient
  val moduleNumber: Int
    get() = moduleSessionTemplate.module.moduleNumber

  @get:Transient
  val moduleName: String
    get() = moduleSessionTemplate.module.name

  @get:Transient
  val sessionNumber: Int
    get() = moduleSessionTemplate.sessionNumber

  @get:Transient
  val sessionName: String
    get() = moduleSessionTemplate.name

  @get:Transient
  val sessionType: SessionType
    get() = moduleSessionTemplate.sessionType

  override fun compareTo(other: SessionEntity): Int = compareValuesBy(
    this,
    other,
    { it.startsAt },
    { it.endsAt },
    { it.id },
  )
}
