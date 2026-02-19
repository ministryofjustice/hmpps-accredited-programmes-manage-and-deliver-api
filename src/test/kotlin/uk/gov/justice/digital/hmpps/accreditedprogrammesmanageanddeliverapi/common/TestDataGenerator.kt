package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity.StagingIapsLicreqnosEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity.StagingReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity.StagingReportingLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AccreditedProgrammeTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.DeliveryLocationPreferenceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleSessionTemplateEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.NDeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocationProbationDeliveryUnitEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralLdcHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralMotivationBackgroundAndNonAssociationsEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralReportingLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.Pathway
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AttendeeRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.time.LocalDate
import java.util.UUID

@Transactional
@Component
class TestDataGenerator {
  @Autowired
  private lateinit var programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  @Autowired
  private lateinit var referralStatusDescriptionRepository: ReferralStatusDescriptionRepository

  @Autowired
  private lateinit var moduleSessionTemplateRepository: ModuleSessionTemplateRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var attendeeRepository: AttendeeRepository

  fun createPreferredDeliveryLocationProbationDeliveryUnit(preferredDeliveryLocationProbationDeliveryUnit: PreferredDeliveryLocationProbationDeliveryUnitEntity) {
    entityManager.persist(preferredDeliveryLocationProbationDeliveryUnit)
  }

  fun createPreferredDeliveryLocation(preferredDeliveryLocation: PreferredDeliveryLocationEntity) {
    entityManager.persist(preferredDeliveryLocation)
  }

  fun createDeliveryLocationPreference(deliveryLocationPreferenceEntity: DeliveryLocationPreferenceEntity) {
    entityManager.persist(deliveryLocationPreferenceEntity)
  }

  fun createReferralWithDeliveryLocationPreferences(
    referralEntity: ReferralEntity,
    pdu: PreferredDeliveryLocationProbationDeliveryUnitEntity? = null,
    preferredDeliveryLocation: PreferredDeliveryLocationEntity? = null,
    deliveryLocationPreference: DeliveryLocationPreferenceEntity? = null,
  ) {
    createReferralWithStatusHistory(referralEntity)
    pdu?.let { createPreferredDeliveryLocationProbationDeliveryUnit(pdu) }
    preferredDeliveryLocation?.let { createPreferredDeliveryLocation(preferredDeliveryLocation) }
    deliveryLocationPreference?.let { createDeliveryLocationPreference(deliveryLocationPreference) }
  }

  fun createLdcHistoryForAReferral(referralLdcHistoryEntity: ReferralLdcHistoryEntity) {
    entityManager.persist(referralLdcHistoryEntity)
  }

  fun getReferralById(id: UUID): ReferralEntity = entityManager
    .createNativeQuery("SELECT * FROM referral r WHERE r.id = :referralId", ReferralEntity::class.java)
    .setParameter("referralId", id)
    .singleResult as ReferralEntity

  fun createAvailability(availabilityEntity: AvailabilityEntity) {
    entityManager.persist(availabilityEntity)
  }

  fun createReferralStatusDescriptionEntity(referralStatusDescriptionEntity: ReferralStatusDescriptionEntity) {
    entityManager.persist(referralStatusDescriptionEntity)
  }

  fun creatReferralStatusHistory(referralStatusHistoryEntity: ReferralStatusHistoryEntity) {
    entityManager.persist(referralStatusHistoryEntity)
  }

  fun createReferralWithStatusHistory(
    referralEntity: ReferralEntity? = null,
    referralStatusHistoryEntity: ReferralStatusHistoryEntity? = null,
  ) {
    val referral = referralEntity ?: ReferralEntityFactory().produce()

    val statusHistory = referralStatusHistoryEntity ?: ReferralStatusHistoryEntityFactory().produce(
      referral,
      referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription(),
    )

    entityManager.persist(referral)
    entityManager.persist(statusHistory)
  }

  fun <T> createReferralWithFields(
    referralEntity: ReferralEntity? = null,
    fields: List<T>,
  ) {
    validateFields(fields)

    entityManager.persist(referralEntity)
    for (field in fields) {
      entityManager.persist(field)
    }
  }

  private fun <T> validateFields(fields: List<T>) {
    // Add to this as we add more fields to the ReferralEntity that want to be persisted through createReferralWithFields.
    val acceptableFieldTypes = listOf(
      ReferralEntity::class.java,
      ReferralStatusHistoryEntity::class.java,
      ReferralMotivationBackgroundAndNonAssociationsEntity::class.java,
    )
    fields.forEach {
      if (!acceptableFieldTypes.contains(it!!::class.java)) {
        throw IllegalArgumentException("Invalid field type for createReferralWithFields method: ${it::class.java}")
      }
    }
  }

  fun createReferralWithStatusHistory(
    referralEntity: ReferralEntity? = null,
    statusDescriptionList: List<ReferralStatusDescriptionEntity>,
  ) {
    val referral = referralEntity ?: ReferralEntityFactory().produce()
    entityManager.persist(referral)

    statusDescriptionList.forEach {
      val statusHistory = ReferralStatusHistoryEntityFactory().produce(referral, it)
      entityManager.persist(statusHistory)
    }
  }

