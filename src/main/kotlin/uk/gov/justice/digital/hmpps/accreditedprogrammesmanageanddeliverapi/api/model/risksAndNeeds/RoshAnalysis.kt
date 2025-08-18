package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class RoshAnalysis(

  @get:JsonProperty("assessmentCompleted", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  val assessmentCompleted: LocalDate? = null,

  @Schema(example = "Tax evasion", description = "The details of the current offence.")
  @get:JsonProperty("offenceDetails") val offenceDetails: String? = null,

  @Schema(example = "At home", description = "The where and when of the current offence.")
  @get:JsonProperty("whereAndWhen") val whereAndWhen: String? = null,

  @Schema(example = "false accounting", description = "How the offence was committed for the current offence.")
  @get:JsonProperty("howDone") val howDone: String? = null,

  @Schema(example = "hmrc", description = "The victims of the current offence.")
  @get:JsonProperty("whoVictims") val whoVictims: String? = null,

  @Schema(
    example = "company secretary",
    description = "Text describing if anyone else was present for the current offence.",
  )
  @get:JsonProperty("anyoneElsePresent") val anyoneElsePresent: String? = null,

  @Schema(example = "Greed", description = "The motivation for the current offence.")
  @get:JsonProperty("whyDone") val whyDone: String? = null,

  @Schema(example = "crown court", description = "The source of this information for the current offence.")
  @get:JsonProperty("sources") val sources: String? = null,

  @Schema(
    example = "Physical assault on cellmate requiring medical attention on 22nd March 2024. Weapon possession (improvised blade) discovered during cell search on 8th February 2024.",
    description = "Identify behaviours / incidents that evidence the individual’s ability to cause serious harm and when they happened",
  )
  @get:JsonProperty("identifyBehavioursIncidents") val identifyBehavioursIncidents: String? = null,

  @Schema(
    example = "Escalating violence in evenings when challenged, targeting vulnerable individuals, causing injuries requiring medical attention.",
    description = "Provide an analysis of any patterns related to these behaviours / incidents, for example: victims, triggers, locations, impact.",
  )
  @get:JsonProperty("analysisBehaviourIncidents") val analysisBehaviourIncidents: String? = null,
)
