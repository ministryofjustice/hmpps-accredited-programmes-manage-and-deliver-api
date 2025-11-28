package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

fun String.reverseNameOrder(): String {
  val parts: List<String> = this.trim().split(Regex("\\s+"))

  return when (parts.size) {
    0 -> ""
    1 -> parts[0]
    2 -> "${parts[1]} ${parts[0]}"
    else -> {
      val first = parts.first()
      val last = parts.last()
      val middle = parts.subList(1, parts.size - 1).joinToString(" ")
      "$last $middle $first"
    }
  }
}
