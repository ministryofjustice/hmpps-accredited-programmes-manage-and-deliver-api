package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.ColumnDefault
import java.util.UUID

@Entity
@Table(name = "referral_status_history_mapping")
class ReferralStatusHistoryMappingEntity(
  @Id
  @ColumnDefault("gen_random_uuid()")
  @Column(name = "id") var id: UUID,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id") var referral: ReferralEntity,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
    name = "referral_status_history_id",
  ) var referralStatusHistory: ReferralStatusHistoryEntity,
)
