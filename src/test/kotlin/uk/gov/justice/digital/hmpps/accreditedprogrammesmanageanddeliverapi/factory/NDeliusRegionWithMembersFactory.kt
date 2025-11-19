package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.FullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegionWithMembers
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomFullName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString

class NDeliusRegionWithMembersFactory {
  fun produce(
    code: String? = null,
    description: String? = null,
    pdus: List<NDeliusRegionWithMembers.NDeliusPduWithTeam>? = null,
  ): NDeliusRegionWithMembers = NDeliusRegionWithMembers(
    code = code ?: randomUppercaseString(2),
    description = description ?: randomSentence(1..2),
    pdus = pdus ?: listOf(NDeliusPduWithTeamFactory().produce()),
  )
}

class NDeliusPduWithTeamFactory {
  fun produce(
    code: String? = null,
    description: String? = null,
    team: List<NDeliusRegionWithMembers.NDeliusUserTeamWithMembers>? = null,
  ): NDeliusRegionWithMembers.NDeliusPduWithTeam = NDeliusRegionWithMembers.NDeliusPduWithTeam(
    code = code ?: randomUppercaseString(2),
    description = description ?: randomSentence(1..2),
    team = team ?: listOf(NDeliusUserTeamWithMembersFactory().produce()),
  )
}

class NDeliusUserTeamWithMembersFactory {
  fun produce(
    code: String? = null,
    description: String? = null,
    members: List<NDeliusRegionWithMembers.NDeliusUserTeamMembers>? = null,
  ): NDeliusRegionWithMembers.NDeliusUserTeamWithMembers = NDeliusRegionWithMembers.NDeliusUserTeamWithMembers(
    code = code ?: randomUppercaseString(2),
    description = description ?: randomSentence(1..2),
    members = members ?: listOf(NDeliusUserTeamMembersFactory().produce()),
  )
}

class NDeliusUserTeamMembersFactory {
  fun produce(
    code: String? = null,
    name: FullName? = null,
  ): NDeliusRegionWithMembers.NDeliusUserTeamMembers = NDeliusRegionWithMembers.NDeliusUserTeamMembers(
    code = code ?: randomUppercaseString(2),
    name = name ?: randomFullName(),
  )
}
