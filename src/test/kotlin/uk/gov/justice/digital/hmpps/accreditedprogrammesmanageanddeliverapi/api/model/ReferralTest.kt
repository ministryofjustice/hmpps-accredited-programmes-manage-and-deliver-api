package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.InterventionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SettingType
import java.time.LocalDateTime
import java.util.UUID

internal class ReferralTest {

  @Test
  fun `toApi should map ReferralEntity to Referral with active status`() {
    // Arrange
    val id = UUID.randomUUID()
    val personName = "John Doe"
    val crn = "X12345"
    val createdAt = LocalDateTime.now()
    val activeStatus = "Created"
    val setting = SettingType.COMMUNITY
    val cohort = OffenceCohort.SEXUAL_OFFENCE

    val statusHistory = ReferralStatusHistoryEntity(status = activeStatus, endDate = null)
    val referralEntity = ReferralEntity(
      id = id,
      personName = personName,
      crn = crn,
      createdAt = createdAt,
      statusHistories = mutableListOf(statusHistory),
      setting = setting,
      interventionType = InterventionType.ACP,
      interventionName = "Building Choices",
      eventNumber = 1,
      eventId = "2500828798",
      cohort = OffenceCohort.SEXUAL_OFFENCE,
    )

    // Act
    val referral = referralEntity.toApi()

    // Assert
    assertEquals(id, referral.id)
    assertEquals(personName, referral.personName)
    assertEquals(crn, referral.crn)
    assertEquals(createdAt, referral.createdAt)
    assertEquals(activeStatus, referral.status)
    assertEquals(cohort, OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `toApi should use 'Unknown' status when no active status is found`() {
    // Arrange
    val id = UUID.randomUUID()
    val personName = "Jane Doe"
    val crn = "Y67890"
    val createdAt = LocalDateTime.now()
    val setting = SettingType.COMMUNITY
    val cohort = OffenceCohort.SEXUAL_OFFENCE

    val statusHistory = ReferralStatusHistoryEntity(status = "Withdrawn", endDate = LocalDateTime.now())
    val referralEntity = ReferralEntity(
      id = id,
      personName = personName,
      crn = crn,
      createdAt = createdAt,
      statusHistories = mutableListOf(statusHistory),
      setting = setting,
      interventionType = InterventionType.ACP,
      interventionName = "Building Choices",
      eventId = "2500828798",
      eventNumber = 1,
      cohort = cohort,
    )

    // Act
    val referral = referralEntity.toApi()

    // Assert
    assertEquals(id, referral.id)
    assertEquals(personName, referral.personName)
    assertEquals(crn, referral.crn)
    assertEquals(createdAt, referral.createdAt)
    assertEquals("Unknown", referral.status)
    assertEquals(cohort, OffenceCohort.SEXUAL_OFFENCE)
  }

  @Test
  fun `toApi should handle empty status history`() {
    // Arrange
    val id = UUID.randomUUID()
    val personName = "Mark Smith"
    val crn = "Z12345"
    val createdAt = LocalDateTime.now()
    val setting = SettingType.COMMUNITY
    val cohort = OffenceCohort.GENERAL_OFFENCE

    val referralEntity = ReferralEntity(
      id = id,
      personName = personName,
      crn = crn,
      createdAt = createdAt,
      statusHistories = mutableListOf(),
      setting = setting,
      interventionType = InterventionType.ACP,
      interventionName = "Building Choices",
      eventId = "2500828798",
      eventNumber = 1,
      cohort = cohort,
    )

    // Act
    val referral = referralEntity.toApi()

    // Assert
    assertEquals(id, referral.id)
    assertEquals(personName, referral.personName)
    assertEquals(crn, referral.crn)
    assertEquals(createdAt, referral.createdAt)
    assertEquals("Unknown", referral.status)
    assertEquals(cohort, OffenceCohort.GENERAL_OFFENCE)
  }
}
