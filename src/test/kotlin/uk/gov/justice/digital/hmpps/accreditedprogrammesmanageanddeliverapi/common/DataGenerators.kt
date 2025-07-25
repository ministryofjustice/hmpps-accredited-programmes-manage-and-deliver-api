package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common

private val upperCase = ('A'..'Z').toList()
private val lowerCase = ('a'..'z').toList()
private val digits = ('0'..'9').toList()

fun Sequence<Char>.asString() = fold(StringBuilder(), StringBuilder::append).toString()
private operator fun Collection<Char>.invoke(n: Int) = generateSequence { random() }.take(n)

private fun space() = sequenceOf(' ')
fun randomWord(length: IntRange) = lowerCase(length.random())
fun randomCapitalisedWord(length: IntRange) = upperCase(1) + lowerCase((length).random() - 1)

fun randomAlphanumericString(length: Int = 6) = (upperCase + lowerCase + digits)(length).asString()
fun randomUppercaseString(length: Int = 6) = upperCase(length).asString()
fun randomLowercaseString(length: Int = 6) = lowerCase(length).asString()
fun randomSentence(wordRange: IntRange = 1..20, wordLength: IntRange = 3..10): String = (sequenceOf(randomCapitalisedWord(wordLength)) + generateSequence { randomWord(wordLength) })
  .take(wordRange.random())
  .reduce { left, right -> left + space() + right }
  .asString()

fun randomEmailAddress() = (lowerCase(5) + ".".asSequence() + lowerCase(8) + "@".asSequence() + lowerCase(6) + ".com".asSequence()).asString()
fun randomPrisonNumber(): String = (upperCase(1) + digits(4) + upperCase(2)).asString()
