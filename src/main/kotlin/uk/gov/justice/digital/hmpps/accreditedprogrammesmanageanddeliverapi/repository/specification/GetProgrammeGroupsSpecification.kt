package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import jakarta.persistence.criteria.Subquery
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageByRegionTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.ProgrammeGroupSexEnum
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceNDeliusOutcomeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.jvm.java

fun hasAtLeastOneActiveMembership(
  query: CriteriaQuery<*>,
  cb: CriteriaBuilder,
  root: Root<ProgrammeGroupEntity>,
): Predicate {
  val membershipExistsSubquery = query.subquery(Long::class.java)
  val membershipRoot = membershipExistsSubquery.from(ProgrammeGroupMembershipEntity::class.java)
  membershipExistsSubquery.select(cb.literal(1L))
  membershipExistsSubquery.where(
    cb.equal(membershipRoot.get<ProgrammeGroupEntity>("programmeGroup"), root),
  )
  return cb.exists(membershipExistsSubquery)
}

fun allSessionsHaveAttendanceData(
  parentSubquery: Subquery<Long>,
  cb: CriteriaBuilder,
  groupMembershipRoot: Root<ProgrammeGroupMembershipEntity>,
): Predicate {
  val groupRoot = groupMembershipRoot.get<ProgrammeGroupEntity>("programmeGroup")

  // Subquery to count relevant sessions for the group (that are NOT catch-up and NOT placeholders)
  val sessionCountSubquery = parentSubquery.subquery(Long::class.java)
  val sessionRoot = sessionCountSubquery.from(SessionEntity::class.java)
  sessionCountSubquery.select(cb.count(sessionRoot))
  sessionCountSubquery.where(
    cb.equal(sessionRoot.get<ProgrammeGroupEntity>("programmeGroup"), groupRoot),
    cb.isFalse(sessionRoot.get("isPlaceholder")),
    cb.isFalse(sessionRoot.get("isCatchup")),
  )

  // Subquery to count attendance records for this membership for those same sessions
  val attendanceCountSubquery = parentSubquery.subquery(Long::class.java)
  val attendanceRoot = attendanceCountSubquery.from(SessionAttendanceEntity::class.java)
  val attendanceSessionJoin = attendanceRoot.join<SessionAttendanceEntity, SessionEntity>("session")
  attendanceCountSubquery.select(cb.count(attendanceRoot))
  attendanceCountSubquery.where(
    cb.equal(attendanceRoot.get<ProgrammeGroupMembershipEntity>("groupMembership"), groupMembershipRoot),
    cb.isFalse(attendanceSessionJoin.get("isPlaceholder")),
    cb.isFalse(attendanceSessionJoin.get("isCatchup")),
    cb.isNotNull(attendanceRoot.get<LocalDateTime>("recordedAt")),
    cb.isNotNull(attendanceRoot.get<SessionAttendanceNDeliusOutcomeEntity>("outcomeType")),
  )

  return cb.equal(sessionCountSubquery, attendanceCountSubquery)
}

fun getProgrammeGroupsSpecification(
  groupCode: String?,
  pdu: String?,
  deliveryLocations: List<String>?,
  cohort: ProgrammeGroupCohort?,
  sex: String?,
  regionName: String?,
): Specification<ProgrammeGroupEntity> = Specification { root, _, cb ->
  val predicates = mutableListOf<Predicate>()

  // Exclude soft-deleted
  predicates.add(cb.isNull(root.get<LocalDateTime>("deletedAt")))

  groupCode?.let {
    predicates.add(cb.like(cb.lower(root.get("code")), "%$groupCode%".lowercase()))
  }

  pdu?.let {
    predicates.add(cb.equal(root.get<String>("probationDeliveryUnitName"), it))
  }

  deliveryLocations?.let { locations ->
    if (locations.isNotEmpty()) {
      val orPredicates = locations.map { deliveryLocation ->
        cb.equal(root.get<String>("deliveryLocationName"), deliveryLocation)
      }
      predicates.add(cb.or(*orPredicates.toTypedArray()))
    }
  }

  cohort?.let {
    val (offenceType, hasLdc) = ProgrammeGroupCohort.toOffenceTypeAndLdc(it)
    predicates.add(cb.equal(root.get<OffenceCohort>("cohort"), offenceType))
    predicates.add(cb.equal(root.get<Boolean>("isLdc"), hasLdc))
  }

  sex?.let {
    predicates.add(cb.equal(root.get<ProgrammeGroupSexEnum>("sex"), it))
  }

  regionName?.let {
    predicates.add(cb.equal(root.get<String>("regionName"), it))
  }

  cb.and(*predicates.toTypedArray())
}

fun hasAttendedPostProgrammeReview(
  parentSubquery: Subquery<Long>,
  cb: CriteriaBuilder,
  groupMembershipRoot: Root<ProgrammeGroupMembershipEntity>,
): Predicate {
  val postProgrammeReviewAttendanceSubquery = parentSubquery.subquery(Long::class.java)
  val attendanceRoot = postProgrammeReviewAttendanceSubquery.from(SessionAttendanceEntity::class.java)
  val sessionJoin = attendanceRoot.join<SessionAttendanceEntity, SessionEntity>("session")
  val moduleSessionTemplateJoin = sessionJoin.join<SessionEntity, ModuleSessionTemplateEntity>("moduleSessionTemplate")
  val moduleJoin = moduleSessionTemplateJoin.join<ModuleSessionTemplateEntity, ModuleEntity>("module")
  val outcomeJoin = attendanceRoot.join<SessionAttendanceEntity, SessionAttendanceNDeliusOutcomeEntity>("outcomeType")

  postProgrammeReviewAttendanceSubquery.select(cb.count(attendanceRoot))
  postProgrammeReviewAttendanceSubquery.where(
    cb.equal(attendanceRoot.get<ProgrammeGroupMembershipEntity>("groupMembership"), groupMembershipRoot),
    cb.like(cb.lower(moduleJoin.get("name")), "post-programme%"),
    cb.isFalse(sessionJoin.get("isPlaceholder")),
    cb.isTrue(outcomeJoin.get("attendance")),
    cb.isTrue(outcomeJoin.get("compliant")),
  )

  return cb.greaterThan(postProgrammeReviewAttendanceSubquery, 0L)
}

