package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import java.time.LocalDateTime
import java.util.UUID

class SessionFactory {
  private var id: UUID? = null
  private var programmeGroup: ProgrammeGroupEntity? = null
  private var moduleSessionTemplate: ModuleSessionTemplateEntity? = null
  private var isCatchup: Boolean = false
  private var locationName: String? = null
  private var startsAt: LocalDateTime = LocalDateTime.now()
  private var endsAt: LocalDateTime = LocalDateTime.now().plusHours(1)
  private var sessionFacilitators: MutableSet<FacilitatorEntity> = mutableSetOf()
  private var createdAt: LocalDateTime? = LocalDateTime.now()
  private var createdByUsername: String? = "UNKNOWN_USER"
  private var attendances: MutableSet<SessionAttendanceEntity> = mutableSetOf()
  private var attendees: MutableList<AttendeeEntity> = mutableListOf()

  fun withId(id: UUID?) = apply { this.id = id }
  fun withProgrammeGroup(programmeGroup: ProgrammeGroupEntity) = apply { this.programmeGroup = programmeGroup }
  fun withModuleSessionTemplate(moduleSessionTemplate: ModuleSessionTemplateEntity) = apply { this.moduleSessionTemplate = moduleSessionTemplate }
  fun withIsCatchup(isCatchup: Boolean) = apply { this.isCatchup = isCatchup }
  fun withLocationName(locationName: String?) = apply { this.locationName = locationName }
  fun withStartsAt(startsAt: LocalDateTime) = apply { this.startsAt = startsAt }
  fun withEndsAt(endsAt: LocalDateTime) = apply { this.endsAt = endsAt }
  fun withSessionFacilitators(sessionFacilitators: MutableSet<FacilitatorEntity>) = apply { this.sessionFacilitators = sessionFacilitators }
  fun withCreatedAt(createdAt: LocalDateTime?) = apply { this.createdAt = createdAt }
  fun withCreatedByUsername(createdByUsername: String?) = apply { this.createdByUsername = createdByUsername }
  fun withAttendances(attendances: MutableSet<SessionAttendanceEntity>) = apply { this.attendances = attendances }
  fun withAttendees(attendees: MutableList<AttendeeEntity>) = apply { this.attendees = attendees }

  fun produce() = SessionEntity(
    id = this.id,
    programmeGroup = this.programmeGroup!!,
    moduleSessionTemplate = this.moduleSessionTemplate!!,
    isCatchup = this.isCatchup,
    locationName = this.locationName,
    startsAt = this.startsAt,
    endsAt = this.endsAt,
    sessionFacilitators = this.sessionFacilitators,
    createdAt = this.createdAt,
    createdByUsername = this.createdByUsername,
    attendances = this.attendances,
    attendees = this.attendees,
  )
}
