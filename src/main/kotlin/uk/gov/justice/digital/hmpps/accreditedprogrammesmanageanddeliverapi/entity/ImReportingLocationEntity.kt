package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "reporting_location", schema = "im_data_import")
class ImReportingLocationEntity(
  @Id
  @Column(name = "source_referral_id")
  var sourceReferralId: String,

  @NotNull
  @Column(name = "region_name", nullable = false)
  var regionName: String,

  @NotNull
  @Column(name = "pdu_name", nullable = false)
  var pduName: String,

  @NotNull
  @Column(name = "reporting_team_name", nullable = false)
  var reportingTeamName: String,
)
