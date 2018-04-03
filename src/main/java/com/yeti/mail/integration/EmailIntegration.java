package com.yeti.mail.integration;

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
import com.yeti.model.action.Email;

import com.yeti.mail.utility.MessageToEmailConverter;

@Service
public class EmailIntegration {

	@Autowired
	private EmailRepository emailRepository;
	
	private static final Logger log = LoggerFactory.getLogger(EmailIntegration.class);
/*
    public boolean addEmail( Message rawMessage ) { 
    	boolean updated = false;
    	try {
	    	Email email = MessageToEmailConverter.convertMessageToEmail(rawMessage);
	    	emailRepository.save(email);
	    	updated = true;
    	} catch( Exception e ) {
    		e.printStackTrace();
    	}
    	return updated;
    }
*/
}
