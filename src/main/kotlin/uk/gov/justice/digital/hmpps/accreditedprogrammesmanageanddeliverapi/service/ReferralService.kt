package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.FindAndReferInterventionApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.FindAndReferReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.toReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.PersonReferenceType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class ReferralService(
  private val findAndReferInterventionApiClient: FindAndReferInterventionApiClient,
  private val referralRepository: ReferralRepository,
  private val serviceUserService: ServiceUserService,
  private val cohortService: CohortService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getReferralDetails(referralId: UUID): ReferralDetails? {
    val referral = referralRepository.findByIdOrNull(referralId) ?: return null
    val personalDetails = serviceUserService.getPersonalDetailsByIdentifier(referral.crn)
    return ReferralDetails.toModel(referral, personalDetails)
  }

  fun getFindAndReferReferralDetails(referralId: UUID): FindAndReferReferralDetails {
    val referralDetails =
      when (val result = findAndReferInterventionApiClient.getFindAndReferReferral(referralId = referralId)) {
        is ClientResult.Failure -> {
          log.warn("Failure to retrieve referral details for uuid : $referralId")
          throw NotFoundException("No referral details found for id: $referralId")
        }

        is ClientResult.Success -> result.body
      }
    return referralDetails
  }

  fun createReferral(findAndReferReferralDetails: FindAndReferReferralDetails) {
    val crn = if (findAndReferReferralDetails.personReferenceType == PersonReferenceType.CRN) {
      findAndReferReferralDetails.personReference
    } else {
      throw IllegalArgumentException("Only CRN personReferenceType is supported")
    }

    val cohort = cohortService.determineOffenceCohort(crn)

    val statusHistoryEntity = ReferralStatusHistoryEntity(
      status = "Created",
      startDate = LocalDateTime.now(),
      endDate = null,
    )

    val referralEntity = findAndReferReferralDetails.toReferralEntity(mutableListOf(statusHistoryEntity), cohort)

    log.info("Inserting referral for Intervention: '${referralEntity.interventionName}' and Crn: '${referralEntity.crn}' with cohort: $cohort")
    referralRepository.save(referralEntity)
  }

  fun getReferralById(id: UUID): ReferralEntity? = referralRepository.findByIdOrNull(id)
}
