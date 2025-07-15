package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.domain.Page
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity

data class ReferralCaseListItem(
  val crn: String,
  val personName: String,
  val referralStatus: String,
)

fun ReferralCaseListItemViewEntity.toApi() = ReferralCaseListItem(
  crn = crn,
  personName = personName,
  referralStatus = status,
)

/**
 *
 * @param content
 * @param last
 * @param totalPages
 * @param totalElements
 * @param first
 * @param sort
 * @param numberOfElements
 * @param empty
 */
data class PagedReferralCaseListItem(
  @get:JsonProperty("content") val content: List<ReferralCaseListItem>? = null,
  @get:JsonProperty("totalPages") val totalPages: Int,
  @get:JsonProperty("totalElements") val totalElements: Long,
  @get:JsonProperty("pageSize") val pageSize: Int,
  @get:JsonProperty("pageNumber") val pageNumber: Int,
  @get:JsonProperty("first") val first: Boolean,
  @get:JsonProperty("last") val last: Boolean,
  @get:JsonProperty("numberOfElements") val numberOfElements: Int,
  @get:JsonProperty("empty") val empty: Boolean,
)

fun Page<ReferralCaseListItem>.toPagedReferralCaseListItem() = PagedReferralCaseListItem(
  content = content,
  totalPages = totalPages,
  totalElements = totalElements,
  pageSize = pageable.pageSize,
  pageNumber = pageable.pageNumber,
  last = isLast,
  first = isFirst,
  numberOfElements = numberOfElements,
  empty = isEmpty,
)
