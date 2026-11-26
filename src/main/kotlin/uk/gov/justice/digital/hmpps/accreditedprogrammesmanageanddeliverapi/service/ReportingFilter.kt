package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import java.time.LocalDate

/**
 * Selects what a report covers, in [regionName] or, when it is null, nationally. [startDate] and [endDate] are inclusive.
 * - Group size and dosage cover the groups that started between the dates, whenever their sessions were held.
 *   A group starts with its first Getting started 1 session.
 * - Session rate and facilitator continuity cover the sessions held between the dates, whenever the group started.
 */
data class ReportingFilter(
  val startDate: LocalDate,
  val endDate: LocalDate,
  val regionName: String? = null,
)

object ReportingRegions {
  /**
   * The 12 probation regions, as named in nDelius.
   * The reporting URL generator in the UI repo offers the same list, so keep the two in step.
   */
  val REGION_NAMES = listOf(
    "East Midlands Region",
    "East of England",
    "Greater Manchester",
    "Kent Surrey Sussex Region",
    "London",
    "North East Region",
    "North West Region",
    "South Central",
    "South West",
    "Wales",
    "West Midlands Region",
    "Yorkshire and The Humber",
  )

  /** Regions that only hold test or retired groups, so are left out of national reports. */
  val EXCLUDED_FROM_NATIONAL = listOf(
    "PRIVATE BETA REGION",
    "REGION FOR DELETION",
    "REGION_FOR_DELETION",
  )
}
