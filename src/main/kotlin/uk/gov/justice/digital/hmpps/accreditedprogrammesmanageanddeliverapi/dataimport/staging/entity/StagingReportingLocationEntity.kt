package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull

/**
 * Staging entity for Reporting Location (for a Referral) data from
 * Interventions Manager.
 * Resides in the im_data_import schema and is linked to a staging referral.
 */
@Entity
@Table(name = "reporting_location", schema = "im_data_import")
class StagingReportingLocationEntity(
  @Id
  @Column(name = "source_referral_id")
  var sourceReferralId: String,

  @NotNull
  @Column(name = "region_name")
  var regionName: String,

  @NotNull
  @Column(name = "pdu_name")
  var pduName: String,

  @NotNull
  @Column(name = "reporting_team_name")
  var reportingTeamName: String,

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_referral_id", insertable = false, updatable = false)
  var referral: StagingReferralEntity? = null,
)
