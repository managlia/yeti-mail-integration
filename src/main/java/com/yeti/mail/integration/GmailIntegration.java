package com.yeti.mail.integration;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import com.yeti.core.repository.action.ActionRepository;
import com.yeti.mail.service.EmailIntegrationService;
import com.yeti.mail.utility.EmailToMessageConverter;
import com.yeti.mail.utility.MessageToEmailConverter;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.Gmail.Users.GetProfile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.mail.internet.MimeMessage;

import com.yeti.model.action.Email;
import com.yeti.model.host.User;

import javassist.bytecode.Descriptor.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GmailIntegration {

	private static final Logger log = LoggerFactory.getLogger(GmailIntegration.class);
	
	@Autowired
	private UserIntegration userIntegration;
	
	@Autowired
	private EmailIntegration emailIntegration;
	
	private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/gmail-java-quickstart");
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;
    private static final List<String> SCOPES = Arrays.asList(GmailScopes.MAIL_GOOGLE_COM);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private String tempoaryEmailAddress = "managlia@gmail.com";
    
    
    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
    
    
    public Email sendMessage(Email email) {
    	try {
	    	String userId = "me";
	        Gmail service = getGmailService();
			log.debug(">>>>>>>>>>>>>>> gmailIntegration:: convert email to message for sending dfmdfm");
	    	Message message = 
	    			EmailToMessageConverter.convertEmailToMessage(email, tempoaryEmailAddress);
			log.debug(">>>>>>>>>>>>>>> gmailIntegration:: about to send the email dfmdfm");
			
			message = service.users().messages().send(userId, message).execute();
	        
	        log.debug(">>>>>>>>>>>>>>>>>>>>> SENT Message id: " + message.getId());
	        System.out.println(">>>>>>>>>>>>>>>>>>>>> SENT Message pretty string: " + message.toPrettyString());

	        Message fullMessage = service.users().messages().get(userId, message.getId()).setFormat("FULL").execute();
			log.debug(">>>>>>>>>>>>>>> gmailIntegration:: got the full message dfmdfm");
	        
	        email = MessageToEmailConverter.convertMessageToEmail(fullMessage);
	        
	        return email;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(),e);
    		log.debug("++++++++++++++++++++++++++++++++++++++" + e.getMessage());
    		return null;
		}
    }
    
    

    public List<Email> getLatestEmails() {
    	List<Email> emails = new ArrayList<Email>();
    	try {
	        Gmail service = getGmailService();
	        String userId = "me";
			listLabelsQuery( service, userId );
			log.debug(" >>>>>>>>>>>>  Getting currentHistory Id");
			BigInteger currentHistoryId = getHistoryId(service, userId);
			log.debug(" >>>>>>>>>>>>  Got currentHistory Id: " + currentHistoryId);
			BigInteger theLastId = getHistoryId(tempoaryEmailAddress);
			log.debug(" >>>>>>>>>>>>  Last Id: " + theLastId);
			if( theLastId == null ) {
				updateHistoryId("managlia@gmail.com", currentHistoryId);
				log.debug(" >>>>>>>>>>>>  Completed updateHistoryId()");
        	} else if( currentHistoryId.compareTo(theLastId) != 0 ) {
				log.debug(" >>>>>>>>>>>>  Starting pullEmailRecords()");
        		emails = pullEmailRecords(service, userId, theLastId);
				log.debug(" >>>>>>>>>>>>  Completed pullEmailRecords() with size " + emails.size() );
				updateHistoryId("managlia@gmail.com", currentHistoryId);
				log.debug(" >>>>>>>>>>>>  Completed updateHistoryId()");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(),e);
    		log.debug("++++++++++++++++++++++++++++++++++++++" + e.getMessage());
		}
    	return emails;
    }

    private BigInteger getHistoryId(String emailAddress) {
    	User user = userIntegration.getUserByEmailAddress(emailAddress);
    	if( user != null ) {
    		return user.getUserEmailHistoryId();
    	} else {
    		return null;
    	}
    }

    private User updateHistoryId(String emailAddress, BigInteger historyId) {
    	return userIntegration.updateHistoryId(historyId, emailAddress);
    }
    
    public BigInteger getHistoryId(Gmail service, String userId) throws IOException {
        GetProfile getProfile = 
        		service.users().getProfile(userId);
        getProfile.setFields("historyId");
        Profile profile = getProfile.execute();
        return profile.getHistoryId();
    }
    
    public List<Email> pullEmailRecords(Gmail service, String userId, BigInteger theLastId) throws IOException {
		log.debug(" >>>>>>>>>>>>  In pullEmailRecords");
    	ArrayList<Email> emails = new ArrayList<Email>();
    	Set<History> histories = new HashSet<History>();
    	ListHistoryResponse response = service.users().history().list(userId).setStartHistoryId(theLastId).execute();

		log.debug(" >>>>>>>>>>>>  In pullEmailRecords with response ");
		if( response != null ) {
			log.debug(" >>>>>>>>>>>>  In pullEmailRecords with response with size " + response.size());
		}
    	
    	
    	while (response.getHistory() != null) {
    	    histories.addAll(response.getHistory());
    	    if (response.getNextPageToken() != null) {
    	        String pageToken = response.getNextPageToken();
    	        response = service.users().history().list(userId)
    	                .setPageToken(pageToken)
    	                .setStartHistoryId(theLastId).execute();
    	    } else {
    	        break;
    	    }
    	}

		log.debug(" >>>>>>>>>>>>  In pullEmailRecords with histories size: " + histories.size());
    	HashSet<String> traversedMessages = new HashSet<String>(); 
    	for (History history : histories) {
    	    List<Message> theMessages = history.getMessages();
    	    for (Message aMessage : theMessages) {
    	    	try {
    	            Message rawMessage = service.users().messages().get(userId, aMessage.getId()).setFormat("FULL").execute();
    	            if( ! traversedMessages.contains(rawMessage.getHistoryId().toString()) ) {
    	            
    	    		log.debug(" >>>>>>>>>>>>  DFM CONSIDERING LABELS ON A MESSAGE "  +  rawMessage.getLabelIds());
	    	    		if( (!rawMessage.getLabelIds().contains("TRASH")) 
		            		&& (!rawMessage.getLabelIds().contains("CATEGORY_UPDATES"))
		            		&& (!rawMessage.getLabelIds().contains("CATEGORY_PROMOTIONS"))
		            		// && (!rawMessage.getLabelIds().contains("SENT"))
		            		&& (!rawMessage.getLabelIds().contains("SPAM"))
		            		&& (!rawMessage.getLabelIds().contains("Label_21"))
	            		) {
	        	    		log.debug(" >>>>>>>>>>>>  PROCESSING A MESSAGE WITH LABEL " +  rawMessage.getLabelIds() );
	        	            Email email = MessageToEmailConverter.convertMessageToEmail(rawMessage);
	        	            if( email != null ) {
	        	            	traversedMessages.add( email.getHistoryId().toString() );
	            	            emails.add(email);
	        	            }
	    	            }
    	            }    	            
    	    	} catch( Exception e ) {
    				log.error(e.getMessage(),e);
    	    		log.debug("++++++++++++++++++++++++++++++++++++++" + e.getMessage());
    	    	}
    	    }
    	}    
    	return emails;
    }
    
    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
            GmailIntegration.class.getResourceAsStream("/client_secret_72756157642-92ma7v0s6godo3cfrjb4hum65t5d9flq.apps.googleusercontent.com.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                //.setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        log.debug(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Gmail client service.
     * @return an authorized Gmail client service
     * @throws IOException
     */
    public Gmail getGmailService() throws IOException {
        Credential credential = authorize();
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException {
        // Build a new authorized API client service.
        String user = "me";
        GmailIntegration gis = new GmailIntegration();
        Gmail service = gis.getGmailService();
        gis.listLabelsQuery( service, user );
        gis.listMessagesMatchingQuery( service, user, null );
    }        

    public void listLabelsQuery(Gmail service, String userId) throws IOException {
    	/*
    	 * KEEPING FOR REFERENCE
    	 * 
        ListLabelsResponse listResponse =
            service.users().labels().list(userId).execute();
        List<Label> labels = listResponse.getLabels();
        if (labels.size() == 0) {
            log.debug(" >>>>>>>>>>>>  No labels found.");
        } else {
            log.debug(" >>>>>>>>>>>>  Labels:");
            for (Label label : labels) {
                System.out.printf("- %s\n", label.getName());
            }
        }
        */
    }

    
    public List<Message> listMessagesMatchingQuery(Gmail service, String userId,
	      String query) throws IOException {
	    ListMessagesResponse response = service.users().messages().list(userId).setMaxResults( new Long(100)).execute();
	    List<Message> messages = new ArrayList<Message>();
	    while (response.getMessages() != null) {
	      messages.addAll(response.getMessages());
	      if (response.getNextPageToken() != null) {
	        String pageToken = response.getNextPageToken();
	        response = service.users().messages().list(userId).setMaxResults( new Long(100))
	            .setPageToken(pageToken).execute();
	      } else {
	        break;
	      }
	    }
	    for (Message message : messages) {
	      log.debug(message.toPrettyString());
	      log.debug(" >>>>>>>>>>>>  History: " + message.getHistoryId());
	      log.debug(" >>>>>>>>>>>>  Body: " + message.getPayload().toPrettyString());
	    }
	    return messages;
	  }    
    
}