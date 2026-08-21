package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type

enum class InterventionType(val displayName: String) {
  SI("Structured Intervention"),
  ACP("Accredited Programme"),
  CRS("Commissioned Rehabilitative Service"),
  TOOLKITS("Toolkits"),
}
