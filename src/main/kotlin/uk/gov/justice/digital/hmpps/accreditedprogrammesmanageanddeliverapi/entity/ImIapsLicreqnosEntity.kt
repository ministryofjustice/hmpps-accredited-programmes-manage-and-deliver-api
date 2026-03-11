package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "iaps_licreqnos", schema = "im_data_import")
class ImIapsLicreqnosEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  var id: String? = null,

  @NotNull
  @Column(name = "source_referral_id", nullable = false)
  var sourceReferralId: String,

  @NotNull
  @Column(name = "licreqno", nullable = false)
  var licreqno: String,
)
