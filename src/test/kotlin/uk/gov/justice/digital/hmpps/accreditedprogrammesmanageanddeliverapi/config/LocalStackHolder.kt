package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config

import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName

object LocalStackHolder {

  val container: LocalStackContainer =
    LocalStackContainer(DockerImageName.parse("localstack/localstack"))
      .withEnv("DEFAULT_REGION", "eu-west-2")
      .withServices(LocalStackContainer.Service.SNS, LocalStackContainer.Service.SQS)
      .apply {
        start()
      }
}
