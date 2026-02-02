package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.integration.IntegrationTestBase

@Disabled("Temporarily disabled")
class TestGroupHelperTest : IntegrationTestBase() {

  @Test
  fun `test group creation helper`() {
    val group = testGroupHelper.createGroup()
    assertThat(group).isNotNull()
    assertThat(group.sessions).isNotEmpty
    assertThat(group.treatmentManager).isNotNull
    assertThat(group.earliestPossibleStartDate).isNotNull
  }
}
