package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusHistory
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "referral_status_history")
class ReferralStatusHistoryEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id")
  val referral: ReferralEntity,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_status_description_id")
  val referralStatusDescription: ReferralStatusDescriptionEntity,

  @Column(name = "additional_details")
  var additionalDetails: String? = null,

  @Column(name = "created_at")
  @CreatedDate
  var createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "created_by")
  @CreatedBy
  var createdBy: String,

  @Column(name = "start_date")
  var startDate: LocalDateTime? = null,
)

fun ReferralStatusHistoryEntity.toApi(): ReferralStatusHistory = ReferralStatusHistory(
  id = id!!,
  referralStatusDescriptionId = referralStatusDescription.id,
  referralStatusDescriptionName = referralStatusDescription.description,
)
