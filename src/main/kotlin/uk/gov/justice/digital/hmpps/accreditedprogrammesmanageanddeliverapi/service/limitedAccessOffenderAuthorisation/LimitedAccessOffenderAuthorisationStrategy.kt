package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.limitedAccessOffenderAuthorisation

import org.slf4j.LoggerFactory
import org.springframework.util.AntPathMatcher
import java.util.UUID

interface LimitedAccessOffenderAuthorisationStrategy {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    const val REFERRAL_ID_PATH_VARIABLE_NAME = "referralId"
    const val SESSION_ID_PATH_VARIABLE_NAME = "sessionId"
  }

  /**
   * Determines if the given HTTP request method and path are supported by the strategy.
   *
   * @param httpRequestMethod the HTTP method of the request (e.g., "GET", "POST").
   * @param httpRequestPath the URL path of the HTTP request.
   * @return true if the method and path are supported, false otherwise.
   */
  fun isSupportedPath(httpRequestMethod: String, httpRequestPath: String): Boolean

  /**
   * Checks if the given user is authorised to access the resource identified by the specified HTTP request path.
   *
   * @param httpRequestPath the path of the HTTP request to check authorisation for
   * @param username the username of the user whose access is being verified
   * @return true if the user is authorised to access the resource, false otherwise
   */
  fun isAuthorised(httpRequestPath: String, username: String): Boolean

  /**
   * Extracts a UUID from the given HTTP request path by matching it against a specified URI pattern.
   *
   * @param httpRequestPath the path of the HTTP request to extract the UUID from
   * @param idPathVariableName the name of the path variable containing the UUID
   * @param uriPatternAnt the URI pattern to match the request path against, using Ant-style syntax
   * @return the extracted UUID if the path contains a valid UUID, or null if parsing fails
   */
  fun getId(httpRequestPath: String, idPathVariableName: String, uriPatternAnt: String): UUID? {
    var id: UUID? = null
    try {
      val pathMatcher = AntPathMatcher()
      val variables = pathMatcher.extractUriTemplateVariables(uriPatternAnt, httpRequestPath)
      val uuidStr = variables[idPathVariableName]
      if (uuidStr != null) {
        id = UUID.fromString(uuidStr)
      }
    } catch (ex: IllegalArgumentException) {
      log.error("Failed to parse id from path: $httpRequestPath", ex)
    } catch (ex: IllegalStateException) {
      log.error(
        "Failed to parse id from path: $httpRequestPath as idPathVariableName: $idPathVariableName doesn't match",
        ex,
      )
    }

    return id
  }
}
