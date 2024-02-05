# Num Aggregator

### Requirements
- Java 21
- Docker

### Design consideration
- Way to call the endpoint is similar to how it is mentioned in task documentation.
- Limit(=10) added over the number of endpoints supplied in the request to prevent from attacks.
- The timeout limit(500ms) is achieved using threadpool invokeAll timeout, and is configurable in application.yml

### Design Flow
- AggregatorController receives request.
- Calls AggregatorService
  - calls EndpointCaller. This maintains a thread pool and calls EndpointClient(makes rest calls) for individual endpoints.
  1. In case endpoint calls timeout, the endpoint is sent asynchronously to retry mechanism, which then sends response to cache.
  2. In case endpoint calls timeout, gets the numbers from already present in cache.
- Calls EndpointResponseCombinator to remove duplicates and sort the numbers received

### Missing items
- ClientCallException needs to be more matured
- Retry of timed out endpoints is made part of cache at the moment for simplicity, but should be kept in a separate class where more specialized retry mechanism can be implemented.
- Integration test misses the test for caching functionality due to wiremock not supporting controlled variable delays.

### Steps to run
- run command `docker-compose up -d`(starts up wiremock, used in tests)
- Build application using command
  `./mvnw clean install`
- Go to target directory by running command `cd target`
- run `java --enable-preview -jar num-aggregator-1.0-SNAPSHOT.jar`
