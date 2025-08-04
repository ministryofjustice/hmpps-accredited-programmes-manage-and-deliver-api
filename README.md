# HMPPS Accredited Programmes Manage and Deliver API

[![repo standards badge](https://img.shields.io/badge/endpoint.svg?&style=flat&logo=github&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-accredited-programmes-manage-and-deliver-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-report/hmpps-accredited-programmes-manage-and-deliver-api "Link to report")
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-accredited-programmes-manage-and-deliver-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://accredited-programmes-manage-and-deliver-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)

This repository contains the service code for the `HMPPS Accredited Programmes Manage and Deliver API`.

## Architecture

This is a Spring Boot service, written in Kotlin, which provides an API for managing and delivering accredited
programmes within HMPPS.

## Required software

Most software can be installed using [homebrew](https://brew.sh/).

* Docker
* Java SDK (OpenJDK 21)
* Kotlin

## Tech stack

- **Language**: Kotlin
- **Framework**: Spring Boot
- **Database**: PostgreSQL with Flyway migrations
- **Message Queue**: AWS SQS via Spring Cloud AWS
- **Authentication**: OAuth 2.0 via HMPPS Auth
- **API Documentation**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, Testcontainers, WireMock
- **Build**: Gradle
- **Containerization**: Docker

## Running the application locally

The application comes with a `local` spring profile that includes default settings for running locally. This is not
necessary when deploying to kubernetes as these values are included in the helm configuration templates -
e.g. `values-dev.yaml`. This run configuration is included in the
project [local run configuration](./.run/Local%20-%20Run.run.xml)
and can be accessed using the run options in the top right of the window in IntelliJ.

There is also a `docker-compose.yml` that can be used to run a local instance of the application in docker and also an
instance of HMPPS Auth.

Run the following command to pull the relevant dependencies for the project.

```bash
docker-compose pull
```

and then the following command to run the containers.

```bash
docker-compose up
```

can optionally be run in detached mode in order to retain terminal use

```bash
docker-compose up -d
```

### Connecting to local database

The service uses a postgres database alongside flyway migrations to create and populate the database. To connect to the
database locally in your preferred database
client ([IntelliJ Ultimate](https://www.jetbrains.com/help/idea/database-tool-window.html), [Dbeaver](https://dbeaver.io/),
[Pgadmin](https://www.pgadmin.org/), etc).

Create new connection using local database credentials;

| Variable | Value          |
|----------|----------------|
| Port     | 5432           |
| Username | admin          |
| Password | admin_password |

### Events

The application listens to events that are published on the `HMPPS_DOMAIN_EVENTS_QUEUE`. This functionality is
replicated locally using localstack. If you wish to view and create messages/queues/etc then it is recommended to use
the `awslocal` wrapper which can be installed using

```zsh
brew install awscli-local
```

You can view the created queue by running:

```zsh
awslocal sqs list-queues
```

To send test events to our queues we can run the following command:

```zsh
awslocal sqs send-message --queue-url http://sqs.eu-west-2.localhost.localstack.cloud:4566/000000000000/hmpps_domain_events_queue --message-body file://src/test/resources/events/interventions/communityReferralCreatedEvent.json
```

### Authorization

The service uses an OAuth 2.0 setup managed through the HMPPS Auth project. To call any endpoints locally a bearer token
must be generated. This can be done through calling the auth endpoint in the HMPPS-auth service.

| Variable         | Value                                                     |
|------------------|-----------------------------------------------------------|
| Grant type       | Client credentials                                        |
| Access token URL | http://hmpps-auth:9090/auth/oauth/token                   |
| Client ID        | hmpps-accredited-programmes-manage-and-deliver-api-client |
| Client Secret    | clientsecret                                              |
| Scope            | Read                                                      |

The values for `ClientId` and `Client Secret` are the local values. These are the same credentials that the UI uses to
call this service.

## Health

- `/health/ping`: will respond `pong` to all requests. This should be used by dependent systems to check connectivity to
  the service.
- `/health`: provides information about the application health and its dependencies. This should only be used by
  hmpps-accredited-programmes-manage-and-deliver-api health monitoring (e.g. pager duty) and not other systems who wish
  to find out the state of the service.
- `/info`: provides information about the version of the deployed application.

## Testing

Run the tests with:

```bash
./gradlew test
```

### Integration tests

Integration tests use Testcontainers to spin up test versions of dependent services (PostgreSQL, localstack for SQS).

### Test coverage

Test coverage reports are enabled by default and after running the tests the report will be written to
`build/reports/jacoco/test/html`.

## Branch naming validator

This project has a branch naming validator in place in the GitHub action pipeline.

To ensure these pipelines pass the branch name must conform one of the following patterns:

* APG-[0-9]/*branch-name-here*
* FRI-[0-9]/*branch-name-here*
* no-ticket/*branch-name-here*
* renovate/*branch-name-here*
* hotfix/*branch-name-here*

## Tracing

This application is configured to send tracing data to Application Insights. When running locally, tracing data will be
sent to the `local` Application Insights instance if the `APPLICATIONINSIGHTS_CONNECTION_STRING` environment variable is
set.

## Alerting

Custom alerts are configured
via [hmpps-helm-charts](https://github.com/ministryofjustice/hmpps-helm-charts/tree/main/charts/generic-service). Alerts
are configured to be sent to the `#accredited-programmes-alerts` Slack channel.

## Monitoring

Application performance monitoring is provided by Application Insights. Logs are shipped to Azure Log Analytics.

## Database

This application uses PostgreSQL for data persistence. Database migrations are managed using Flyway.

### Database migrations

Database migrations are automatically applied on application startup via Flyway. Migration files are located in
`src/main/resources/db/migration/`.

## Formatting

This project is formatted using ktlint.

Run the following command to add a pre-commit hook to ensure your code is formatted correctly before pushing.

```bash
./gradlew addKtlintFormatGitPreCommitHook
```

## Configuration

Configuration is provided via environment variables and spring configuration files. Key configuration areas include:

- **Database**: Connection details for PostgreSQL
- **SQS**: Queue configuration for domain events
- **OAuth**: Client credentials for HMPPS Auth integration
- **API clients**: Configuration for downstream API calls

## API Documentation

Interactive API documentation is available via Swagger UI:

- **Dev
  **: [API Documentation](https://accredited-programmes-manage-and-deliver-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)
- **Preprod
  **: [API Documentation](https://accredited-programmes-manage-and-deliver-api-preprod.hmpps.service.justice.gov.uk/swagger-ui/index.html)

## Dependencies

This service integrates with several other HMPPS services:

- **HMPPS Auth**: For authentication and authorization
- **Probation Integration - nDelius**: For person information in the community
- **Probation Integration - OaSys**: For prison and prisoner data in custody
- **Domain Events**: For publishing and consuming business events

## Common Kotlin patterns

Many patterns have evolved for HMPPS Kotlin applications. Using these patterns provides consistency across our suite of
Kotlin microservices and allows you to concentrate on building your business needs rather than reinventing the
technical approach.

Documentation for these patterns can be found in
the [HMPPS tech docs](https://tech-docs.hmpps.service.justice.gov.uk/common-kotlin-patterns/).
If this documentation is incorrect or needs improving please report
to [#ask-prisons-digital-sre](https://moj.enterprise.slack.com/archives/C06MWP0UKDE)
or [raise a PR](https://github.com/ministryofjustice/hmpps-tech-docs).

## Deployments

Deployments are part of our CI process, on the `main` branch using GitHub Actions workflows.

Deployments require a manual approval step for production environments.

### Kubernetes

All deployments and environments are managed through Kubernetes.

For information on how to connect to the Cloud Platform's Kubernetes cluster follow the setup
guide [here](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/kubectl-config.html#connecting-to-the-cloud-platform-39-s-kubernetes-cluster).

For further Kubernetes commands a useful cheat sheet is
provided [here](https://kubernetes.io/docs/reference/kubectl/quick-reference/). Similarly, the `--help` flag on any
`kubectl` command will give you more information.

### Testing a Deployment

#### tl;dr

```zsh
kubectl get deployments -n $NAMESPACE
```

```zsh
kubectl get pods -n $NAMESPACE
```

```zsh
kubectl logs $POD_NAME -n $NAMESPACE
```

#### 1. Find the deployments in the namespace:

```zsh
$ kubectl get deployments -n hmpps-accredited-programmes-manage-and-deliver-dev

NAME                                                    READY   UP-TO-DATE   AVAILABLE   AGE
hmpps-accredited-programmes-manage-and-deliver-api      2/2     2            2           41d
```

#### 2. Double-check the Pod(s) associated with the Deployment:

Per [Kubernetes' docs](https://kubernetes.io/docs/concepts/workloads/pods/):

> A Pod is similar to a set of containers with shared namespaces and shared filesystem volumes.

View the Pods in the namespace, these are what the `READY` column in the `get deployments` refer to:

```zsh
$ kubectl get pods -n hmpps-accredited-programmes-manage-and-deliver-prod

NAME                                                                     READY   STATUS    RESTARTS   AGE
hmpps-accredited-programmes-manage-and-deliver-api-58bb6f56b4-7q566      1/1     Running   0          35m
hmpps-accredited-programmes-manage-and-deliver-api-58bb6f56b4-kzqsn      1/1     Running   0          35m
```

#### 3. Check the logs of a Pod

It is possible to read the logs of a given Pod to check that the build and spin-up process for the Pod has been
successful.

To view the logs from any of the Pods whose name is given in the previous responses:

```zsh
$ kubectl logs $POD_NAME --$NAMESPACE hmpps-accredited-programmes-manage-and-deliver-dev
```

Where `$POD_NAME` is the full string Pod name given in the `get pods` response.
AND `$NAMESPACE` is the namespace which you are running the command against

## Connecting Postman to Test Environments

There is a guide on how to configure your postman client to be able to call endpoints in the Test environments (
Dev) [here](https://dsdmoj.atlassian.net/wiki/spaces/IC/pages/5784142235/Connecting+Postman+to+test+environments).

## Security

Our security policy is
located [here](https://github.com/ministryofjustice/hmpps-accredited-programmes-manage-and-deliver-api/security/policy).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For project-specific questions, please reach out to the Accredited Programmes team in `#accredited-programmes-dev`.
Please raise any questions or queries there. Contributions welcome!

