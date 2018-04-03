package com.yeti.mail.utility;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.yeti.model.action.Email;

public class MessageToEmailConverter {

	private static final Logger log = LoggerFactory.getLogger(MessageToEmailConverter.class);
	
	public static Email convertMessageToEmail( Message rawMessage ) {
    	Email email = new Email();
    	try {
	    	email.setExternalId(rawMessage.getId());
	
	    	email.setThreadId(rawMessage.getThreadId());
	    	email.setHistoryId(rawMessage.getHistoryId());
	    	email.setOwnerId(new Integer(1));
	    	
	    	
	    	email.setLastRetrievedDate(new Date());
	    	email.setEmailDetails(rawMessage.toPrettyString());
	
	    	List<MessagePartHeader> headers = rawMessage.getPayload().getHeaders();
	    	String to = null, from = null, subject = null, xReceived = null;
	    	
	    	Optional<MessagePartHeader> toValue = headers
	            .stream()
	            .filter(a -> a.getName().equals("To"))
	            .findFirst();
	    	if( toValue.isPresent() ) {
	    		to = toValue.get().getValue();
	    	}
	    	Optional<MessagePartHeader> fromValue = headers
	                .stream()
	                .filter(a -> a.getName().equals("From"))
	                .findFirst();
	    	if( fromValue.isPresent() ) {
	    		from = fromValue.get().getValue();
	    	}
	    	Optional<MessagePartHeader> subjectValue = headers
	                .stream()
	                .filter(a -> a.getName().equals("Subject"))
	                .findFirst();
	    	if( subjectValue.isPresent() ) {
	    		subject = subjectValue.get().getValue();
	    	}
	
	    	Optional<MessagePartHeader> xReceivedValue = headers
	                .stream()
	                .filter(a -> a.getName().equals("X-Received"))
	                .findFirst();
	    	if( xReceivedValue.isPresent() ) {
	    		xReceived = xReceivedValue.get().getValue();
	    	}
	
	    	log.debug("email to : " + to);
	    	log.debug("email from : " + from);
	    	log.debug("email subject : " + subject);
	    	log.debug("email xReceived : " + xReceived);
	    	
	    	
	    	if( rawMessage.getSnippet().indexOf(" ------ Original Message") > -1 ) {
		    	email.setDescription( rawMessage.getSnippet().substring(0, rawMessage.getSnippet().indexOf(" ------ Original Message")) );
	    	} else {
		    	email.setDescription( rawMessage.getSnippet() );
	    	}
	    	email.setInstanceDate(new Date(rawMessage.getInternalDate()));
	    	
	    	email.setName(subject);
	    	email.setActive(true);
	    	/*
	    	if( rawMessage.getPayload() != null ) {
		    	for( MessagePartHeader header : rawMessage.getPayload().getHeaders() ) {
		    		log.debug("----------------------------  header name|value : " + header.getName() + "|" + header.getValue());
		    	}
	    	}
	    	*/
	    	log.debug("email externalId : " + email.getExternalId());
	    	log.debug("email threadId : " + email.getThreadId());
	    	log.debug("email historyId : " + email.getHistoryId());
	    	log.debug("email lastRetrievedDate : " + email.getLastRetrievedDate());
	    	log.debug("email getInstanceDate : " + email.getInstanceDate());
	    	//log.debug("email lastRetrievedValiue : " + email.getEmailDetails());
	    	log.debug("email description : " + email.getDescription());
	    	log.debug("******************************************************************************************");
    	} catch( Exception e ) {
			log.error(e.getMessage(),e);
    		log.debug("++++++++++++++++++++++++++++++++++++++" + e.getMessage());
    		email = null;
    	}
    	return email;
	}
	
}
