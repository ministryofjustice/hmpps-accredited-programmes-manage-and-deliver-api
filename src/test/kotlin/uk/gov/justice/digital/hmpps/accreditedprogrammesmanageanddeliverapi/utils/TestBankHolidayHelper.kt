package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import java.time.LocalDate

class TestBankHolidayHelper {

  fun getBankHolidayDates(): Set<LocalDate> = setOf(
    LocalDate.of(2026, 8, 31),
    LocalDate.of(2026, 12, 25),
    LocalDate.of(2026, 12, 28),
    LocalDate.of(2027, 1, 1),
    LocalDate.of(2027, 3, 26),
    LocalDate.of(2027, 3, 29),
    LocalDate.of(2027, 5, 3),
    LocalDate.of(2027, 5, 31),
    LocalDate.of(2027, 8, 30),
    LocalDate.of(2027, 12, 27),
    LocalDate.of(2027, 12, 28),
    LocalDate.of(2028, 1, 3),
    LocalDate.of(2028, 4, 14),
    LocalDate.of(2028, 4, 17),
    LocalDate.of(2028, 5, 1),
    LocalDate.of(2028, 5, 29),
    LocalDate.of(2028, 8, 28),
    LocalDate.of(2028, 12, 25),
    LocalDate.of(2028, 12, 26),
  )
}
