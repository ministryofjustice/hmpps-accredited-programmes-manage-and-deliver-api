package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableScheduling
class AccreditedProgrammesManageAndDeliverApi

fun main(args: Array<String>) {
  runApplication<AccreditedProgrammesManageAndDeliverApi>(*args)
}
