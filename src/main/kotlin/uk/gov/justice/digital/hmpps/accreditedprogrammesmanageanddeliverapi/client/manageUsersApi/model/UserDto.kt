package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.manageUsersApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserDto(
  val username: String,
  val active: Boolean,
  val name: String,
)
