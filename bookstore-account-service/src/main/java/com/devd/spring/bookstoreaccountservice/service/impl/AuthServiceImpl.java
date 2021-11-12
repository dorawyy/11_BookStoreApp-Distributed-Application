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

    OAuthClient oAuthClient = OAuthClient.builder() // call
        .client_secret(encode) // call
        .authorities(String.join(",", createOAuthClientRequest.getAuthorities())) // call // call
        .authorized_grant_types( // call
            String.join(",", createOAuthClientRequest.getAuthorized_grant_types())) // call 
        .scope(String.join(",", createOAuthClientRequest.getScope())) // call // call
        .resource_ids(String.join(",", createOAuthClientRequest.getResource_ids()))  // call // call
        .build();  // call

    OAuthClient saved = oAuthClientRepository.save(oAuthClient); // call

    return CreateOAuthClientResponse.builder() // call 
        .client_id(saved.getClient_id()) // call // call 
        .client_secret(clientSecret) // call
        .build(); // call

  }

  @Override
  public CreateUserResponse registerUser(SignUpRequest signUpRequest) {

    if (userRepository.existsByUserName(signUpRequest.getUserName())) { // call, missing (interface) // call 
      throw new RunTimeExceptionPlaceHolder("Username is already taken!!"); // call
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) { // call, missing (interface) // call
      throw new RunTimeExceptionPlaceHolder("Email address already in use!!"); // call. missing 
    }

    // Creating user's account
    com.devd.spring.bookstoreaccountservice.repository.dao.User user =
        new com.devd.spring.bookstoreaccountservice.repository.dao.User( // call
            signUpRequest.getUserName(), // call
            signUpRequest.getPassword(), // call
            signUpRequest.getFirstName(), // call
            signUpRequest.getLastName(), // call
            signUpRequest.getEmail()); // call

    user.setPassword(passwordEncoder.encode(user.getPassword())); // call // call 

    Role userRole = roleRepository.findByRoleName("STANDARD_USER") // call, missing (interface)
        .orElseThrow(() -> new RuntimeException("User Role not set."));

    user.setRoles(Collections.singleton(userRole)); // call

    com.devd.spring.bookstoreaccountservice.repository.dao.User savedUser =
        userRepository.save(user); // call, missing (interface)

    return CreateUserResponse.builder() // call
        .userId(savedUser.getUserId()) // call // call
        .userName(savedUser.getUserName()) // call // call
        .build(); // call

  }
}
