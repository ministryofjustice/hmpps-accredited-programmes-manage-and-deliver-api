package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

/**
 * Recognised lower-case surname particles ("nobiliary particles"). When one of
 * these appears immediately before the last uppercase-leading token, it is
 * treated as part of the surname (e.g. `van der Berg` -> `van der Berg`).
 *
 * Case-insensitive match. Kept small and explicit rather than "any lowercase
 * token" to avoid false positives on data-entry mistakes such as a lower-cased
 * forename ("joe Bloggs" -> still returns `Bloggs`).
 *
 * The set covers Dutch (`van`, `van der`, `ten`, `ter`), German (`von`, `zu`),
 * French (`de`, `du`, `la`, `le`), Spanish/Portuguese (`de`, `del`, `de la`,
 * `da`, `dos`), Italian (`di`, `della`), Arabic transliteration (`al`, `el`,
 * `bin`, `ibn`) and Scandinavian (`af`). Extend the set as new real-world
 * cases surface — the change is a one-liner.
 *
 * See `docs/APG-2580-sar-recorded-by-surname-only.md` for the audit that
 * scoped the reviewer's request to two SAR fields.
 */
private val SURNAME_PARTICLES: Set<String> = setOf(
  "van", "von", "der", "den", "de", "del", "della", "di", "da", "dos",
  "du", "la", "le", "ten", "ter", "zu", "af", "bin", "ibn", "al", "el",
)

/**
 * Returns the surname portion of a full name for SAR output, complying with
 * the data-dictionary rule agreed in the round-2 QAT review (Sep 2026):
 * "surname only" with compound surnames such as `van der Berg` preserved.
 *
 * Rules:
 *  * `null` / blank input -> `null`
 *  * single-token input -> returned unchanged (already a surname)
 *  * multi-token input -> last token, plus any preceding tokens that match
 *    [SURNAME_PARTICLES] (case-insensitive)
 *  * casing preserved
 *  * leading / trailing / repeated whitespace tolerated
 *
 * Examples:
 * ```
 * toSurname("Joe Bloggs")         -> "Bloggs"
 * toSurname("John van der Berg")  -> "van der Berg"
 * toSurname("Maria de la Cruz")   -> "de la Cruz"
 * toSurname("Anne-Marie O'Brien") -> "O'Brien"
 * toSurname("Bloggs")             -> "Bloggs"
 * toSurname(null)                 -> null
 * toSurname("   ")                -> null
 * ```
 */
fun toSurname(fullName: String?): String? {
  val trimmed = fullName?.trim().orEmpty()
  if (trimmed.isEmpty()) return null
  val tokens = trimmed.split(Regex("\\s+"))
  if (tokens.size == 1) return tokens[0]
  var startIndex = tokens.lastIndex
  while (startIndex > 0 && tokens[startIndex - 1].lowercase() in SURNAME_PARTICLES) {
    startIndex--
  }
  return tokens.subList(startIndex, tokens.size).joinToString(" ")
}
