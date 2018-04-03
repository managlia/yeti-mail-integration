package com.yeti.mail.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import com.yeti.mail.service.EmailIntegrationService;
import com.yeti.model.action.Email;
import com.yeti.model.util.Batch;

/* 
 * 
 * Integration with the email client; gets and puts records to/from email client instead of database 
 * 
 * In all, this controller will:
 * 1. Get latest emails from the email client.
 * 2. Get a specific email from the email client.
 * 3. Send an email (returning the email as an Email record)
 * 4. Update an email on the email client.
 * 5. Update a batch of emails (not implementing but will if such functionality becomes needed)
 * 
 */
@RestController
@ExposesResourceFor(Email.class)
@RequestMapping(value = "/ExtEmails", produces = "application/hal+json")
public class EmailIntegrationController {

	@Autowired
	private EmailIntegrationService emailIntegrationService;

    private static final Logger log = LoggerFactory.getLogger(EmailIntegrationController.class);
	
	@GetMapping
	public ResponseEntity<List<Resource<Email>>> getRecentEmails() {
		/*
		 * 1. Get LATEST emails from email client
		 * 2. Put the emails into the database (going to do this via Camel as a subsequent step)
		 * 3. Return array of latest emails
		 */
		List<Email> emails = emailIntegrationService.getAllEmails();
		if( emails != null ) {
			List<Resource<Email>> returnEmails = new ArrayList<Resource<Email>>();
			for( Email email : emails ) {
				returnEmails.add(getEmailResource(email));
			}
			return ResponseEntity.ok(returnEmails);
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Resource<Email>> getEmail(@PathVariable Integer id) {
		/*
		 * 1. Takes an email client thread id gets the email directly from the client
		 */
		Email email = emailIntegrationService.getEmail(id);
		if( email == null ) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(getEmailResource(email));
		}
	}
	
	@PostMapping
	public ResponseEntity<Resource<Email>> sendAnEmail(@RequestBody Email email, HttpServletRequest request ) {
		/*
		 * 1. Send an email via the email client
		 * 2. Put the sent email into the Action db table (going to do this via Camel as a subsequent step)
		 * 3. Add the reminder to the client calendar (going to do this via Camel as a subsequent step)
		 * 4. Put the added reminder into the Action db table (going to do this via Camel as a subsequent step)
		 * 5. Return the Email object and its child reminder Action (contained within the email)
		 */
		Email newEmail = emailIntegrationService.sendAnEmail(email);
		if( newEmail != null ) {
			String requestURI = request.getRequestURI();
			try {
				return ResponseEntity.ok(getEmailResource(email));
			} catch( Exception e ) {
				return ResponseEntity.badRequest().build();
			}
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Resource<Email>> updateEmailRecord(@RequestBody Email email, @PathVariable Integer id) {
		/*
		 * 1. Possible updates: Mark as read, move to different folder, etc. There may be some hidden fields 
		 *    that we may want to update too. 
		 * 2. After update occurs on email client, the Email record should be updated in the Action db table.  (going to do this via Camel as a subsequent step)
		 * 3. Return the updated record as an Email object. 
		 */
		if( !emailIntegrationService.exists(id) ) {
			return ResponseEntity.notFound().build();
		} else {
			emailIntegrationService.updateEmail(id, email);
			Email updatedEmail = emailIntegrationService.updateEmail(id, email);
			if( updatedEmail != null ) {
				return ResponseEntity.accepted().build();		
			} else {
				return ResponseEntity.badRequest().build();
			}
		}
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Resource<Email>> deleteEmail(@PathVariable Integer id) {
		/* 
		 * This is a placeholder but I don't think we will implement it. The act of deleting an email
		 * will be completed by moving to trash in the updateEmailRecord action above.
		 */
		return null;
	}
	
	@PatchMapping
	public void processBatch(@RequestBody Batch batch) {
		/* 
		 * Imagine we could make bulk changes but nothing planned yet.
		 */
		emailIntegrationService.processBatch(batch);
	}

	private Resource<Email> getEmailResource(Email a) {
	    Resource<Email> resource = new Resource<Email>(a);
	    // resource.add(linkTo(methodOn(EmailIntegrationController.class).getEmail(a.getActionId())).withSelfRel());
	    return resource;
	}

}








