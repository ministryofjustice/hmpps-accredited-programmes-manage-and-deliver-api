package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class AccreditedProgrammesManageAndDeliverApi

fun main(args: Array<String>) {
  runApplication<AccreditedProgrammesManageAndDeliverApi>(*args)
}
