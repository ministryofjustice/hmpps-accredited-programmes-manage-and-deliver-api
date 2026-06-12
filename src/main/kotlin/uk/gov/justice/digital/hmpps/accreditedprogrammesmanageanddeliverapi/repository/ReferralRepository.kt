package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

interface ReferralRepository : JpaRepository<ReferralEntity, UUID> {
  @Query("SELECT r FROM ReferralEntity r LEFT JOIN FETCH r.programmeGroupMemberships WHERE r.id = :id")
  fun findByIdWithMemberships(id: UUID): ReferralEntity?

  @Query("SELECT id from ReferralEntity")
  fun getAllIds(): List<UUID>

  @EntityGraph(attributePaths = ["statusHistories"])
  override fun findById(id: UUID): Optional<ReferralEntity>

  @EntityGraph(attributePaths = ["statusHistories"])
  fun findByCrn(crn: String): List<ReferralEntity>

  fun findByCrnAndEventIdAndSourcedFrom(
    crn: String,
    eventId: String,
    sourcedFrom: ReferralEntitySourcedFrom,
  ): ReferralEntity?

  fun findAllByCreatedAtBefore(createdAtBefore: LocalDateTime): MutableList<ReferralEntity>

  @Query(
    value = """
      SELECT DISTINCT r.id AS referralId,
             r.event_id AS licReqNo,
             r.crn AS crn,
             COALESCE(rrl.region_name, 'UNKNOWN_REGION_NAME') AS regionName
      FROM referral r
      LEFT JOIN referral_reporting_location rrl ON r.id = rrl.referral_id
      LEFT JOIN referral_status_history rsh ON r.id = rsh.referral_id
      LEFT JOIN referral_status_description rsd ON rsd.id = rsh.referral_status_description_id
      WHERE r.intervention_name = 'Building Choices'
        AND (
          (CAST(:referralsCreatedSince AS DATE) IS NOT NULL AND r.created_at::date > CAST(:referralsCreatedSince AS DATE))
          OR
          (CAST(:referralsCompletedAfter AS DATE) IS NOT NULL AND rsd.description_text = 'Programme complete' AND rsh.created_at::date > CAST(:referralsCompletedAfter AS DATE))
        )
      """,
    nativeQuery = true,
  )
  fun getDosageReportReferrals(
    referralsCreatedSince: LocalDate?,
    referralsCompletedAfter: LocalDate?,
  ): List<DosageReportReferralProjection>
}

interface DosageReportReferralProjection {
  val referralId: UUID
  val licReqNo: String?
  val crn: String
  val regionName: String
}
