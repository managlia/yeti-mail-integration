package com.yeti.mail.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yeti.mail.ScheduledTasks;
import com.yeti.mail.integration.GmailIntegration;

@RestController
public class ManualTriggerController {
	
	@Autowired
	private GmailIntegration gmailIntegration;
	
    private static final Logger log = LoggerFactory.getLogger(ManualTriggerController.class);

	@GetMapping("/")
	public String triggerUpdate() {
		// now this does nothing
        // log.info(gmailIntegration.updateMail());
		return "Manually triggered the mail integration scheduler";
	}

}
