package com.contact.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.contact.entities.Contact;
import com.contact.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	// pagination
	@Query("from Contact as c where c.user.id=:userId")
	// pg object has two informatin -current page and no of contact per page
	public Page<Contact> findContactByUser(@Param("userId") int userId, Pageable pg);

	// predefined method for search which search all those contact which have name
	// in it and belong to that particular user only
	public List<Contact> findByNameContainingAndUser(String name, User user);
}
