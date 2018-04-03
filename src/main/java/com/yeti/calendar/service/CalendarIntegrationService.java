package com.yeti.calendar.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yeti.calendar.integration.GoogleCalendarIntegration;
import com.yeti.model.action.CalendarEvent;
import com.yeti.model.util.Batch;

@Service
public class CalendarIntegrationService {

	@Autowired
	GoogleCalendarIntegration googleCalendarIntegration;
	
	public List<CalendarEvent> getEvents(Date startDate, Date endDate) {
		// TODO Auto-generated method stub
		List<CalendarEvent> calendarEvents = new ArrayList<CalendarEvent>();
		calendarEvents = googleCalendarIntegration.getEvents(startDate, endDate);
		return calendarEvents;
	}

	public CalendarEvent getEvent(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	public CalendarEvent createEvent(CalendarEvent calendarEvent) {
		// TODO Auto-generated method stub
		CalendarEvent scheduledEvent = googleCalendarIntegration.createEvent(calendarEvent);
		return scheduledEvent;
	}

	public boolean exists(Integer id) {
		// TODO Auto-generated method stub
		return false;
	}

	public CalendarEvent updateEvent(Integer id, CalendarEvent calendarEvent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void processBatch(Batch batch) {
		// TODO Auto-generated method stub
		
	}

}
