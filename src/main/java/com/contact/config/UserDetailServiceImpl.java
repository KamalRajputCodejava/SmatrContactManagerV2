package com.contact.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.contact.dao.UserRepo;
import com.contact.entity.User;

public class UserDetailServiceImpl implements UserDetailsService {
 //using dao ;
	@Autowired
	private UserRepo userRepo ;
	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		//fetching the user from db ;
		
		User user = userRepo.getUserByUserName(username);
		if(user==null) {
			throw new UsernameNotFoundException("could not found user !!");
		}
		CustomUserDetails customUserDetails = new CustomUserDetails(user);
		
		return customUserDetails;
	}
	
	

}  
