package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event

enum class HmppsDomainEventTypes(val value: String) {
  ACP_COMMUNITY_PROGRAMME_COMPLETE("accredited-programmes-community.programme.complete"),
  ACP_COMMUNITY_REFERRAL_STATUS_UPDATED("accredited-programmes-community.referral.status-updated"),
  INTERVENTIONS_COMMUNITY_REFERRAL_CREATED("interventions.community-referral.created"),
  INTERVENTIONS_COMMUNITY_REFERRAL_IMPORTED("interventions.community-referral.imported"),
}
