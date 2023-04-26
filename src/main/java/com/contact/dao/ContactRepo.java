package com.contact.dao;

import java.util.List;

//import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.contact.entity.Contact;
import com.contact.entity.User;
@Service
public interface ContactRepo  extends JpaRepository<Contact,Integer>{
	//in which we want to override the pagination method ;
	@Query("from Contact as c where c.user.id=:userId")
	//current page , contacts per page 5 ;
	public Page<Contact> findContactsByUser(@Param("userId") int userId ,Pageable pePageable);
	
	public List<Contact> findByNameContainingAndUser(String name, User user); //very important for searching ;

}
