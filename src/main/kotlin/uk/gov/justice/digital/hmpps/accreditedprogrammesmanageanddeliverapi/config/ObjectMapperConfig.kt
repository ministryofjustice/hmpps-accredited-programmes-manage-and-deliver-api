package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ObjectMapperConfig {

  @Bean
  fun objectMapper(): ObjectMapper = jacksonObjectMapper().apply {
    registerModule(JavaTimeModule())
  }
}
