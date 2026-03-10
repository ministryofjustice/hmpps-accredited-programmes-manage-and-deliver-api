package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

@Entity
@Table(name = "referral", schema = "im_data_import")
class ImReferralEntity(
  @Id
  @Column(name = "source_referral_id")
  var sourceReferralId: String,

  @NotNull
  @Column(name = "crn", nullable = false)
  var crn: String,

  @NotNull
  @Column(name = "first_name", nullable = false)
  var firstName: String,

  @NotNull
  @Column(name = "last_name", nullable = false)
  var lastName: String,

  @NotNull
  @Column(name = "created_at", nullable = false)
  var createdAt: LocalDate,

  @NotNull
  @Column(name = "sourced_from", nullable = false)
  var sourcedFrom: String,

  @NotNull
  @Column(name = "sourced_from_id", nullable = false)
  var sourcedFromId: String,

  @NotNull
  @Column(name = "sex", nullable = false)
  var sex: String,

  @NotNull
  @Column(name = "date_of_birth", nullable = false)
  var dateOfBirth: LocalDate,
)
