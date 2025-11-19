package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

data class CreateGroupSessionSlot(val dayOfWeek: String, val hour: Int, val minutes: Int, val amOrPm: String)
