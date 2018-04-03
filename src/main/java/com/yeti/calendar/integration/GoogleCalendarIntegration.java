package com.yeti.calendar.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.yeti.calendar.utility.CalendarEventToEventConverter;
import com.yeti.calendar.utility.EventToCalendarEventConverter;
import com.yeti.model.action.CalendarEvent;
import com.yeti.model.action.Email;

@Service
public class GoogleCalendarIntegration {

	private static final Logger log = LoggerFactory.getLogger(GoogleCalendarIntegration.class);

	private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
	private static final java.io.File DATA_STORE_DIR = new java.io.File(
			System.getProperty("user.home"), ".credentials/calendar-java-quickstart");
	private static FileDataStoreFactory DATA_STORE_FACTORY;
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static HttpTransport HTTP_TRANSPORT;
	private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR_READONLY);

	static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }	
	
	public static Credential authorize() throws IOException {
		InputStream in = GoogleCalendarIntegration.class.getResourceAsStream(
				"/client_secret_72756157642-92ma7v0s6godo3cfrjb4hum65t5d9flq.apps.googleusercontent.com.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
		return credential;
	}

	public static Calendar getCalendarService() throws IOException {
		Credential credential = authorize();
		return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}
	
    public List<CalendarEvent> getEvents(Date startDate, Date endDate) {
    	List<CalendarEvent> calendarEvents = new ArrayList<CalendarEvent>();
    	try {
	        com.google.api.services.calendar.Calendar service =
	                getCalendarService();
	
	        DateTime now = new DateTime( startDate.getTime() );
	        DateTime andThen = new DateTime( endDate.getTime() );
	        Events events = service.events().list("primary")
	            .setMaxResults(1000)
	            .setTimeMin(now)
	            .setTimeMax(andThen)
	            .setOrderBy("startTime")
	            .setSingleEvents(true)
	            .execute();
	        List<Event> items = events.getItems();
	        if (items.size() == 0) {
	            System.out.println("No upcoming events found.");
	        } else {
	            System.out.println("Upcoming events");
	            for (Event event : items) {
	            	CalendarEvent ce = EventToCalendarEventConverter.convertEventToCalendarEvent(event);
	            	if( ce != null ) {
	            		calendarEvents.add(ce);
	            	}
	            }
	        }
		} catch( IOException e ) {
			log.error(e.getMessage(),e);
			log.debug("++++++++++++++++++++++++++++++++++++++" + e.getMessage());
		}
    	
    	return calendarEvents;
    }

	public CalendarEvent createEvent(CalendarEvent calendarEvent) {
		try {
	        Calendar service = getCalendarService();
			String calendarId = "primary";
			Event createdEvent = service.events().insert(calendarId, CalendarEventToEventConverter.convertCalendarEventToEvent(calendarEvent, "managlia@gmail.com")).execute();
			System.out.printf("Event created: %s\n", createdEvent.getHtmlLink());
			return EventToCalendarEventConverter.convertEventToCalendarEvent(createdEvent);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			log.debug("++++++++++++++++++++++++++++++++++++++" + e.getMessage());
			return null;
		}
	}
    
	public static void main(String[] args) throws IOException {
        com.google.api.services.calendar.Calendar service =
            getCalendarService();

        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
            .setMaxResults(10)
            .setTimeMin(now)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute();
        List<Event> items = events.getItems();
        if (items.size() == 0) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }
    }



}
