package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType

@Converter(autoApply = true)
class ReferralStatusConverter : AttributeConverter<ReferralStatusType, String> {

  override fun convertToDatabaseColumn(referralStatusType: ReferralStatusType?): String? {
    if (referralStatusType == null) {
      return null
    }

    return referralStatusType.displayName
  }

  override fun convertToEntityAttribute(displayName: String?): ReferralStatusType? {
    if (displayName == null) {
      return null
    }

    return ReferralStatusType.entries
      .firstOrNull { it.displayName == displayName }
      ?: throw IllegalArgumentException()
  }
}
