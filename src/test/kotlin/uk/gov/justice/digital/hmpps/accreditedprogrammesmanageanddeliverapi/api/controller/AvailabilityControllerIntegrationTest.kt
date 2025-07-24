package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Availability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.AvailabilityOption
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Slot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.CreateAvailability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.update.UpdateAvailability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.AvailabilityService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.DefaultAvailabilityConfigService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.toAvailabilityOptions
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.UUID

class AvailabilityControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var testDataCleaner: TestDataCleaner

  @Autowired
  private lateinit var testDataGenerator: TestDataGenerator

  @Autowired
  private lateinit var availabilityService: AvailabilityService

  @Autowired
  private lateinit var defaultAvailabilityConfigService: DefaultAvailabilityConfigService

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `Default availability config is returned when referral does not exist`() {
    val referralId = UUID.randomUUID()

    val availability = performRequestAndExpectOk(
      HttpMethod.GET,
      "/availability/referral/$referralId",
      object : ParameterizedTypeReference<Availability>() {},
    )

    assertThat(availability).isNotNull
    assertThat(availability.id).isNull()
    assertThat(availability.availabilities).isEqualTo(getDefaultAvailability())
  }

  @Test
  fun `Default availability is returned when referral with no availability exists`() {
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)

    val availability = performRequestAndExpectOk(
      HttpMethod.GET,
      "/availability/referral/${referralEntity.id}",
      object : ParameterizedTypeReference<Availability>() {},
    )

    with(availability) {
      assertNull(id)
      assertNull(referralId)
      assertNull(startDate)
      assertNull(endDate)
      assertNull(otherDetails)
      assertNull(lastModifiedBy)
      assertNull(lastModifiedAt)
      assertThat(availabilities.size).isEqualTo(7)

      val expectedLabels = listOf(
        "Mondays",
        "Tuesdays",
        "Wednesdays",
        "Thursdays",
        "Fridays",
        "Saturdays",
        "Sundays",
      )

      availabilities.forEachIndexed { index, dailyAvailability ->
        assertEquals(expectedLabels[index], dailyAvailability.label.toString())
        assertEquals(2, dailyAvailability.slots.size)

        val slotLabels = dailyAvailability.slots.map { it.label }
        assertTrue("daytime" in slotLabels)
        assertTrue("evening" in slotLabels)

        dailyAvailability.slots.forEach { slot ->
          assertFalse(slot.value)
        }
      }
    }
  }

  @Test
  fun `Create availability is successful`() {
    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)

    val otherDetails = "Available remotely"
    val lastModifiedBy = "AUTH_ADM"

    val startDate: LocalDateTime? = LocalDateTime.now()
    val endDate: LocalDateTime? = LocalDateTime.now().plusDays(10)
    val availability = performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.POST,
      uri = "/availability",
      returnType = object : ParameterizedTypeReference<Availability>() {},
      body = buildCreateAvailabilityModel(
        referralId = referralEntity.id!!,
        startDate = startDate,
        endDate = endDate,
        otherDetails = otherDetails,
      ),
      expectedResponseStatus = HttpStatus.CREATED.value(),
    )

    assertThat(availability.id.toString()).isNotNull
    assertThat(availability.referralId.toString()).isEqualTo(referralEntity.id.toString())
    assertThat(availability.startDate?.toLocalDate()).isEqualTo(startDate?.toLocalDate())
    assertThat(availability.endDate?.toLocalDate()).isEqualTo(endDate?.toLocalDate())
    assertThat(availability.otherDetails).isEqualTo(otherDetails)
    assertThat(availability.lastModifiedBy).isEqualTo(lastModifiedBy)
    assertThat(availability.lastModifiedAt).isNotNull

    assertThat(availability.availabilities).hasSize(7)

    val monday = availability.availabilities.find { it.label.displayName == "Mondays" }
    assertThat(monday).isNotNull
    assertThat(monday!!.slots).containsExactly(
      Slot(SlotName.DAYTIME.displayName, true),
      Slot(SlotName.EVENING.displayName, true),
    )

    val tuesday = availability.availabilities.find { it.label.displayName == "Tuesdays" }
    assertThat(tuesday).isNotNull
    assertThat(tuesday!!.slots).allSatisfy { slot ->
      assertThat(slot.value).isFalse()
    }

    val wednesday = availability.availabilities.find { it.label.displayName == "Wednesdays" }
    assertThat(wednesday).isNotNull
    assertThat(wednesday!!.slots).containsExactly(
      Slot(SlotName.DAYTIME.displayName, false),
      Slot(SlotName.EVENING.displayName, true),
    )
  }

  @Test
  fun `Update availability is successful`() {
    val otherDetails = "Available remotely"
    val lastModifiedBy = "AUTH_ADM"
    val startDate: LocalDateTime = LocalDateTime.now()
    val endDate: LocalDateTime = LocalDateTime.now().plusDays(10)

    val referralEntity = ReferralEntityFactory().produce()
    testDataGenerator.createReferral(referralEntity)

    val createAvailability = createAndGetAvailability(referralEntity, startDate, endDate, otherDetails)

    val availability = performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.PUT,
      uri = "/availability",
      returnType = object : ParameterizedTypeReference<Availability>() {},
      body = buildUpdateAvailabilityModel(
        availabilityId = createAvailability.id!!,
        referralId = referralEntity.id!!,
        startDate = startDate,
        endDate = endDate,
        otherDetails = otherDetails,
        selectedSlots = mapOf(
          AvailabilityOption.MONDAY.displayName to setOf(SlotName.DAYTIME.displayName, SlotName.EVENING.displayName),
          AvailabilityOption.WEDNESDAY.displayName to setOf(SlotName.EVENING.displayName),
        ),
      ),
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    assertThat(availability.id.toString()).isNotNull
    assertThat(availability.referralId.toString()).isEqualTo(referralEntity.id.toString())
    assertThat(availability.startDate?.toLocalDate()).isEqualTo(startDate.toLocalDate())
    assertThat(availability.endDate?.toLocalDate()).isEqualTo(endDate.toLocalDate())
    assertThat(availability.otherDetails).isEqualTo(otherDetails)
    assertThat(availability.lastModifiedBy).isEqualTo(lastModifiedBy)
    assertThat(availability.lastModifiedAt).isNotNull

    assertThat(availability.availabilities).hasSize(7)

    val monday = availability.availabilities.find { it.label.displayName == "Mondays" }
    assertThat(monday).isNotNull
    assertThat(monday?.slots).containsExactly(
      Slot(SlotName.DAYTIME.displayName, true),
      Slot(SlotName.EVENING.displayName, true),
    )

    val tuesday = availability.availabilities.find { it.label.displayName == "Tuesdays" }
    assertThat(tuesday).isNotNull
    assertThat(tuesday?.slots).allSatisfy { slot ->
      assertThat(slot.value).isFalse()
    }

    val wednesday = availability.availabilities.find { it.label.displayName == "Wednesdays" }
    assertThat(wednesday).isNotNull
    assertThat(wednesday!!.slots).containsExactly(
      Slot(SlotName.DAYTIME.displayName, false),
      Slot(SlotName.EVENING.displayName, true),
    )
  }

  fun getDefaultAvailability(): List<DailyAvailabilityModel> {
    val slotLabels = SlotName.entries.map { it -> it.displayName }
    return DayOfWeek.entries.map { day ->
      DailyAvailabilityModel(
        label = day.toAvailabilityOptions(),
        slots = slotLabels.map { slot ->
          Slot(label = slot, value = false)
        },
      )
    }
  }

  fun buildUpdateAvailabilityModel(
    availabilityId: UUID,
    referralId: UUID,
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    otherDetails: String?,
    selectedSlots: Map<String, Set<String>>,
  ): UpdateAvailability = UpdateAvailability(
    availabilityId = availabilityId,
    referralId = referralId,
    startDate = startDate,
    endDate = endDate,
    otherDetails = otherDetails,
    availabilities = AvailabilityOption.values().map { option ->
      DailyAvailabilityModel(
        label = option,
        slots = SlotName.values().map { slot ->
          Slot(
            label = slot.displayName,
            value = selectedSlots[option.displayName]?.contains(slot.displayName) ?: false,
          )
        },
      )
    },
  )
}

