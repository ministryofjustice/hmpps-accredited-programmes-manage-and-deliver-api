package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import org.springframework.boot.convert.ApplicationConversionService
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class EnumMappingConfig : WebMvcConfigurer {
  override fun addFormatters(registry: FormatterRegistry) {
    ApplicationConversionService.configure(registry)
  }
}
