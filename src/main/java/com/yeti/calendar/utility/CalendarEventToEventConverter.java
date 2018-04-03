package com.yeti.calendar.utility;

import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.yeti.mail.utility.EmailToMessageConverter;
import com.yeti.model.action.CalendarEvent;

public class CalendarEventToEventConverter {

	private static final Logger log = LoggerFactory.getLogger(EmailToMessageConverter.class);
	
	public static Event convertCalendarEventToEvent( CalendarEvent calendarEvent, String emailAddress ) {
		try {
			Event event = new Event();
			
			event.setSummary(calendarEvent.getName());
			event.setLocation("181 West Madison Ave, Chicago, IL 60606");
			event.setDescription(calendarEvent.getDescription());
			
			
			DateTime startDateTime = new DateTime(calendarEvent.getTargetCompletionDate());
			EventDateTime start = new EventDateTime()
			    .setDateTime(startDateTime)
			    .setTimeZone("America/New_York");
			event.setStart(start);
			
			DateTime endDateTime = new DateTime(calendarEvent.getTargetCompletionDate());
			EventDateTime end = new EventDateTime()
			    .setDateTime(endDateTime)
			    .setTimeZone("America/New_York");
			event.setEnd(end);			
			
			EventAttendee[] attendees = new EventAttendee[] {
			    new EventAttendee().setEmail(emailAddress)
			};			
			event.setAttendees(Arrays.asList(attendees));
			
			EventReminder[] reminderOverrides = new EventReminder[] {
			    new EventReminder().setMethod("email").setMinutes(24 * 60),
			    new EventReminder().setMethod("popup").setMinutes(10),
			};
			
			Event.Reminders reminders = new Event.Reminders()
				    .setUseDefault(false)
				    .setOverrides(Arrays.asList(reminderOverrides));
				event.setReminders(reminders);
			
			return event;
		} catch( Exception e ) {
			log.error(e.getMessage(),e);
			log.debug("++++++++++++++++++++++++++++++++++++++" + e.getMessage());
			return null;
		}
	}
}
