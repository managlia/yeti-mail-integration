package com.yeti.mail.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/Test", produces = "application/hal+json")
public class TestController {

	private final String RESPONSE_VALUE = "Connection established";
	
    private static final Logger log = LoggerFactory.getLogger(TestController.class);
	
	@GetMapping
	public String completeGetTest() {
		log.debug(RESPONSE_VALUE);
		return RESPONSE_VALUE;
	}
	
	@GetMapping("/{id}")
	public String completeGetTestWithId(@PathVariable String id) {
		String valueWithId =  RESPONSE_VALUE + id;
		log.debug(valueWithId);
		return valueWithId;
	}
	
}