fun isLatestReferralStatusProgrammeComplete(
  query: CriteriaQuery<*>,
  cb: CriteriaBuilder,
  referralJoin: Join<ProgrammeGroupMembershipEntity, ReferralEntity>,
): Predicate {
  val latestStatusCreatedAtSubquery = query.subquery(LocalDateTime::class.java)
  val latestStatusHistoryRoot = latestStatusCreatedAtSubquery.from(ReferralStatusHistoryEntity::class.java)

  latestStatusCreatedAtSubquery.select(cb.greatest(latestStatusHistoryRoot.get("createdAt")))
  latestStatusCreatedAtSubquery.where(
    cb.equal(latestStatusHistoryRoot.get<ReferralEntity>("referral"), referralJoin),
  )

  val programmeCompleteStatusExistsSubquery = query.subquery(Long::class.java)
  val statusHistoryRoot = programmeCompleteStatusExistsSubquery.from(ReferralStatusHistoryEntity::class.java)
  val referralStatusDescriptionJoin =
    statusHistoryRoot.join<ReferralStatusHistoryEntity, ReferralStatusDescriptionEntity>("referralStatusDescription")

  programmeCompleteStatusExistsSubquery.select(cb.literal(1L))
  programmeCompleteStatusExistsSubquery.where(
    cb.equal(statusHistoryRoot.get<ReferralEntity>("referral"), referralJoin),
    cb.equal(statusHistoryRoot.get<LocalDateTime>("createdAt"), latestStatusCreatedAtSubquery),
    cb.equal(referralStatusDescriptionJoin.get<String>("description"), "Programme complete"),
  )

  return cb.exists(programmeCompleteStatusExistsSubquery)
}

fun incompleteMembershipCountSubquery(
  query: CriteriaQuery<*>,
  cb: CriteriaBuilder,
  root: Root<ProgrammeGroupEntity>,
): Subquery<Long> {
  val incompleteMembershipCountSubquery = query.subquery(Long::class.java)
  val groupMembershipRoot = incompleteMembershipCountSubquery.from(ProgrammeGroupMembershipEntity::class.java)
  val referralJoin = groupMembershipRoot.join<ProgrammeGroupMembershipEntity, ReferralEntity>("referral")

  val isMembershipComplete = cb.and(
    isLatestReferralStatusProgrammeComplete(query, cb, referralJoin),
    hasAttendedPostProgrammeReview(incompleteMembershipCountSubquery, cb, groupMembershipRoot),
    allSessionsHaveAttendanceData(incompleteMembershipCountSubquery, cb, groupMembershipRoot),
  )

  incompleteMembershipCountSubquery.select(cb.count(groupMembershipRoot))
  incompleteMembershipCountSubquery.where(
    cb.equal(groupMembershipRoot.get<ProgrammeGroupEntity>("programmeGroup"), root),
    cb.not(isMembershipComplete),
  )
  return incompleteMembershipCountSubquery
}

fun getProgrammeGroupsByRegionTabSpecification(
  selectedTab: GroupPageByRegionTab,
): Specification<ProgrammeGroupEntity> = Specification { root, query, cb ->
  val datePath = root.get<LocalDate>("earliestPossibleStartDate")
  val incompleteMembershipCountSubquery = incompleteMembershipCountSubquery(query, cb, root)

  when (selectedTab) {
    GroupPageByRegionTab.NOT_STARTED_OR_IN_PROGRESS -> {
      val notStartedOrInProgressDateSpec = cb.or(
        cb.isNull(datePath),
        cb.greaterThan(datePath, LocalDate.now()),
      )

      cb.or(
        notStartedOrInProgressDateSpec,
        cb.greaterThan(incompleteMembershipCountSubquery, 0L),
        cb.not(hasAtLeastOneActiveMembership(query, cb, root)),
      )
    }

    GroupPageByRegionTab.COMPLETE -> {
      cb.and(
        cb.lessThanOrEqualTo(datePath, LocalDate.now()),
        cb.equal(incompleteMembershipCountSubquery, 0L),
        hasAtLeastOneMembershipIncludingDeleted(query, cb, root),
      )
    }
  }
}

fun hasAtLeastOneMembershipIncludingDeleted(
  query: CriteriaQuery<*>,
  cb: CriteriaBuilder,
  root: Root<ProgrammeGroupEntity>,
): Predicate {
  val membershipExistsSubquery = query.subquery(Long::class.java)
  val membershipRoot = membershipExistsSubquery.from(ProgrammeGroupMembershipEntity::class.java)

  membershipExistsSubquery.select(cb.literal(1L))
  membershipExistsSubquery.where(
    cb.equal(membershipRoot.get<ProgrammeGroupEntity>("programmeGroup"), root),
    cb.isNotNull(membershipRoot.get<LocalDateTime>("deletedAt")),
  )

  return cb.exists(membershipExistsSubquery)
}
