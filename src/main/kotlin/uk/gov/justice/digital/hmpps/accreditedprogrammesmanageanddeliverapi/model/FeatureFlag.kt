package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model

/**
 * Flags in our accredited-programmes-community namespace in https://github.com/ministryofjustice/hmpps-feature-flags
 */
enum class FeatureFlag(val key: String) {
  REFERRAL_LOCATION_USE_REQUIREMENT_MANAGER("referral-location-use-requirement-manager"),
}
