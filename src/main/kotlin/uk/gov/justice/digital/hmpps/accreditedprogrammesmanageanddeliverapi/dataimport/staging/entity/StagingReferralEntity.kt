package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import java.time.LocalDate

/**
 * This entity represents some transformed Referral from IM.
 * IM's Referral table has ~120 columns, and therefore we don't want to
 * map it directly.
 */
@Entity
@Table(name = "referral", schema = "im_data_import")
class StagingReferralEntity(
  @Id
  @Column(name = "source_referral_id")
  var sourceReferralId: String,

  @NotNull
  @Column(name = "crn")
  var crn: String,

  @NotNull
  @Column(name = "first_name")
  var firstName: String,

  @NotNull
  @Column(name = "last_name")
  var lastName: String,

  @NotNull
  @Column(name = "created_at")
  var createdAt: LocalDate,

  @NotNull
  @Column(name = "sourced_from")
  @Enumerated(EnumType.STRING)
  var sourcedFrom: ReferralEntitySourcedFrom? = null,

  @NotNull
  @Column(name = "sourced_from_id")
  var sourcedFromId: String,

  @NotNull
  @Column(name = "sex")
  var sex: String,

  @NotNull
  @Column(name = "date_of_birth")
  var dateOfBirth: LocalDate,

  @OneToOne(mappedBy = "referral", fetch = FetchType.LAZY)
  var reportingLocation: StagingReportingLocationEntity? = null,

  @OneToMany(mappedBy = "referral", fetch = FetchType.LAZY)
  var iapsReqLicNos: MutableList<StagingIapsLicreqnosEntity> = mutableListOf(),
)
