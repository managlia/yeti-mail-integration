package com.yeti.mail.integration;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yeti.core.repository.user.UserRepository;
import com.yeti.model.host.User;

@Service
public class UserIntegration {

    private static final Logger log = LoggerFactory.getLogger(UserIntegration.class);

	
	@Autowired
	private UserRepository userRepository;

	public List<User> getAllUsers() {
		List<User> companies = new ArrayList<User>();
		userRepository.findAll().forEach(companies::add);
		return companies;
	}

	public User getUser(Integer id) {
		return userRepository.findOne(id);
	}

	public User getUserByEmailAddress(String emailAddress) {
		Optional<User> oUser = userRepository.findByEmailAddress(emailAddress);
		if( oUser.isPresent() ) {
			log.debug("getting user: user found");
			return oUser.get();
		} else {
			log.debug("getting user: user not found");
			return null;
		}
	}
	
	public User updateUser(Integer id, User user) {
		return userRepository.save(user);
	}

	public User updateHistoryId(BigInteger historyId, User user) {
		user.setUserEmailHistoryId(historyId);
		log.debug("1 updating " + user.getEmailAddress() + " to " + historyId);
		return userRepository.save(user);
	}
	
	public User updateHistoryId(BigInteger historyId, Integer id) {
		User user = getUser(id);
		if( user != null ) {
			user.setUserEmailHistoryId(historyId);
			log.debug("2 updating " + user.getEmailAddress() + " to " + historyId);
			return userRepository.save(user);
		} else {
			return null;
		}
	}

	public User updateHistoryId(BigInteger historyId, String emailAddress) {
		User user = getUserByEmailAddress(emailAddress);
	    log.debug("User email --> " + emailAddress);
		if( user != null ) {
			log.debug("found a user");
			user.setUserEmailHistoryId(historyId);
			log.debug("3 updating " + user.getEmailAddress() + " to " + historyId);
			return userRepository.save(user);
		} else {
			log.debug("No suser found.");
			return null;
		}
	}

	public boolean exists(Integer id) {
		return (userRepository.findOne(id) != null);
	}
	
	public boolean exists(String emailAddress) {
		return userRepository.findByEmailAddress(emailAddress).isPresent();
	}
	
}
