package com.example.resttest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service for interacting with Job REST endpoints
 */
@Service
public class JobService {
	private static final Logger LOG = LoggerFactory.getLogger(JobService.class);
	private WebClient webClient;
	private long timeout;
	private int concurrency;

	public JobService(@Value("${url}") String baseUrl,
			@Value("${timeout-seconds:30}") long timeout,
			@Value("${concurrency:30}") int concurrency) {
		LOG.info("Using base URL: {}", baseUrl);
		this.webClient = WebClient.create(baseUrl);
		this.timeout = timeout;
		this.concurrency = concurrency;
	}

	/**
	 * Perform simultaneous API requests to endpoint and retrieve list of job IDs
	 * 
	 * @param jobService  service to make API calls
	 * @param requestCoun how many calls to make
	 * @return list of job IDs
	 */
	public List<String> requestJobIds(int requestCount) {
		LOG.info("Requesting {} job IDs with timeout of {} and concurrency of", requestCount, timeout, concurrency);
		// Pre-generate list of request IDs to use
		List<Integer> requestIds = IntStream.rangeClosed(1, requestCount)
				.boxed().collect(Collectors.toList());
		// Set up stream of API calls but cap concurrency to specified amount
		Flux<JobDetails> jobFlux = Flux.fromIterable(requestIds)
				.flatMap(id -> getJobDetails(id), concurrency);

		// Execute calls and store job IDs. Block until calls are finished
		List<String> jobIds = new ArrayList<>();
		jobFlux.map(job -> job.jobId)
				.doOnNext(jobIds::add)
				.onErrorContinue((ex, o) -> LOG.error("Failed to retrieve job details", ex))
				.blockLast();
		return jobIds;
	}

	/**
	 * Query getjobdetails endpoint and convert JSON response to JobDetails
	 * 
	 * @param id request ID
	 * @return stream for retrieving a single JobDetails
	 */
	private Mono<JobDetails> getJobDetails(int id) {
		LOG.debug("Getting job details for request {}", id);
		return webClient
				.get()
				.uri("/getjobdetails/{id}", id)
				.retrieve()
				.bodyToMono(JobDetails.class)
				.timeout(Duration.ofSeconds(timeout));
	}
}
