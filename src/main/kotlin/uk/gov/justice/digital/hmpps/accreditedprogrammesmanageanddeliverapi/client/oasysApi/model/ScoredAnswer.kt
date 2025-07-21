package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model

import kotlin.collections.firstOrNull
import kotlin.text.equals
import kotlin.text.firstOrNull

interface ScoredAnswer {
  val score: Int?

  enum class YesNo(override val score: Int?) : ScoredAnswer {
    YES(2),
    NO(0),
    Unknown(null),
    ;

    companion object {
      fun of(value: String?): YesNo = entries.firstOrNull { it.name.equals(value, true) } ?: Unknown
    }
  }

  enum class Problem(override val score: Int?) : ScoredAnswer {
    NONE(0),
    SOME(1),
    SIGNIFICANT(2),
    MISSING(null),
    ;

    companion object {
      fun of(value: String?): Problem = when (value?.firstOrNull()) {
        '0' -> NONE
        '1' -> SOME
        '2' -> SIGNIFICANT
        else -> MISSING
      }
    }
  }
}
