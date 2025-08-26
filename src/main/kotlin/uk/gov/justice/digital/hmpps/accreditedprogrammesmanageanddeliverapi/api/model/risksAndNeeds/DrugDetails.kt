package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.risksAndNeeds
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class DrugDetails(

  @Schema(example = "1 August 2023")
  @JsonFormat(pattern = "d MMMM yyyy")
  @get:JsonProperty("assessmentCompleted") val assessmentCompleted: LocalDate? = null,

  @Schema(example = "2 - At least weekly")
  @get:JsonProperty("levelOfUseOfMainDrug") val levelOfUseOfMainDrug: String? = null,

  @Schema(example = "1 - Some problems")
  @get:JsonProperty("drugsMajorActivity") val drugsMajorActivity: String? = null,
)
