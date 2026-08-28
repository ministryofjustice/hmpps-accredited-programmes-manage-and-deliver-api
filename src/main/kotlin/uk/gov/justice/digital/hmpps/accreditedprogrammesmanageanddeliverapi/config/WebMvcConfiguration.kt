package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.interceptor.LimitedAccessOffenderAuthorisationInterceptor

@Configuration
@EnableWebMvc
class WebMvcConfiguration(
  private val limitedAccessOffenderAuthorisationInterceptor: LimitedAccessOffenderAuthorisationInterceptor,
) : WebMvcConfigurer {

  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(limitedAccessOffenderAuthorisationInterceptor)
      .addPathPatterns("/referral-details/**") // Apply to all referral details API paths
  }
}
