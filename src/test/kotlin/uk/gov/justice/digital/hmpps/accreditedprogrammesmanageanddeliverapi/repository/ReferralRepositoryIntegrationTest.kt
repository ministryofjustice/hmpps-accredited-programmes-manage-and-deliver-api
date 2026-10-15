package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

class ReferralRepositoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @BeforeEach
  override fun beforeEach() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  @Transactional
  fun `findAllIdsWherePersonSurnameIsNull should return only IDs of referrals with null surname`() {
    // Given
    val referralWithNullSurname = ReferralEntityFactory()
      .withPersonSurname(null)
      .produce()

    val referralWithSurname = ReferralEntityFactory()
      .withPersonSurname("Smith")
      .produce()

    referralRepository.save(referralWithNullSurname)
    referralRepository.save(referralWithSurname)

    // When
    val result = referralRepository.findAllIdsWherePersonSurnameIsNull()

    // Then
    assertThat(result).hasSize(1)
    assertThat(result).containsExactly(referralWithNullSurname.id)
  }

  @Test
  @Transactional
  fun `findAllIdsWherePersonSurnameIsNull should return empty list when no referrals have null surname`() {
    // Given
    val referralWithSurname1 = ReferralEntityFactory()
      .withPersonSurname("Smith")
      .produce()
    val referralWithSurname2 = ReferralEntityFactory()
      .withPersonSurname("Jones")
      .produce()

    referralRepository.save(referralWithSurname1)
    referralRepository.save(referralWithSurname2)

    // When
    val result = referralRepository.findAllIdsWherePersonSurnameIsNull()

    // Then
    assertThat(result).isEmpty()
  }

  @Test
  @Transactional
  fun `findAllIdsWherePersonSurnameIsNull should return all IDs when all referrals have null surname`() {
    // Given
    val referralWithNullSurname1 = ReferralEntityFactory()
      .withPersonSurname(null)
      .produce()
    val referralWithNullSurname2 = ReferralEntityFactory()
      .withPersonSurname(null)
      .produce()

    referralRepository.save(referralWithNullSurname1)
    referralRepository.save(referralWithNullSurname2)

    // When
    val result = referralRepository.findAllIdsWherePersonSurnameIsNull()

    // Then
    assertThat(result).hasSize(2)
    assertThat(result).containsExactlyInAnyOrder(referralWithNullSurname1.id, referralWithNullSurname2.id)
  }
}
