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
  fun `findAllIdsWherePersonSurnameOrForenameIsUnknown should return matching referrals`() {
    // Given
    val referralWithUnknownSurname = ReferralEntityFactory()
      .withPersonForename("John")
      .withPersonSurname("UNKNOWN")
      .produce()
    val referralWithUnknownForename = ReferralEntityFactory()
      .withPersonForename("UNKNOWN")
      .withPersonSurname("Smith")
      .produce()
    val referralWithKnownNames = ReferralEntityFactory()
      .withPersonForename("Jane")
      .withPersonSurname("Doe")
      .produce()

    referralRepository.save(referralWithUnknownSurname)
    referralRepository.save(referralWithUnknownForename)
    referralRepository.save(referralWithKnownNames)

    // When
    val result = referralRepository.findAllIdsWherePersonSurnameOrForenameIsUnknown()

    // Then
    assertThat(result).hasSize(2)
    assertThat(result).containsExactlyInAnyOrder(referralWithUnknownSurname.id, referralWithUnknownForename.id)
  }

  @Test
  @Transactional
  fun `findAllIdsWherePersonSurnameOrForenameIsUnknown should return empty list when no referrals have UNKNOWN names`() {
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
    val result = referralRepository.findAllIdsWherePersonSurnameOrForenameIsUnknown()

    // Then
    assertThat(result).isEmpty()
  }
}
