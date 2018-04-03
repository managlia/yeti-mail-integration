package com.yeti.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yeti.mail.integration.GmailIntegration;

@Component
public class ScheduledTasks {

	
	@Autowired
	private GmailIntegration gmailIntegrationService;

	
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    
    @Scheduled(fixedRate = 100000)
    public void reportCurrentTime() {
        //log.info(gmailIntegrationService.updateMail());
    }
}
