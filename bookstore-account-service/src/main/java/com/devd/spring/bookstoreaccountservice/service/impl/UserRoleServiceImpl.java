package com.devd.spring.bookstoreaccountservice.service.impl;

import com.devd.spring.bookstoreaccountservice.exception.SuccessCodeWithErrorResponse;
import com.devd.spring.bookstoreaccountservice.repository.RoleRepository;
import com.devd.spring.bookstoreaccountservice.repository.UserRepository;
import com.devd.spring.bookstoreaccountservice.repository.dao.Role;
import com.devd.spring.bookstoreaccountservice.repository.dao.User;
import com.devd.spring.bookstoreaccountservice.service.UserRoleService;
import com.devd.spring.bookstoreaccountservice.web.MapRoleToUsersRequest;
import com.devd.spring.bookstoreaccountservice.web.MapUserToRolesRequest;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.devd.spring.bookstorecommons.exception.Error;
import com.devd.spring.bookstorecommons.exception.ErrorResponse;
import com.devd.spring.bookstorecommons.exception.RunTimeExceptionPlaceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Devaraj Reddy, Date : 2019-07-01
 */
@Service
public class UserRoleServiceImpl implements UserRoleService {

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Override
  public void mapUserToRoles(String userNameOrEmail, MapUserToRolesRequest mapUserToRolesRequest) {

    Optional<User> userNameOrEmailOptional = userRepository
        .findByUserNameOrEmail(userNameOrEmail, userNameOrEmail); // call, missing 

    User user = userNameOrEmailOptional.orElseThrow(() ->
        new RunTimeExceptionPlaceHolder("UserNameOrEmail doesn't exist!!") // call, missing
    );

    Set<Role> roles = user.getRoles(); // call, missing

    ErrorResponse errorResponse = ErrorResponse.builder() // call, missing
        .uuid(UUID.randomUUID()) // call, missing
        .errors(new ArrayList<>()) // call, missing
        .build(); // call

    mapUserToRolesRequest.getRoleNames().forEach(roleName -> { // call, missing
      //if role exists add to list and persist, else add to error response persist valid roles and send
      // response containing invalid roles.
      roleRepository.findByRoleName(roleName).<Runnable>map(role -> () -> roles.add(role)) // call, missing
          .orElse(() -> {
            Error error = Error.builder() // call, missing
                .code("400") // call, missing
                .message(roleName + " role doesn't exist!!") // call, missing
                .build(); // call, missing
            errorResponse.getErrors().add(error); // call, missing
          })
          .run(); // call, missing
    });

    user.setRoles(roles); // call, missing

    userRepository.save(user); // call, missing

    if (!errorResponse.getErrors().isEmpty()) { // call, missing
      throw new SuccessCodeWithErrorResponse(errorResponse); // call, missing
    }

  }

  @Override
  public void removeRolesFromUser(String userNameOrEmail, MapUserToRolesRequest mapUserToRolesRequest) {

    Optional<User> userNameOrEmailOptional = userRepository
            .findByUserNameOrEmail(userNameOrEmail, userNameOrEmail); // call, missing

    User user = userNameOrEmailOptional.orElseThrow(() ->
            new RunTimeExceptionPlaceHolder("UserNameOrEmail doesn't exist!!") // call, missing
    );

    Set<Role> roles = user.getRoles(); // call, missing

    ErrorResponse errorResponse = ErrorResponse.builder() // call, missing
            .uuid(UUID.randomUUID()) // call, missing
            .errors(new ArrayList<>()) // call, missing
            .build(); // call, missing

    mapUserToRolesRequest.getRoleNames().forEach(roleName -> { // call, missing
      //if role exists add to list and persist, else add to error response persist valid roles and send
      // response containing invalid roles.
      roleRepository.findByRoleName(roleName).<Runnable>map(role -> () -> roles.remove(role)) // call, missing
              .orElse(() -> {
                Error error = Error.builder() // call, missing
                        .code("400") // call, missing
                        .message(roleName + " role doesn't exist!!") // call, missing
                        .build(); // call, missing
                errorResponse.getErrors().add(error); // call, missing
              })
              .run(); // call, missing
    });

    user.setRoles(roles); // call, missing

    userRepository.save(user); // call, missing

    if (!errorResponse.getErrors().isEmpty()) { // call, missing
      throw new SuccessCodeWithErrorResponse(errorResponse); // call, missing
    }
  }

  @Override
  public void mapRoleToUsers(String roleName, MapRoleToUsersRequest mapRoleToUsersRequest) {

    Role role = roleRepository.findByRoleName(roleName) // call, missing
        .orElseThrow(() -> new RuntimeException("Role doesn't exist!!"));

    ErrorResponse errorResponse = ErrorResponse.builder() // call, missing
        .uuid(UUID.randomUUID()) // call, missing
        .errors(new ArrayList<>()) // call, missing
        .build(); // call, missing

    mapRoleToUsersRequest.getUserNames().forEach(userName -> { // call, missing
      userRepository.findByUserName(userName).<Runnable>map(user -> () -> role.addUser(user)) // call, missing // call, missing
          .orElse(() -> {
            Error error = Error.builder() // call, missing
                .code("400") // call, missing
                .message(userName + " userName doesn't exist!!") // call, missing
                .build(); // call, missing
            errorResponse.getErrors().add(error); // call, missing
          })
          .run(); // call, missing
    });

    roleRepository.save(role); // call, missing

    if (!errorResponse.getErrors().isEmpty()) { // call, missing
      throw new SuccessCodeWithErrorResponse(errorResponse); // call, missing
    }
  }
}
