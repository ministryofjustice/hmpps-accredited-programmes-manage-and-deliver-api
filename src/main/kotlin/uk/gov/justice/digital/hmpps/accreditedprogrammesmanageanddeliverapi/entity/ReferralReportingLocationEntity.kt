package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.ColumnDefault
import java.util.UUID

@Entity
@Table(name = "referral_reporting_location")
open class ReferralReportingLocationEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  open var id: UUID? = null,

  @NotNull
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id")
  open var referral: ReferralEntity,

  @NotNull
  @ColumnDefault("UNKNOWN_REGION_NAME")
  @Column(name = "region_name")
  open var regionName: String,

  @NotNull
  @ColumnDefault("UNKNOWN_PDU_NAME")
  @Column(name = "pdu_name")
  open var pduName: String,

  @NotNull
  @ColumnDefault("UNKNOWN_REPORTING_TEAM")
  @Column(name = "reporting_team")
  open var reportingTeam: String,
)
