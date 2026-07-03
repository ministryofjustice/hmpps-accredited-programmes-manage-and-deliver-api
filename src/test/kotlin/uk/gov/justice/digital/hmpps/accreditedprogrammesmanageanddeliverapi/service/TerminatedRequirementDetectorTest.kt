package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TerminatedRequirementDetectorTest {

  @Nested
  inner class IsTerminated {
    @Test
    fun `returns true for a canonical nDelius terminated-requirement body`() {
      val body = """{"status":400,"message":"Invalid Requirement IDs: [1503618208]"}"""
      assertThat(TerminatedRequirementDetector.isTerminated(body)).isTrue()
    }

    @Test
    fun `returns true when the marker appears alongside other text`() {
      val body = "Some prefix — Invalid Requirement IDs: [123] — some suffix"
      assertThat(TerminatedRequirementDetector.isTerminated(body)).isTrue()
    }

    @Test
    fun `returns false for a generic 400 body without the marker`() {
      val body = """{"status":400,"message":"Validation failure"}"""
      assertThat(TerminatedRequirementDetector.isTerminated(body)).isFalse()
    }

    @Test
    fun `returns false for a 404 requirement-not-found body`() {
      val body = """{"status":404,"message":"Requirement with id of 123 not found"}"""
      assertThat(TerminatedRequirementDetector.isTerminated(body)).isFalse()
    }

    @Test
    fun `returns false for null body`() {
      assertThat(TerminatedRequirementDetector.isTerminated(null)).isFalse()
    }

    @Test
    fun `returns false for empty body`() {
      assertThat(TerminatedRequirementDetector.isTerminated("")).isFalse()
    }
  }

  @Nested
  inner class ExtractRequirementIds {
    @Test
    fun `extracts a single ID`() {
      val body = """{"status":400,"message":"Invalid Requirement IDs: [1503618208]"}"""
      assertThat(TerminatedRequirementDetector.extractRequirementIds(body))
        .containsExactly("1503618208")
    }

    @Test
    fun `extracts multiple comma-separated IDs`() {
      val body = "Invalid Requirement IDs: [123, 456, 789]"
      assertThat(TerminatedRequirementDetector.extractRequirementIds(body))
        .containsExactly("123", "456", "789")
    }

    @Test
    fun `trims whitespace between IDs`() {
      val body = "Invalid Requirement IDs: [  123 ,  456  ]"
      assertThat(TerminatedRequirementDetector.extractRequirementIds(body))
        .containsExactly("123", "456")
    }

    @Test
    fun `returns empty list for empty brackets`() {
      val body = "Invalid Requirement IDs: []"
      assertThat(TerminatedRequirementDetector.extractRequirementIds(body)).isEmpty()
    }

    @Test
    fun `returns empty list when the marker is absent`() {
      val body = "some other error message"
      assertThat(TerminatedRequirementDetector.extractRequirementIds(body)).isEmpty()
    }

    @Test
    fun `returns empty list for null body`() {
      assertThat(TerminatedRequirementDetector.extractRequirementIds(null)).isEmpty()
    }

    @Test
    fun `returns empty list when brackets are missing`() {
      // Defensive — nDelius has never sent this shape, but the regex should not throw
      val body = "Invalid Requirement IDs: 123"
      assertThat(TerminatedRequirementDetector.extractRequirementIds(body)).isEmpty()
    }
  }
}
