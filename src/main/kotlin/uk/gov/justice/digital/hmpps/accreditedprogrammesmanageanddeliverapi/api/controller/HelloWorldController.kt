package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWorldController {

  @PreAuthorize("permitAll()")
  @GetMapping("/hello-world", produces = ["text/plain"])
  fun helloWorld() = "Hello World"
}
