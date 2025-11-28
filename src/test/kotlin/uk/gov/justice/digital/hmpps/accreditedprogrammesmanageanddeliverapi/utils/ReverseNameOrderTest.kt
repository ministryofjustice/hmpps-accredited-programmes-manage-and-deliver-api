package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReverseNameOrderTest {

  @Test
  fun `test two part name`() {
    assertEquals("Smith John", ("John Smith").reverseNameOrder())
  }

  @Test
  fun `test three part name`() {
    assertEquals("Smith Paul John", ("John Paul Smith").reverseNameOrder())
  }

  @Test
  fun `test multiple middle names`() {
    assertEquals(
      "Johnson Elizabeth Anne Mary",
      ("Mary Elizabeth Anne Johnson").reverseNameOrder(),
    )
  }

  @Test
  fun `test extra spaces`() {
    assertEquals("Smith John", ("   John   Smith   ").reverseNameOrder())
    assertEquals("Brown Anna Mary John", (" John    Anna   Mary   Brown ").reverseNameOrder())
  }

  @Test
  fun `test single name`() {
    assertEquals("James", ("James").reverseNameOrder())
  }

  @Test
  fun `test empty string`() {
    assertEquals("", ("").reverseNameOrder())
    assertEquals("", ("   ").reverseNameOrder())
  }
}
