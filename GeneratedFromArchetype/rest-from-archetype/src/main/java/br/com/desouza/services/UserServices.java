package br.com.desouza.services;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.desouza.respositories.UserRepository;

@Service
public class UserServices implements UserDetailsService {

	@Autowired
	UserRepository repository;
	private Logger logger = Logger.getLogger(UserServices.class.getName());

	public UserServices(UserRepository repository) {
		this.repository = repository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		logger.info("Finding a User by username " + username + "...");
		var user = repository.findByUsername(username);
		if(user != null) return user;
		else throw new UsernameNotFoundException("Username " + username + " not found!");
	}
}