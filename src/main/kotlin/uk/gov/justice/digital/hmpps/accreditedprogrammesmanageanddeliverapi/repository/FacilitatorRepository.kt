package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import java.util.UUID

interface FacilitatorRepository : JpaRepository<FacilitatorEntity, UUID> {
  fun findByNdeliusPersonCode(ndeliusPersonCode: String): FacilitatorEntity?

  @Query("SELECT f.ndeliusPersonCode FROM FacilitatorEntity f WHERE f.personSurname IS NULL")
  fun findAllNdeliusPersonCodesWherePersonSurnameIsNull(): List<String>
}
