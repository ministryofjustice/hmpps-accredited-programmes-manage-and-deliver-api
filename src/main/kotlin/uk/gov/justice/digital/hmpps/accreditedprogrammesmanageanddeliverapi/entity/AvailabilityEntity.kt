package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode.SUBSELECT
import org.springframework.data.annotation.LastModifiedBy
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "availability")
class AvailabilityEntity(

  @Id
  @GeneratedValue
  var id: UUID? = null,

  @Column(name = "referral_id", nullable = false)
  var referralId: UUID,

  @Column(name = "start_date")
  var startDate: LocalDate,

  @Column(name = "end_date")
  var endDate: LocalDate? = null,

  @Column(name = "other_details")
  var otherDetails: String? = null,

  /**
   * lastModifiedBy could be UNKNOWN, if we are not able to get the username from the bearer token
   */
  @Column(name = "last_modified_by", nullable = false)
  @LastModifiedBy
  var lastModifiedBy: String,

  @Column(name = "last_modified_at", nullable = false)
  var lastModifiedAt: LocalDateTime = LocalDateTime.now(),

  @OneToMany(mappedBy = "availability", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
  @Fetch(SUBSELECT)
  var slots: MutableSet<SlotEntity> = mutableSetOf(),
)
