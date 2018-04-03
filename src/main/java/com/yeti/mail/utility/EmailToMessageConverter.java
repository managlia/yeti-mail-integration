package com.yeti.mail.utility;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.Message;
import com.yeti.model.action.Email;
import com.yeti.model.contact.Contact;
import com.yeti.model.general.Attachment;

public class EmailToMessageConverter {

	private static final Logger log = LoggerFactory.getLogger(EmailToMessageConverter.class);
	

	 private static RecipientType getRecipientType(String recipientKey) {
		if (recipientKey.equalsIgnoreCase("to")) {
			return javax.mail.Message.RecipientType.TO;
		} else if (recipientKey.equalsIgnoreCase("cc")) {
			return javax.mail.Message.RecipientType.CC;
		} else if (recipientKey.equalsIgnoreCase("bcc")) {
			return javax.mail.Message.RecipientType.BCC;
		} else {
			return null;
		}
	 }		
	 
	 public static Message convertEmailToMessage(Email emailMessage, String fromEmailAddress)
	            throws MessagingException, IOException {
		MimeMessage emailContent = EmailToMessageConverter.convertEmailToMimeMessage(emailMessage, fromEmailAddress);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		emailContent.writeTo(buffer);
		byte[] bytes = buffer.toByteArray();
		String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}	 
	 
	 
	private static MimeMessage convertEmailToMimeMessage( Email email, 
 													 String fromEmailAddress ) {
		try {
	        Properties props = new Properties();
	        Session session = Session.getDefaultInstance(props, null);    	

	        MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(fromEmailAddress));

			log.debug( " email "  + email );
			log.debug( " email.getRecipients() "  + email.getRecipients() );
			log.debug( " email.getRecipients().size() "  + email.getRecipients().size() );
			for( Contact contact : email.getRecipients() ) {
				if( contact.getEmailRecipientIndicator() != null ) {
					message.addRecipient( 
							EmailToMessageConverter.getRecipientType(contact.getEmailRecipientIndicator()),
			                new InternetAddress(contact.getContactEmailAddress()));
				}
			}
			message.setSubject(email.getName());

			if( email.getAttachments() == null || email.getAttachments().size() == 0 ) {
				message.setText(email.getDescription());
			} else {
		        MimeBodyPart mimeBodyPart = new MimeBodyPart();
		        mimeBodyPart.setContent(email.getDescription(), "text/plain");
		        
		        Multipart multipart = new MimeMultipart();
		        multipart.addBodyPart(mimeBodyPart);
		        
		        for(Attachment attachment : email.getAttachments()) { 
					byte[] fileInBytes = attachment.getAttachmentFile();
					File fileAttachment = new File(attachment.getName());
					FileUtils.writeByteArrayToFile(fileAttachment, fileInBytes);
					
					mimeBodyPart = new MimeBodyPart();
					DataSource source = new FileDataSource(fileAttachment);
					
					mimeBodyPart.setDataHandler(new DataHandler(source));
					mimeBodyPart.setFileName(attachment.getName());
					    
					multipart.addBodyPart(mimeBodyPart);
				}
		        message.setContent(multipart);
			}
			
	        return message;			
			
		} catch( Exception e ) {
			log.error(e.getMessage(),e);
    		log.debug("++++++++++++++++++++++++++++++++++++++" + e.getMessage());
			return null;
		}
	}
}

