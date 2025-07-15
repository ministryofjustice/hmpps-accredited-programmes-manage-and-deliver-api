package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataCleaner
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.TestDataGenerator
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Availability
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Slot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.AvailabilityService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.DefaultAvailabilityConfigService
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.toPluralLabel
import java.time.DayOfWeek
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
}