private fun AvailabilityControllerIntegrationTest.createAndGetAvailability(
  referralEntity: ReferralEntity,
  startDate: LocalDateTime,
  endDate: LocalDateTime,
  otherDetails: String,
): Availability = performRequestAndExpectStatusWithBody(
  httpMethod = HttpMethod.POST,
  uri = "/availability",
  returnType = object : ParameterizedTypeReference<Availability>() {},
  body = buildCreateAvailabilityModel(
    referralId = referralEntity.id!!,
    startDate = startDate,
    endDate = endDate,
    otherDetails = otherDetails,
    selectedSlots = mapOf(
      AvailabilityOption.MONDAY.displayName to setOf(SlotName.DAYTIME.displayName, SlotName.EVENING.displayName),
      AvailabilityOption.WEDNESDAY.displayName to setOf(SlotName.EVENING.displayName),
    ),
  ),
  expectedResponseStatus = HttpStatus.CREATED.value(),
)

fun buildCreateAvailabilityModel(
  referralId: UUID = UUID.randomUUID(),
  startDate: LocalDateTime? = LocalDateTime.now().minusDays(1),
  endDate: LocalDateTime? = LocalDateTime.now().plusDays(10),
  otherDetails: String? = "Available remotely",
  selectedSlots: Map<String, Set<String>> = mapOf(
    AvailabilityOption.MONDAY.displayName to setOf(SlotName.DAYTIME.displayName, SlotName.EVENING.displayName),
    AvailabilityOption.WEDNESDAY.displayName to setOf(SlotName.EVENING.displayName),
  ),
): CreateAvailability {
  val allSlotLabels = SlotName.entries.map { it.displayName }

  val availabilities = DayOfWeek.entries.map { day ->
    val label = day.toAvailabilityOptions()
    val selectedForDay = selectedSlots[label.displayName] ?: emptySet()

    DailyAvailabilityModel(
      label = label,
      slots = allSlotLabels.map { slotLabel ->
        Slot(
          label = slotLabel,
          value = slotLabel in selectedForDay,
        )
      },
    )
  }

  return CreateAvailability(
    referralId = referralId,
    startDate = startDate,
    endDate = endDate,
    otherDetails = otherDetails,
    availabilities = availabilities,
  )
}
