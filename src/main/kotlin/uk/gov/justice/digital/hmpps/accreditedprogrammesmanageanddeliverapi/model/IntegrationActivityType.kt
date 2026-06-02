package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model

enum class IntegrationActivityType(val eventName: String) {
  UPDATE_REFERRAL_STATUS_N_DELIUS("Referral.status-update-nDelius"),
  REFERRAL_COMPLETED_N_DELIUS("Referral.completed-nDelius"),
  GET_PERSONAL_DETAILS_N_DELIUS("PersonalDetails.get-nDelius"),
  GET_SENTENCE_DETAILS_N_DELIUS("SentenceDetails.get-nDelius"),
  GET_LIMITED_ACCESS_OFFENDER_N_DELIUS("LimitedAccessOffender.get-nDelius"),
  GET_OFFENCE_N_DELIUS("Offence.get-nDelius"),
  GET_REGISTRATION_N_DELIUS("Registration.get-nDelius"),
  GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS("RequirementManagerDetails.get-nDelius"),
  GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS("LicenceConditionManagerDetails.get-nDelius"),
  GET_USER_TEAM_N_DELIUS("UserTeam.get-nDelius"),
  GET_REGION_PDU_N_DELIUS("Pdu.get-for-region-nDelius"),
  GET_PDU_OFFICE_LOCATION_N_DELIUS("OfficeLocation.get-for-PDU-nDelius"),
  CREATE_APPOINTMENT_N_DELIUS("Appointment.create-nDelius"),
  DELETE_APPOINTMENT_N_DELIUS("Appointment.delete-nDelius"),
  UPDATE_APPOINTMENT_N_DELIUS("Appointment.update-nDelius"),
}
