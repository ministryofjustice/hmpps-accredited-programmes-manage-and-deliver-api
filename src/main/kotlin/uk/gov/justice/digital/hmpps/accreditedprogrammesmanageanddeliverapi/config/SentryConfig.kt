package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import io.sentry.SentryOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.dao.DataIntegrityViolationException

@Configuration
class SentryConfig {

  @Bean
  fun ignoreHealthRequests() = SentryOptions.BeforeSendTransactionCallback { transaction, _ ->
    transaction.transaction?.let { if (it.startsWith("GET /health") || it.startsWith("GET /info")) null else transaction }
  }

  // The group waitlist view can transiently return duplicate rows; fingerprint these events so Sentry
  // groups them as a single known issue instead of one issue per stack trace.
  @Bean
  fun groupWaitlistDuplicateFingerprint() = SentryOptions.BeforeSendCallback { event, _ ->
    val message = event.throwable?.message.orEmpty()
    if (event.throwable is DataIntegrityViolationException &&
      message.contains(GROUP_WAITLIST_VIEW) &&
      message.contains(DUPLICATE_ROWS_ERROR)
    ) {
      event.fingerprints = listOf(GROUP_WAITLIST_DUPLICATE_FINGERPRINT)
      event.setTag("error.type", GROUP_WAITLIST_DUPLICATE_FINGERPRINT)
    }
    event
  }

  private companion object {
    private const val GROUP_WAITLIST_VIEW = "group_waitlist_item_view"
    private const val DUPLICATE_ROWS_ERROR = "duplicate rows"
    private const val GROUP_WAITLIST_DUPLICATE_FINGERPRINT = "group-waitlist-view-duplicate-rows"
  }
}
