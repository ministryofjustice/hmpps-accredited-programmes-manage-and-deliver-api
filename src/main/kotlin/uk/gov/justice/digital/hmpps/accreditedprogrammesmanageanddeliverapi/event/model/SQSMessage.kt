package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.model

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

/**
 * This data class models the structure of messages sent through AWS Simple Queue Service (SQS),
 * including the message ID, content, and associated attributes. The class is designed to work
 * with Jackson JSON deserialization and ignores unknown properties for forward compatibility.
 *
 * The class provides convenient access to common message attributes, particularly the event type
 * which is frequently used for message routing and processing.
 *
 * @param messageId Unique identifier for the SQS message, assigned by AWS SQS
 * @param message The actual message body content as a string
 * @param attributes Collection of message attributes that provide metadata about the message
 *
 * @see MessageAttributes
 * @see MessageAttribute
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SQSMessage(
  @JsonProperty("MessageId") val messageId: UUID,
  @JsonProperty("Message") val message: String,
  @JsonProperty("MessageAttributes") val attributes: MessageAttributes = MessageAttributes(),
) {
  /**
   * Convenience property to access the event type from message attributes.
   *
   * @return The event type value if present in the message attributes, null otherwise
   */
  val eventType: String? @JsonIgnore get() = attributes["eventType"]?.value
}

/**
 * This class wraps a mutable map of message attributes and provides convenient constructors
 * for common use cases. It implements MutableMap through delegation to allow direct map
 * operations while providing additional functionality specific to SQS message attributes.
 *
 * The class uses Jackson annotations to handle dynamic JSON properties, allowing it to
 * serialize and deserialize arbitrary attribute names.
 *
 * @param attributes Internal map storing the message attributes
 *
 * @see MessageAttribute
 */
data class MessageAttributes(
  @JsonAnyGetter @JsonAnySetter
  private val attributes: MutableMap<String, MessageAttribute> = mutableMapOf(),
) : MutableMap<String, MessageAttribute> by attributes {

  /**
   * Creates a MessageAttributes instance with a predefined eventType attribute.
   * This constructor is typically used when creating messages programmatically.
   *
   * @param eventType The type of event this message represents
   */
  constructor(eventType: String) : this(
    mutableMapOf(
      "eventType" to MessageAttribute(
        "String",
        eventType,
      ),
    ),
  )

  /**
   * Retrieves a message attribute by key.
   *
   * @param key The attribute name to look up
   * @return The MessageAttribute if found, null otherwise
   */
  override operator fun get(key: String): MessageAttribute? = attributes[key]
}

/**
 * AWS SQS message attributes consist of a data type and a value. This class models
 * that structure to support proper serialization and deserialization of SQS messages.
 *
 * Example from a typical SQS message:
 *  * ```json
 *  * "eventType": {
 *  *   "Type": "String",
 *  *   "Value": "interventions.community-referral.created"
 *  * }
 *
 * @param type The data type of the attribute value (e.g., "String", "Number", "Binary")
 * @param value The actual attribute value as a string
 *
 * @see <a href="https://docs.aws.amazon.com/AWSSimpleQueueService/latest/APIReference/API_MessageAttributeValue.html">AWS SQS MessageAttributeValue</a>
 */
data class MessageAttribute(
  @JsonProperty("Type") val type: String,
  @JsonProperty("Value") val value: String,
)
