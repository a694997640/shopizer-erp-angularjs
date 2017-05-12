package com.shopizer.business.repository.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.shopizer.business.entity.user.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
	
	User findByUserName(String userName);

}
