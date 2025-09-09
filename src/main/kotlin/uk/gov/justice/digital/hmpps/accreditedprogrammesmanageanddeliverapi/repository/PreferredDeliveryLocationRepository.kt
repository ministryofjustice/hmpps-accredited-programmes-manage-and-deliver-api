package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.repository.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.PreferredDeliveryLocation
import java.util.UUID

interface PreferredDeliveryLocationRepository : Repository<PreferredDeliveryLocation, UUID>
