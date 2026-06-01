package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "bank_holiday")
class BankHolidayEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @Column(name = "title")
  var title: String,

  @NotNull
  @Column(name = "holiday_date")
  var holidayDate: LocalDate,

  @Column(name = "notes")
  var notes: String? = null,

  @NotNull
  @Column(name = "bunting")
  var bunting: Boolean = false,
)
