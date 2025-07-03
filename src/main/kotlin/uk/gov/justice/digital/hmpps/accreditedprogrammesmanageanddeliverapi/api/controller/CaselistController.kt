package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CaselistController {
  @PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
  @GetMapping("/pages/caselist/open", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getOpenCaselistReferrals() = mapOf(
    "referrals" to listOf(
      mapOf(
        "id" to "abc-987",
        "personName" to "Bob Buddy",
        "personCrn" to "X456",
        "status" to "Awaiting Assessment",
      ),
      mapOf(
        "id" to "abc-654",
        "personName" to "Dave David",
        "personCrn" to "X123",
        "status" to "Another Status",
      ),
      mapOf(
        "id" to "abc-312",
        "personName" to "Frank Ferdinand",
        "personCrn" to "X987",
        "status" to "Another Status",
      ),
    ),
  )

  @PreAuthorize("hasAnyRole('ROLE_ACCREDITED_PROGRAMMES_MANAGE_AND_DELIVER_API__ACPMAD_UI_WR')")
  @GetMapping("/pages/caselist/closed", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getClosedCaselistReferrals() = mapOf(
    "referrals" to listOf(
      mapOf(
        "id" to "abc-987",
        "personName" to "Tony Trellis",
        "personCrn" to "X456",
        "status" to "Awaiting Assessment",
      ),
      mapOf(
        "id" to "abc-654",
        "personName" to "Steve Stevio",
        "personCrn" to "X123",
        "status" to "Another Status",
      ),
      mapOf(
        "id" to "abc-312",
        "personName" to "Bruce Benjamin",
        "personCrn" to "X987",
        "status" to "Another Status",
      ),
    ),
  )
}
