package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import java.time.LocalDateTime

data class LicenceCondition(
  val id: Long,
  val mainCategory: CodedValue,
  val subCategory: CodedValue?,
  val manager: Manager,
  val probationDeliveryUnits: List<PduOfficeLocations>,
  val eventNumber: String,
  val createdAt: LocalDateTime,
)

data class LicenceConditions(
  val content: List<LicenceCondition>,
)
