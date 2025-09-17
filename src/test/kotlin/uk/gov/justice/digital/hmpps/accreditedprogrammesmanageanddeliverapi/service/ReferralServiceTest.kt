package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.FindAndReferInterventionApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.toReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.Ldc
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.toPniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.FindAndReferReferralDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.NDeliusPersonalDetailsFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniAssessmentFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.PniResponseFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ReferralServiceTest {

  @Mock
  private lateinit var findAndReferInterventionApiClient: FindAndReferInterventionApiClient

  @Mock
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Mock
  private lateinit var referralRepository: ReferralRepository

  @Mock
  private lateinit var serviceUserService: ServiceUserService

  @Mock
  private lateinit var cohortService: CohortService

  @Mock
  private lateinit var pniService: PniService

  @InjectMocks
  private lateinit var referralService: ReferralService

  @Test
  fun `getFindAndReferReferralDetails should return referral details when present`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralDetails = FindAndReferReferralDetailsFactory().withReferralId(referralId).produce()

    `when`(findAndReferInterventionApiClient.getFindAndReferReferral(referralId)).thenReturn(
      ClientResult.Success(
        status = HttpStatusCode.valueOf(200),
        body = referralDetails,
      ),
    )

    // When
    val result = referralService.getFindAndReferReferralDetails(referralId)

    // Then
    assertThat(referralDetails).isEqualTo(result)
    verify(findAndReferInterventionApiClient).getFindAndReferReferral(referralId)
  }

  @Test
  fun `getFindAndReferReferralDetails should throw NotFoundException when referral is not found`() {
    // Given
    val referralId = UUID.randomUUID()
    `when`(findAndReferInterventionApiClient.getFindAndReferReferral(referralId)).thenReturn(
      ClientResult.Failure.StatusCode(HttpMethod.GET, "/referral/$referralId", HttpStatusCode.valueOf(404), ""),
    )

    // When & Then
    assertThrows<NotFoundException> { referralService.getFindAndReferReferralDetails(referralId) }
    verify(findAndReferInterventionApiClient).getFindAndReferReferral(referralId)
  }

  @Test
  fun `createReferral should save referral and add status history and determine ldc status`() {
    // Given
    val referralDetails = FindAndReferReferralDetailsFactory().produce()
    val cohort = OffenceCohort.SEXUAL_OFFENCE
    val statusHistoryEntity = ReferralStatusHistoryEntityFactory().withStatus("Created").produce()
    val referralEntity =
      referralDetails.toReferralEntity(
        mutableListOf(statusHistoryEntity),
        cohort,
      ) // Use actual transformation with cohort

    val ldcResponse = Ldc(
      score = 4,
      subTotal = 4,
    )
    val pniAssessment = PniAssessmentFactory().withLdc(ldcResponse).produce()
    val pniResponse = PniResponseFactory().withAssessment(pniAssessment).produce()
    `when`(cohortService.determineOffenceCohort(pniResponse.toPniScore())).thenReturn(cohort)
    `when`(pniService.getPniCalculation(referralDetails.personReference)).thenReturn(pniResponse)
    `when`(referralRepository.save(any())).thenReturn(referralEntity)

    // When
    referralService.createReferral(referralDetails)

    // Then
    verify(cohortService).determineOffenceCohort(pniResponse.toPniScore())
    verify(referralRepository).save(
      argThat { saved ->
        saved.crn == referralEntity.crn &&
          saved.interventionType == referralEntity.interventionType &&
          saved.interventionName == referralEntity.interventionName &&
          saved.cohort == cohort &&
          saved.statusHistories.first().status == "Created" &&
          saved.referralLdcHistories.first().hasLdc
      },
    )
  }

  @Test
  fun `createReferral should save referral and add status history and determine ldc status when ldc is null from pni`() {
    // Given
    val referralDetails = FindAndReferReferralDetailsFactory().produce()
    val cohort = OffenceCohort.SEXUAL_OFFENCE
    val statusHistoryEntity = ReferralStatusHistoryEntityFactory().withStatus("Created").produce()
    val referralEntity =
      referralDetails.toReferralEntity(
        mutableListOf(statusHistoryEntity),
        cohort,
      ) // Use actual transformation with cohort

    val pniAssessment = PniAssessmentFactory().withLdc(null).produce()
    val pniResponse = PniResponseFactory().withAssessment(pniAssessment).produce()
    `when`(cohortService.determineOffenceCohort(pniResponse.toPniScore())).thenReturn(cohort)
    `when`(pniService.getPniCalculation(referralDetails.personReference)).thenReturn(pniResponse)
    `when`(referralRepository.save(any())).thenReturn(referralEntity)

    // When
    referralService.createReferral(referralDetails)

    // Then
    verify(cohortService).determineOffenceCohort(pniResponse.toPniScore())
    verify(referralRepository).save(
      argThat { saved ->
        saved.crn == referralEntity.crn &&
          saved.interventionType == referralEntity.interventionType &&
          saved.interventionName == referralEntity.interventionName &&
          saved.cohort == cohort &&
          saved.statusHistories.first().status == "Created" &&
          !saved.referralLdcHistories.first().hasLdc
      },
    )
  }

  @Test
  fun `getReferralDetails should retrieve details and return API model`() {
    // Given
    val referralId = UUID.randomUUID()
    val referralEntity: ReferralEntity = ReferralEntityFactory().withId(referralId).produce()
    val optionalEntity: Optional<ReferralEntity> =
      Optional.of<ReferralEntity>(referralEntity)
    `when`(referralRepository.findById(referralId)).thenReturn(optionalEntity as Optional<ReferralEntity?>)

    val personalDetails = NDeliusPersonalDetailsFactory().produce()
    // Use Mockito's when instead of MockK's every
    `when`(serviceUserService.getPersonalDetailsByIdentifier(referralEntity.crn)).thenReturn(personalDetails)

    // When
    val referralDetails = referralService.getReferralDetails(referralId)

    // Then
    verify(referralRepository).findById(referralId)
    verify(serviceUserService).getPersonalDetailsByIdentifier(referralEntity.crn)
    val expectedReferral = ReferralDetails.toModel(referralEntity, personalDetails)

    assertThat(referralDetails).isEqualTo(expectedReferral)
  }
}
