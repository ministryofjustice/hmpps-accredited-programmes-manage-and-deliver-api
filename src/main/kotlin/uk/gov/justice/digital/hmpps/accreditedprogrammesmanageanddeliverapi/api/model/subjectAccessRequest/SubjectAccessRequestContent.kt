package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

data class SubjectAccessRequestContent(
  val referrals: List<SubjectAccessRequestReferral>,
  val groupWaitlistItemViews: List<SubjectAccessRequestGroupWaitlistItemView>,
  val referralCaseListItemViews: List<SubjectAccessRequestReferralCaseListItemView>,
)
