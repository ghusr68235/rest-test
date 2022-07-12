package com.example.resttest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.junit.jupiter.api.Test;

public class RestTestApplicationTest {
	/**
	 * Verify output is formated as expected
	 */
	@Test
	void testOutputFormatting() {
		List<String> jobIds = List.of(
				"7a09911f-8663-4170-9bc3-7b0cf83e1d82",
				"74989807-ae77-4927-912f-0627fd075385",
				"5c46c87b-c347-40a5-93df-1f04854c97d2");
		
		RestTestApplication app = new RestTestApplication();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		app.writeOutput(jobIds, buffer);
		
		String expected = """
				{
					"jobs" : [ "7a09911f-8663-4170-9bc3-7b0cf83e1d82", "74989807-ae77-4927-912f-0627fd075385", "5c46c87b-c347-40a5-93df-1f04854c97d2" ]
				}"""
				.replaceAll("\\s", "");
		// Verify output is expected JSON format while ignoring whitespace
		assertEquals(expected, buffer.toString().replaceAll("\\s", ""));
	}
}
