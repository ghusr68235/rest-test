package com.example.resttest;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Integration-level tests for JobService that set up a fake web server with
 * mocked responses to to run API calls against
 */
public class JobServiceIntTest {
	private MockWebServer server;
	private String baseUrl;
	private JobService jobService;

	@BeforeEach
	void setUp() throws IOException {
		server = new MockWebServer();
		server.start();
		this.baseUrl = String.format("http://localhost:%s", server.getPort());
		this.jobService = new JobService(baseUrl, 30, 30);
	}

	/**
	 * Verify all job IDs are retrieved and match expected values. Use set to check
	 * returned IDs since order not guaranteed
	 * 
	 * @throws JsonProcessingException
	 */
	@Test
	void testRequestJobIdsReturnsExpectedIds() throws JsonProcessingException {
		List<JobDetails> mockedJobs = this.mockResponses(50, 0);
		Set<String> expectedIds = new HashSet<>();
		for (JobDetails job : mockedJobs) {
			expectedIds.add(job.jobId);
		}

		List<String> jobIds = jobService.requestJobIds(50);

		assertEquals(expectedIds, new HashSet<String>(jobIds));
	}

	/**
	 * Verify concurrent processing works as expected - that requests with delays
	 * aren't being processed one at a time
	 * 
	 * @throws JsonProcessingException
	 */
	@Test
	void testRequestsProcessedConcurrently() throws JsonProcessingException {
		this.jobService = new JobService(this.baseUrl, 30, 5);
		this.mockResponses(15, 1);

		long start = System.currentTimeMillis();
		jobService.requestJobIds(15);
		float duration = (System.currentTimeMillis() - start) / 1000;

		// Given a concurrency of 5, processing 15 jobs with a 1 second delay should
		// only take ~3 seconds. Test against 6 to leave buffer for other overhead
		assertTrue(duration < 6);
	}

	/**
	 * Verify long running HTTP requests are timed out and do not block processing
	 * 
	 * @throws JsonProcessingException
	 */
	@Test
	void testRequestTakingTooLongDoesntBlock() throws JsonProcessingException {
		// Service with 1 second timeout
		this.jobService = new JobService(this.baseUrl, 1, 1);
		// Response with long delay
		this.mockResponses(1, 30);

		long start = System.currentTimeMillis();
		jobService.requestJobIds(1);
		float duration = (System.currentTimeMillis() - start) / 1000;

		assertTrue(duration < 3);
	}

	/**
	 * Verify malformed HTTP responses do not block other processing
	 * 
	 * @throws JsonProcessingException
	 */
	@Test
	void testMalformedDataDoesntBlockProcessing() throws JsonProcessingException {
		JobDetails jobDetail = new JobDetails(UUID.randomUUID().toString());
		JobDetails jobDetail2 = new JobDetails(UUID.randomUUID().toString());

		ObjectMapper objectMapper = new ObjectMapper();
		server.enqueue(new MockResponse().setBody(objectMapper.writeValueAsString(jobDetail))
				.addHeader("Content-Type", "application/json"));
		server.enqueue(new MockResponse().setBody("this is bad data"));
		server.enqueue(new MockResponse().setBody(objectMapper.writeValueAsString(jobDetail2))
				.addHeader("Content-Type", "application/json"));

		List<String> jobIds = jobService.requestJobIds(3);
		assertEquals(2, jobIds.size());
		assertTrue(jobIds.contains(jobDetail.jobId));
		assertTrue(jobIds.contains(jobDetail2.jobId));
	}

	/**
	 * Mock a certain number of responses and return the list of mocked job details
	 * 
	 * @param count how many responses to mock
	 * @param delay number of seconds each response takes to return; if 0, no delay
	 * @return list of mocked job details
	 * @throws JsonProcessingException
	 */
	private List<JobDetails> mockResponses(int count, long delay) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		List<JobDetails> jobList = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			JobDetails jobDetail = new JobDetails(UUID.randomUUID().toString());
			jobList.add(jobDetail);

			MockResponse mockResponse = new MockResponse().setBody(objectMapper.writeValueAsString(jobDetail))
					.addHeader("Content-Type", "application/json");
			if (delay > 0) {
				mockResponse.setBodyDelay(delay, TimeUnit.SECONDS);
			}
			server.enqueue(mockResponse);
		}

		return jobList;
	}

	@AfterEach
	void tearDown() throws IOException {
		try {
			server.shutdown();
		} catch (IOException e) {
			// Workaround to handle timeout test that kills live HTTP connection
			if (!"Gave up waiting for queue to shut down".equals(e.getMessage())) {
				throw e;
			}
		}
	}
}
