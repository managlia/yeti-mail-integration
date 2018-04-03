package com.yeti.calendar.utility;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.yeti.mail.utility.EmailToMessageConverter;
import com.yeti.model.action.CalendarEvent;

public class EventToCalendarEventConverter {

	private static final Logger log = LoggerFactory.getLogger(EmailToMessageConverter.class);
	
	public static CalendarEvent convertEventToCalendarEvent( Event event ) {
		try {
			CalendarEvent calendarEvent = new CalendarEvent();
			
			calendarEvent.setTargetCompletionDate( new Date(event.getStart().getDateTime().getValue()) );
			calendarEvent.setName(event.getSummary());
			calendarEvent.setDescription(event.getDescription());
			calendarEvent.setExternalId(event.getId());
			
			calendarEvent.setOwnerId(new Integer(1));
			calendarEvent.setLastRetrievedDate(new Date());
			calendarEvent.setLastRetrievedValue(event.toPrettyString());
			
			return calendarEvent;
		} catch( Exception e ) {
			log.error(e.getMessage(),e);
			log.debug("++++++++++++++++++++++++++++++++++++++" + e.getMessage());
			return null;
		}
	}
	
	
}
