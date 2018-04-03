package com.yeti.calendar.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import com.yeti.calendar.service.CalendarIntegrationService;
import com.yeti.model.action.CalendarEvent;
import com.yeti.model.action.Email;
import com.yeti.model.util.Batch;

/*
 *
 * Integration with the third party calendar client; gets and puts records to/from calendar client instead of database
 *
 * In all, this controller will:
 * 1. Get latest calendar events from the calendar client.
 * 2. Get a specific event from the calendar client.
 * 3. Create a new event (returning the event as an CalendarEvent record)
 * 4. Update an event on the calendar client.
 * 5. Update a batch of events
 *
 */
@RestController
@ExposesResourceFor(CalendarEvent.class)
@RequestMapping(value = "/ExtEvents", produces = "application/hal+json")
public class CalendarIntegrationController {

	@Autowired
	private CalendarIntegrationService calendarIntegrationService;

    private static final Logger log = LoggerFactory.getLogger(CalendarIntegrationController.class);

	@GetMapping
	public ResponseEntity<List<Resource<CalendarEvent>>> getEvents(
				@RequestParam(required=false) @DateTimeFormat(pattern="yyyy-MM-dd") Optional<Date> startDate,
				@RequestParam(required=false) @DateTimeFormat(pattern="yyyy-MM-dd") Optional<Date> endDate,
				@RequestParam(required=false) Optional<Integer> spanInDays
	) {
		
		/* 
		 * Determine the start and end dates based on the data passed in.
		 * Business rules: 
		 * 1. If startDate is passed in alone, then endDate is startDate + 1 month
		 * 2. If startDate is blank but spanInDays is populated or endDate is populated with a future date, then startDate is today
		 * 3. If nothing is passed in, startDate is today and endDate is today + 1 month
		 * 4. If startDate is after endDate, reject.
		 * 5. If spanInDays is negative, reject.
		 * 6. If endDate and spanInDays are both populated, reject.
		 */

		log.debug("dfm123 >>>>>>>>>>>>>>>>>>>>>>>>>>>>   startDate:: " + (startDate.isPresent() ? startDate.get() : "NOT PRESENT") );
		log.debug("dfm123 >>>>>>>>>>>>>>>>>>>>>>>>>>>>   endDate:: " + (endDate.isPresent() ? endDate.get() : "NOT PRESENT") );
		log.debug("dfm123 >>>>>>>>>>>>>>>>>>>>>>>>>>>>   spanInDays:: " + (spanInDays.isPresent() ? spanInDays.get() : "NOT PRESENT") );
		Date searchStartDate = null;
		Date searchEndDate = null;
		String rejectReason = null;
		
		if( endDate.isPresent() && spanInDays.isPresent() ) {
			log.debug("dfm123 >>>>>>>>>>>>>>>>>>>>>>>>>>>>   reject for incongruent filters");
			rejectReason = "Bad filters: endDate and spanInDays both present";
		
		} else if( startDate.isPresent() && endDate.isPresent() && startDate.get().after(endDate.get()) ) {
			log.debug("dfm123 >>>>>>>>>>>>>>>>>>>>>>>>>>>>   REJECT for bad date filters");
			rejectReason = "Bad filters: endDate cannot be before starDate";

		} else if( spanInDays.isPresent() && spanInDays.get().intValue() <= 0 ) {
			log.debug("dfm123 >>>>>>>>>>>>>>>>>>>>>>>>>>>>   REJECT for bad spanInDays filter");
			rejectReason = "Bad filter: spanInDays cannot be negative";

		} else {
			log.debug("dfm123 >>>>>>>>>>>>>>>>>>>>>>>>>>>>   we have data we can work with (we think)");
			searchStartDate = startDate.isPresent() ? startDate.get() : getToday();
			if( endDate.isPresent() && endDate.get().before(searchStartDate) ) {
				log.debug("dfm123 >>>>>>>>>>>>>>>>>>>>>>>>>>>>   REJECT because endDate is before startDate");
				rejectReason = "Bad filter: endDate before startDate (today)";
			} else {
				searchEndDate = endDate.isPresent() ? endDate.get() : getEndDate(searchStartDate, spanInDays);
				log.debug("dfm123 >>>>>>>>>>>>>>>>>>>>>>>>>>>>   READY TO GO WITH searchStartDate " + searchStartDate);
				log.debug("dfm123 >>>>>>>>>>>>>>>>>>>>>>>>>>>>   READY TO GO WITH searchEndDate " + searchEndDate);
			}
		}
	
		
		/*
		 * 1. Get events from event client (may want to fail if no filters are provided)
		 * 2. Put the events into the database (going to do this via Camel as a subsequent step)
		 * 3. Return array of events
		 */
		
		if( rejectReason == null ) {
			List<CalendarEvent> calendarEvents = calendarIntegrationService.getEvents(searchStartDate, searchEndDate);
			if( calendarEvents != null ) {
				List<Resource<CalendarEvent>> returnEvents = new ArrayList<Resource<CalendarEvent>>();
				for( CalendarEvent calendarEvent : calendarEvents ) {
					returnEvents.add(getEventResource(calendarEvent));
				}
				ResponseEntity<List<Resource<CalendarEvent>>> re = ResponseEntity.ok(returnEvents);
				/*
				ArrayList<String> startDateList = new ArrayList<String>();
				ArrayList<String> endDateList = new ArrayList<String>();
				startDateList.add(searchStartDate.toString());
				endDateList.add(searchEndDate.toString());
				re.getHeaders().put("X-START-DATE", startDateList);
				re.getHeaders().put("X-END-DATE", endDateList);
				*/
				return re;
			} else {
				return ResponseEntity.badRequest().build();
			}
		} else {
			ResponseEntity<List<Resource<CalendarEvent>>> re = ResponseEntity.badRequest().build();
			/*
			ArrayList<String> rejectList = new ArrayList<String>();
			rejectList.add(rejectReason);
			re.getHeaders().put("X-REJECT-REASON", rejectList);
			*/
			return re;
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<Resource<CalendarEvent>> getEvent(@PathVariable Integer id) {
		/*
		 * 1. Takes an event client thread id gets the event directly from the client
		 */
		CalendarEvent calendarEvent = calendarIntegrationService.getEvent(id);
		if( calendarEvent == null ) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(getEventResource(calendarEvent));
		}
	}

	@PostMapping
	public ResponseEntity<Resource<CalendarEvent>> scheduleAnEvent(@RequestBody CalendarEvent calendarEvent, HttpServletRequest request ) {
	
	//public ResponseEntity<Resource<CalendarEvent>> scheduleAnEvent(@RequestBody Email email, HttpServletRequest request ) {
	//public ResponseEntity<Resource<CalendarEvent>> scheduleAnEvent(@RequestBody CalendarEvent calendarEvent, HttpServletRequest request ) {
		//CalendarEvent calendarEvent = email.getCalendarEvents().get(0); 
		
		/*
		 * 1. Send an event via the event client
		 * 2. Put the sent event into the Action db table (going to do this via Camel as a subsequent step)
		 * 3. Add the reminder to the client calendar (going to do this via Camel as a subsequent step)
		 * 4. Put the added reminder into the Action db table (going to do this via Camel as a subsequent step)
		 * 5. Return the Event object and its child reminder Action (contained within the event)
		 */
		CalendarEvent scheduledEvent = calendarIntegrationService.createEvent(calendarEvent);
		if( scheduledEvent != null ) {
			String requestURI = request.getRequestURI();
			try {
				return ResponseEntity.ok(getEventResource(scheduledEvent));
			} catch( Exception e ) {
				return ResponseEntity.badRequest().build();
			}
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<Resource<CalendarEvent>> updateEventRecord(@RequestBody CalendarEvent calendarEvent, @PathVariable Integer id) {
		/*
		 * 1. Possible updates: Mark as read, move to different folder, etc. There may be some hidden fields
		 *    that we may want to update too.
		 * 2. After update occurs on event client, the Event record should be updated in the Action db table.  (going to do this via Camel as a subsequent step)
		 * 3. Return the updated record as an Event object.
		 */
		if( !calendarIntegrationService.exists(id) ) {
			return ResponseEntity.notFound().build();
		} else {
			calendarIntegrationService.updateEvent(id, calendarEvent);
			CalendarEvent updatedEvent = calendarIntegrationService.updateEvent(id, calendarEvent);
			if( updatedEvent != null ) {
				return ResponseEntity.accepted().build();
			} else {
				return ResponseEntity.badRequest().build();
			}
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Resource<CalendarEvent>> deleteEvent(@PathVariable Integer id) {
		/*
		 * This is a placeholder but I don't think we will implement it. The act of deleting an event
		 * will be completed by moving to trash in the updateEventRecord action above.
		 */
		return null;
	}

	@PatchMapping
	public void processBatch(@RequestBody Batch batch) {
		/*
		 * We will use this for recurring events or batch schedule changes (e.g., Cancel all my meetings for tomorrow.)
		 */
		calendarIntegrationService.processBatch(batch);
	}

	private Resource<CalendarEvent> getEventResource(CalendarEvent a) {
	    Resource<CalendarEvent> resource = new Resource<CalendarEvent>(a);
	    // resource.add(linkTo(methodOn(EventIntegrationController.class).getEvent(a.getActionId())).withSelfRel());
	    return resource;
	}

	private Date getToday() {
		Calendar c = new GregorianCalendar();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
	
	private Date getEndDate(Date searchStartDate, Optional<Integer> spanInDays) {
		Calendar c = new GregorianCalendar();
		c.setTime(searchStartDate);
		if( spanInDays.isPresent() ) {
			c.add(Calendar.DATE, spanInDays.get().intValue());
		} else {
			c.add(Calendar.MONTH, 1);
		}
		return c.getTime();
	}
}
