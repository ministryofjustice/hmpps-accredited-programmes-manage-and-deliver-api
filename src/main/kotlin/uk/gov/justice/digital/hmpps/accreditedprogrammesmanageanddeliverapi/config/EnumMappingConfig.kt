package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import org.springframework.boot.convert.ApplicationConversionService
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

// This class converts our ENUMS in web endpoints to upper case to avoid any issues around casing.
@Configuration
class EnumMappingConfig : WebMvcConfigurer {
  override fun addFormatters(registry: FormatterRegistry) {
    ApplicationConversionService.configure(registry)
  }
}
