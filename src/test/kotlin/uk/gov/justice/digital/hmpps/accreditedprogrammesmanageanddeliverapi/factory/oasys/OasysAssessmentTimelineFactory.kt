package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.oasys

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.risksAndNeeds.Timeline
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomCrn
import java.time.LocalDateTime
import kotlin.random.Random

class OasysAssessmentTimelineFactory {
  private var crn: String? = randomCrn()
  private var nomsId: String? = null
  private var timeline: List<Timeline> = listOf(TimelineFactory().produce())

  fun withCrn(crn: String?) = apply { this.crn = crn }
  fun withNomisId(nomsId: String?) = apply { this.nomsId = nomsId }
  fun withTimeline(timeline: List<Timeline>) = apply { this.timeline = timeline }

  fun produce() = OasysAssessmentTimeline(
    crn = this.crn,
    nomsId = this.nomsId,
    timeline = this.timeline,
  )
}

class TimelineFactory {
  private var id: Long = Random.nextLong(1, 100000)
  private var status: String = "COMPLETE"
  private var type: String = "LAYER3"
  private var completedAt: LocalDateTime? =
    if (Random.nextBoolean()) LocalDateTime.now().minusDays(Random.nextLong(1, 365)) else null

  fun withId(id: Long) = apply { this.id = id }
  fun withStatus(status: String) = apply { this.status = status }
  fun withType(type: String) = apply { this.type = type }
  fun withCompletedAt(completedAt: LocalDateTime?) = apply { this.completedAt = completedAt }

  fun produce() = Timeline(
    id = this.id,
    status = this.status,
    type = this.type,
    completedAt = this.completedAt,
  )
}
