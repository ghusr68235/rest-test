package com.example.resttest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Main entry point for application
 */
@SpringBootApplication
public class RestTestApplication {
	private static final Logger LOG = LoggerFactory.getLogger(RestTestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(RestTestApplication.class, args);
	}

	/**
	 * Entry point for running application on command line
	 * 
	 * @param jobService   service to make API calls
	 * @param requestCount configurable number of calls to make
	 * @return logic to execute
	 */
	@Bean
	public CommandLineRunner run(JobService jobService, @Value("${request-count:1}") int requestCount) {
		return args -> {
			List<String> jobIds = jobService.requestJobIds(requestCount);
			writeOutput(jobIds, System.out);
		};
	}

	/**
	 * Format job IDs and print to specified output stream
	 * 
	 * @param jobIds    list of job IDs
	 * @param outStream output stream to print to
	 */
	void writeOutput(List<String> jobIds, OutputStream outStream) {
		Output out = new Output(jobIds);
		
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print
		try {
			objectMapper.writeValue(outStream, out);
		} catch (IOException e) {
			LOG.error("Failed to write output", e);
		}
	}
}
