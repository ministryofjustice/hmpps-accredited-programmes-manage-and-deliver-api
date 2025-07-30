# WireMock for Local Development

This directory contains WireMock stub mappings for mocking external services when running the application with the local profile.

## Overview

When running the application with the local profile, it is configured to connect to external services at `http://localhost:8097`. The docker-compose.yml file includes a WireMock service that runs on this port and serves mock responses for these external services.

## Included Stubs

The following external services are mocked:

### OasysApi
- `GET /assessments/pni/{crn}?community=true` - Returns PNI assessment data
  - Success response for any CRN
  - 404 response for CRNs starting with "UNKNOWN"

### FindAndReferInterventionApi
- `GET /referral/{referralId}` - Returns referral details
  - Success response for any valid UUID
  - 404 response for referral ID "00000000-0000-0000-0000-000000000000"

### NDeliusIntegrationApi
- `GET /case/{crn}/personal-details` - Returns offender identifiers
  - Success response for any CRN
  - 404 response for CRNs starting with "UNKNOWN"
- `POST /users/{username}/access` - Checks user access to offenders
  - Success response for any username
  - 403 response for username "restricted.user"

### HMPPS Auth
- `POST /auth/oauth/token` - Returns an OAuth token
- `GET /auth/health/ping` - Returns a health check response

## How to Use

1. Start the Docker containers:
   ```
   docker-compose up -d
   ```

2. Run the application with the local profile:
   ```
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```
   
   Or use the IntelliJ run configuration "Local - Run"

3. The application will automatically use the WireMock server for all external API calls.

## Adding New Stubs

To add a new stub:

1. Create a new JSON file in the `mappings` directory
2. Define the request pattern and response using the WireMock JSON format
3. Restart the WireMock container to load the new stub:
   ```
   docker-compose restart wiremock
   ```

## Verifying Stubs

You can verify that the stubs are working by making requests directly to the WireMock server:

```
curl -v http://localhost:8097/auth/health/ping
```

You should see a response with a 200 status code and a JSON body.

## Viewing Requests

WireMock logs all requests it receives. You can view these logs with:

```
docker logs hmpps-mandd-wiremock
```

## Troubleshooting

If the application is not connecting to the WireMock server:

1. Verify that the WireMock container is running:
   ```
   docker ps | grep wiremock
   ```

2. Verify that the stubs are loaded:
   ```
   docker exec hmpps-mandd-wiremock ls -la /home/wiremock/mappings
   ```

3. Test the WireMock server directly:
   ```
   curl -v http://localhost:8097/auth/health/ping
   ```

4. Check the application logs for connection errors.