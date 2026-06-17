package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfiguration {

  @Bean
  fun cacheManager(): CacheManager {
    val cacheManager = CaffeineCacheManager()
    cacheManager.registerCustomCache(
      "user-regions",
      Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .maximumSize(500)
        .build(),
    )
    cacheManager.registerCustomCache(
      "bank-holidays",
      Caffeine.newBuilder()
        .expireAfterWrite(24, TimeUnit.HOURS)
        .maximumSize(1)
        .build(),
    )
    cacheManager.registerCustomCache(
      "pni-daily",
      Caffeine.newBuilder()
        .expireAfterWrite(24, TimeUnit.HOURS)
        .maximumSize(20000)
        .build(),
    )
    return cacheManager
  }
}
