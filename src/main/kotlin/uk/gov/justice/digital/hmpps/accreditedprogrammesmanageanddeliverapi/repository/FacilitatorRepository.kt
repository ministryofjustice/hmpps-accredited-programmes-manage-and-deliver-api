package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import java.util.UUID

@Repository
interface FacilitatorRepository : JpaRepository<FacilitatorEntity, UUID> {
  fun findByNdeliusPersonCode(ndeliusPersonCode: String): FacilitatorEntity?

  @Query("SELECT f FROM FacilitatorEntity f WHERE f.personSurname IS NULL")
  fun findAllWherePersonSurnameIsNull(pageable: Pageable): Page<FacilitatorEntity>
}
