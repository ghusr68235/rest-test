# RestTest
Java Spring application that issues requests to a single REST endpoint and prints the retrieved data to the console
> NOTE: Example commands below assume you are running on Windows. If running on Linux, change `gradlew` to`.\gradle`

## Prerequisites
* git client
  * https://git-scm.com/download/win
* Java 17 SDK
  * https://www.oracle.com/java/technologies/downloads/#java17
## Building
```
git clone https://github.com/ghusr68235/rest-test.git
cd rest-test
gradlew build
```
## Running Tests
```
gradlew test
```
## Running the Application
```
gradlew bootRun --args="--url=http://www.example.com --request-count=30 --concurrency=5"
```
Alternatively, you may run the built jar directly
```
java -jar build\libs\resttest-1.0.0.jar --url=http://www.example.com --request-count=30 --concurrency=5
```
### Parameters

| Parameter | Description | Example Value |
| --------- | ----------- | ------------- |
| `url` | Target server to query | http://www.example.com |
| `request-count` | How many total calls to make to the REST endpoint  | 100 |
| `timeout-seconds` | How long to wait (in seconds) until considering a REST call timed out | 30 |
| `concurrency` | Approximately how many concurrent REST calls to have open at a given time | 10 |
### Example Output
```
{
  "jobs" : [ "141ae9c2-f11a-4d85-acd4-20aa6dc8b990", "141ae9c2-f11a-4d85-acd4-20aa6dc8b990", "141ae9c2-f11a-4d85-acd4-20aa6dc8b990" ]
}
```

## Troubleshooting
* If you see unexpected output when running the application, examine `resttest.log` to see if any errors occurred
