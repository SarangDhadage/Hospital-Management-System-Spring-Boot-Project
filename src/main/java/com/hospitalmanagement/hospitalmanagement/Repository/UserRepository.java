package com.hospitalmanagement.hospitalmanagement.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hospitalmanagement.hospitalmanagement.model.User;


public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("select u from User u where u.email = :email")
    public User getUserByUserName(@Param("email") String email);
}