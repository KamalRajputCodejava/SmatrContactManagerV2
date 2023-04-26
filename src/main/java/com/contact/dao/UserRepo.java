package com.contact.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.contact.entity.User;
@Service
public interface  UserRepo  extends JpaRepository<User,Integer> {
@Query("select u from User u where u.email=:email")	
public User getUserByUserName(@Param("email")String email );	

}
