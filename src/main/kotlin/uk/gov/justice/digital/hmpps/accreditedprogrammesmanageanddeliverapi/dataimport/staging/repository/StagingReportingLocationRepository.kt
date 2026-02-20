package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dataimport.staging.entity.StagingReportingLocationEntity

/**
 * Repository for accessing staging reporting location data from the im_data_import schema.
 */
interface StagingReportingLocationRepository : JpaRepository<StagingReportingLocationEntity, String>
