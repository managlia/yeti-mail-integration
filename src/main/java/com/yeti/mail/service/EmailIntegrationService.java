package com.yeti.mail.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.yeti.core.repository.action.EmailRepository;
import com.yeti.mail.integration.GmailIntegration;
import com.yeti.model.action.Email;
import com.yeti.model.util.Batch;

@Service
public class EmailIntegrationService {

	@Autowired
	private GmailIntegration gmailIntegration;
	
	private static final Logger log = LoggerFactory.getLogger(EmailIntegrationService.class);

	public List<Email> getAllEmails() {
		return gmailIntegration.getLatestEmails();
	}

	public Email getEmail(Integer id) {
		// TODO Auto-generated method stub
		// Get a specific email from the email client
		return null;
	}

	public Email sendAnEmail(Email email) {
		// TODO Auto-generated method stub
		log.debug(">>>>>>>>>>>>>>> service triggering SEND in gmailIntegration");
		return gmailIntegration.sendMessage(email);
	}

	public boolean exists(Integer id) {
		// TODO Auto-generated method stub
		return false;
	}

	public Email updateEmail(Integer id, Email email) {
		return email;
		// TODO Auto-generated method stub
		
	}

	public void deleteEmail(Integer id) {
		// TODO Auto-generated method stub
		
	}

	public void processBatch(Batch batch) {
		// TODO Auto-generated method stub
		
	}
	
	
}
