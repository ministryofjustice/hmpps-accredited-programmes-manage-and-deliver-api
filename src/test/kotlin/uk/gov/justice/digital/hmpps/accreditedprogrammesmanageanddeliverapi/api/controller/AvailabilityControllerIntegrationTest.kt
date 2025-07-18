package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Availability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Slot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create.CreateAvailability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.AvailabilityService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.DefaultAvailabilityConfigService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.toPluralLabel
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
    assertThat(availability).isNotNull
    assertThat(availability.id).isNull()
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
      body = buildAvailabilityCreateModel(referralId = referralEntity.id!!, startDate = startDate, endDate = endDate, otherDetails = otherDetails),
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

    val monday = availability.availabilities.find { it.label == "Mondays" }
    assertThat(monday).isNotNull
    assertThat(monday!!.slots).containsExactly(
      Slot("daytime", true),
      Slot("evening", true),
    )

    val tuesday = availability.availabilities.find { it.label == "Tuesdays" }
    assertThat(tuesday).isNotNull
    assertThat(tuesday!!.slots).allSatisfy { slot ->
      assertThat(slot.value).isFalse()
    }

    val wednesday = availability.availabilities.find { it.label == "Wednesdays" }
    assertThat(wednesday).isNotNull
    assertThat(wednesday!!.slots).containsExactly(
      Slot("daytime", false),
      Slot("evening", true),
    )
  }

  fun getDefaultAvailability(): List<DailyAvailabilityModel> {
    val slotLabels = SlotName.entries.map { it -> it.displayName }
    return DayOfWeek.entries.map { day ->
      DailyAvailabilityModel(
        label = day.toPluralLabel(),
        slots = slotLabels.map { slot ->
          Slot(label = slot, value = false)
        },
      )
    }
  }

  fun buildAvailabilityCreateModel(
    referralId: UUID = UUID.randomUUID(),
    startDate: LocalDateTime? = LocalDateTime.now().minusDays(1),
    endDate: LocalDateTime? = LocalDateTime.now().plusDays(10),
    otherDetails: String? = "Available remotely",
    selectedSlots: Map<String, Set<String>> = mapOf(
      "Mondays" to setOf("daytime", "evening"),
      "Wednesdays" to setOf("evening"),
    ),
  ): CreateAvailability {
    val allSlotLabels = SlotName.entries.map { it.displayName }

    val availabilities = DayOfWeek.entries.map { day ->
      val label = day.toPluralLabel()
      val selectedForDay = selectedSlots[label] ?: emptySet()

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
}