  fun createGroupMembership(
    programmeGroupMembership: ProgrammeGroupMembershipEntity,
  ) {
    entityManager.persist(programmeGroupMembership)
  }

  fun createReferralWithReportingLocation(referralReportingLocationEntity: ReferralReportingLocationEntity) {
    entityManager.persist(referralReportingLocationEntity)
  }

  fun createReferralWithReportingLocationAndStatusHistory(
    referralEntity: ReferralEntity,
    referralStatusHistoryEntity: ReferralStatusHistoryEntity,
    referralReportingLocationEntity: ReferralReportingLocationEntity,
  ) {
    entityManager.persist(referralEntity)
    entityManager.persist(referralStatusHistoryEntity)
    entityManager.persist(referralReportingLocationEntity)
  }

  fun createReferralStatusHistory(
    referralStatusHistoryEntity: ReferralStatusHistoryEntity,
  ) {
    entityManager.persist(referralStatusHistoryEntity)
  }

  fun refreshReferralCaseListItemView() {
    entityManager.createNativeQuery("REFRESH MATERIALIZED VIEW referral_caselist_item_view").executeUpdate()
  }

  fun createGroup(
    group: ProgrammeGroupEntity,
    sessions: MutableSet<SessionEntity> = mutableSetOf(),
  ): ProgrammeGroupEntity {
    entityManager.persist(group)
    sessions.forEach { entityManager.persist(it) }
    return group
  }

  fun createSession(
    session: SessionEntity,
  ): SessionEntity {
    entityManager.persist(session)
    return session
  }

  fun createAccreditedProgrammeTemplate(
    name: String,
  ): AccreditedProgrammeTemplateEntity {
    val template = AccreditedProgrammeTemplateEntity(
      name = name,
      validFrom = LocalDate.now(),
      validUntil = null,
    )
    entityManager.persist(template)
    return template
  }

  fun createModule(
    template: AccreditedProgrammeTemplateEntity,
    name: String,
    moduleNumber: Int,
  ): ModuleEntity {
    val module = ModuleEntity(
      accreditedProgrammeTemplate = template,
      name = name,
      moduleNumber = moduleNumber,
    )
    entityManager.persist(module)
    return module
  }

  fun createModuleSessionTemplate(moduleSessionTemplate: ModuleSessionTemplateEntity): ModuleSessionTemplateEntity = moduleSessionTemplateRepository.save(moduleSessionTemplate)

  fun createFacilitator(facilitator: FacilitatorEntity): FacilitatorEntity {
    entityManager.persist(facilitator)
    return facilitator
  }

  fun createReferral(personName: String, crn: String): ReferralEntity {
    val referral = ReferralEntityFactory().withPersonName(personName).withCrn(crn).produce()
    return referralRepository.save(referral)
  }

  fun createAttendee(referral: ReferralEntity, session: SessionEntity): AttendeeEntity {
    val attendee = AttendeeEntity(referral = referral, session = session)
    return attendeeRepository.save(attendee)
  }

  fun createNDeliusAppointment(
    session: SessionEntity,
    referral: ReferralEntity,
    nDeliusAppointmentId: UUID = UUID.randomUUID(),
  ): NDeliusAppointmentEntity {
    val appointment = NDeliusAppointmentEntity(
      session = session,
      referral = referral,
      ndeliusAppointmentId = nDeliusAppointmentId,
    )
    entityManager.persist(appointment)
    return appointment
  }

  fun allocateReferralsToGroup(
    referrals: List<ReferralEntity>,
    group: ProgrammeGroupEntity,
  ): List<ProgrammeGroupMembershipEntity> {
    val groupMembershipEntities = referrals.map {
      ProgrammeGroupMembershipEntity(
        referral = it,
        programmeGroup = group,
      )
    }
    return programmeGroupMembershipRepository.saveAll(groupMembershipEntities)
  }

  fun createModuleSessionTemplate(
    module: ModuleEntity,
    name: String,
    sessionNumber: Int,
    sessionType: SessionType = SessionType.GROUP,
    pathway: Pathway = Pathway.MODERATE_INTENSITY,
    durationMinutes: Int = 120,
  ): ModuleSessionTemplateEntity {
    val template = ModuleSessionTemplateEntity(
      module = module,
      name = name,
      sessionNumber = sessionNumber,
      sessionType = sessionType,
      pathway = pathway,
      durationMinutes = durationMinutes,
    )
    entityManager.persist(template)
    return template
  }

  fun createStagingReferral(stagingReferral: StagingReferralEntity): StagingReferralEntity {
    entityManager.persist(stagingReferral)
    return stagingReferral
  }

  fun createStagingReportingLocation(stagingReportingLocation: StagingReportingLocationEntity): StagingReportingLocationEntity {
    entityManager.persist(stagingReportingLocation)
    return stagingReportingLocation
  }

  fun createStagingIapsLicreqno(stagingIapsLicreqno: StagingIapsLicreqnosEntity): StagingIapsLicreqnosEntity {
    entityManager.persist(stagingIapsLicreqno)
    return stagingIapsLicreqno
  }
}
