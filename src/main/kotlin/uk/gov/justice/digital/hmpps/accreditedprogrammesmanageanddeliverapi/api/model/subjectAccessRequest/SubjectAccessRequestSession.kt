package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import java.time.LocalDateTime
import java.util.UUID

data class SubjectAccessRequestSession(
  val createdByUsername: String?,
  val endsAt: LocalDateTime,
  val locationName: String?,
  val startsAt: LocalDateTime,
  val module: SubjectAccessRequestModule,
  val moduleSessionTemplate: SubjectAccessRequestModuleSessionTemplate,
  val sessionFacilitators: List<SubjectAccessRequestSessionFacilitator>,
)

data class SubjectAccessRequestModule(
  val name: String,
)

data class SubjectAccessRequestModuleSessionTemplate(
  val description: String?,
  val durationMinutes: Int,
  val moduleId: UUID,
  val name: String,
  val pathway: String,
  val sessionNumber: Int,
  val sessionType: String,
)

fun SessionEntity.toApi() = SubjectAccessRequestSession(
  createdByUsername = createdByUsername,
  endsAt = endsAt,
  locationName = locationName,
  startsAt = startsAt,
  module = SubjectAccessRequestModule(
    name = moduleSessionTemplate.module.name,
  ),
  moduleSessionTemplate = SubjectAccessRequestModuleSessionTemplate(
    description = moduleSessionTemplate.description,
    durationMinutes = moduleSessionTemplate.durationMinutes,
    moduleId = moduleSessionTemplate.module.id!!,
    name = moduleSessionTemplate.name,
    pathway = moduleSessionTemplate.pathway.name,
    sessionNumber = moduleSessionTemplate.sessionNumber,
    sessionType = moduleSessionTemplate.sessionType.name,
  ),
  sessionFacilitators = sessionFacilitators.map { it.toApi() },
)
