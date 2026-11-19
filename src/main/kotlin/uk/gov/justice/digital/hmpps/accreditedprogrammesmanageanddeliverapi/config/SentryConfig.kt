package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import io.netty.handler.timeout.ReadTimeoutException
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.NestedExceptionUtils
import org.springframework.dao.DataIntegrityViolationException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.AppointmentUpdateException

@Configuration
class SentryConfig {

  @Bean
  fun ignoreHealthRequests() = SentryOptions.BeforeSendTransactionCallback { transaction, _ ->
    transaction.transaction?.let { if (it.startsWith("GET /health") || it.startsWith("GET /info")) null else transaction }
  }

  @Bean
  fun beforeSend() = SentryOptions.BeforeSendCallback { event, _ ->
    val throwable = event.throwable
    val message = throwable?.message.orEmpty()

    when (throwable) {
      is DataIntegrityViolationException if message.contains(GROUP_WAITLIST_VIEW) &&
        message.contains(DUPLICATE_ROWS_ERROR) -> {
        event.fingerprints = listOf(GROUP_WAITLIST_DUPLICATE_FINGERPRINT)
        event.setTag("error.type", GROUP_WAITLIST_DUPLICATE_FINGERPRINT)
      }

      is AppointmentUpdateException -> {
        event.level = SentryLevel.WARNING
        event.fingerprints = listOf(APPOINTMENT_UPDATE_FAILURE_FINGERPRINT)
        event.setTag("error.type", APPOINTMENT_UPDATE_FAILURE_FINGERPRINT)
      }

      else if NestedExceptionUtils.getRootCause(throwable) is ReadTimeoutException -> {
        event.level = SentryLevel.WARNING
        event.fingerprints = listOf(UPSTREAM_READ_TIMEOUT_FINGERPRINT)
        event.setTag("error.type", UPSTREAM_READ_TIMEOUT_FINGERPRINT)
      }
    }
    event
  }

  private companion object {
    private const val GROUP_WAITLIST_VIEW = "group_waitlist_item_view"
    private const val DUPLICATE_ROWS_ERROR = "duplicate rows"
    private const val GROUP_WAITLIST_DUPLICATE_FINGERPRINT = "group-waitlist-view-duplicate-rows"
    private const val APPOINTMENT_UPDATE_FAILURE_FINGERPRINT = "ndelius-appointment-update-failure"
    private const val UPSTREAM_READ_TIMEOUT_FINGERPRINT = "upstream-api-read-timeout"
  }
}
