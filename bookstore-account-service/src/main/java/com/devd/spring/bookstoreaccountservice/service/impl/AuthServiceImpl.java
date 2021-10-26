package com.devd.spring.bookstoreaccountservice.service.impl;

import com.devd.spring.bookstoreaccountservice.repository.OAuthClientRepository;
import com.devd.spring.bookstoreaccountservice.repository.RoleRepository;
import com.devd.spring.bookstoreaccountservice.repository.UserRepository;
import com.devd.spring.bookstoreaccountservice.repository.dao.OAuthClient;
import com.devd.spring.bookstoreaccountservice.repository.dao.Role;
import com.devd.spring.bookstoreaccountservice.service.AuthService;
import com.devd.spring.bookstoreaccountservice.web.CreateOAuthClientRequest;
import com.devd.spring.bookstoreaccountservice.web.CreateOAuthClientResponse;
import com.devd.spring.bookstoreaccountservice.web.CreateUserResponse;
import com.devd.spring.bookstoreaccountservice.web.SignUpRequest;
import com.devd.spring.bookstorecommons.exception.RunTimeExceptionPlaceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

/**
 * @author: Devaraj Reddy, Date : 2019-06-30
 */
@Service
public class AuthServiceImpl implements AuthService {

  @Autowired
  BCryptPasswordEncoder passwordEncoder;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  OAuthClientRepository oAuthClientRepository;

  @Autowired
  AuthenticationManager authenticationManager;

  @Value("${security.jwt.key-store}")
  private Resource keyStore;

  @Value("${security.jwt.key-store-password}")
  private String keyStorePassword;

  @Value("${security.jwt.key-pair-alias}")
  private String keyPairAlias;

  @Value("${security.jwt.key-pair-password}")
  private String keyPairPassword;

  @Value("${security.jwt.public-key}")
  private Resource publicKey;

  @Override
  public CreateOAuthClientResponse createOAuthClient(
      CreateOAuthClientRequest createOAuthClientRequest) {

    //Generate client secret.
    String clientSecret = UUID.randomUUID().toString();
    String encode = passwordEncoder.encode(clientSecret);

    OAuthClient oAuthClient = OAuthClient.builder() // call, missing
        .client_secret(encode) // call, missing
        .authorities(String.join(",", createOAuthClientRequest.getAuthorities())) // call, missing // call, missing
        .authorized_grant_types( // call, missing
            String.join(",", createOAuthClientRequest.getAuthorized_grant_types())) // call, missing 
        .scope(String.join(",", createOAuthClientRequest.getScope())) // call, missing // call, missing
        .resource_ids(String.join(",", createOAuthClientRequest.getResource_ids()))  // call, missing // call, missing
        .build();  // call, missing

    OAuthClient saved = oAuthClientRepository.save(oAuthClient); // call, missing

    return CreateOAuthClientResponse.builder() // call, missing 
        .client_id(saved.getClient_id()) // call, missing // call, missing 
        .client_secret(clientSecret) // call, missing
        .build(); // call, missing

  }

  @Override
  public CreateUserResponse registerUser(SignUpRequest signUpRequest) {

    if (userRepository.existsByUserName(signUpRequest.getUserName())) { // call, missing // call, missing 
      throw new RunTimeExceptionPlaceHolder("Username is already taken!!"); // call, missing
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) { // call, missing // call, missing
      throw new RunTimeExceptionPlaceHolder("Email address already in use!!"); // call. missing 
    }

    // Creating user's account
    com.devd.spring.bookstoreaccountservice.repository.dao.User user =
        new com.devd.spring.bookstoreaccountservice.repository.dao.User( // call, missing
            signUpRequest.getUserName(), // call, missing
            signUpRequest.getPassword(), // call, missing
            signUpRequest.getFirstName(), // call, missing
            signUpRequest.getLastName(), // call, missing
            signUpRequest.getEmail()); // call, missing

    user.setPassword(passwordEncoder.encode(user.getPassword())); // call, missing // call, missing 

    Role userRole = roleRepository.findByRoleName("STANDARD_USER") // call, missing (interface)
        .orElseThrow(() -> new RuntimeException("User Role not set."));

    user.setRoles(Collections.singleton(userRole)); // call, missing

    com.devd.spring.bookstoreaccountservice.repository.dao.User savedUser =
        userRepository.save(user); // call, missing

    return CreateUserResponse.builder() // call, missing
        .userId(savedUser.getUserId()) // call, missing // call, missing
        .userName(savedUser.getUserName()) // call, missing // call, missing
        .build(); // call, missing

  }
}
