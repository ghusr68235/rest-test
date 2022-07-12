package com.example.resttest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model for JSON returned from getjobdetails endpoint
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobDetails {
	/**
	 * Job UUID
	 */
	public String jobId;

	/**
	 * Default constructor for deserializer
	 */
	public JobDetails() {
	}

	public JobDetails(String jobId) {
		this.jobId = jobId;
	}
}
