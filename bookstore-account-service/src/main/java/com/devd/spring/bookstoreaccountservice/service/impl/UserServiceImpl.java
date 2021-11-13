package com.devd.spring.bookstoreaccountservice.service.impl;

import com.devd.spring.bookstoreaccountservice.exception.SuccessCodeWithErrorResponse;
import com.devd.spring.bookstoreaccountservice.repository.RoleRepository;
import com.devd.spring.bookstoreaccountservice.repository.UserRepository;
import com.devd.spring.bookstoreaccountservice.repository.dao.Role;
import com.devd.spring.bookstoreaccountservice.repository.dao.User;
import com.devd.spring.bookstoreaccountservice.service.UserRoleService;
import com.devd.spring.bookstoreaccountservice.service.UserService;
import com.devd.spring.bookstoreaccountservice.web.CreateUserRequest;
import com.devd.spring.bookstoreaccountservice.web.GetUserInfoResponse;
import com.devd.spring.bookstoreaccountservice.web.GetUserResponse;
import com.devd.spring.bookstoreaccountservice.web.MapUserToRolesRequest;
import com.devd.spring.bookstoreaccountservice.web.UpdateUserRequest;
import com.devd.spring.bookstoreaccountservice.web.UpdateUserRequestFromAdmin;
import com.devd.spring.bookstorecommons.exception.Error;
import com.devd.spring.bookstorecommons.exception.ErrorResponse;
import com.devd.spring.bookstorecommons.exception.RunTimeExceptionPlaceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author: Devaraj Reddy, Date : 2019-06-30
 */
@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private UserRoleService userRoleService;

  @Override
  public String createUser(CreateUserRequest createUserRequest) {

    String encodedPassword = passwordEncoder.encode(createUserRequest.getPassword()); // call

    if (userRepository.existsByUserName(createUserRequest.getUserName())) { // call, missing // call
      throw new RunTimeExceptionPlaceHolder("Username is already taken!!"); // call
    }

    if (userRepository.existsByEmail(createUserRequest.getEmail())) { // call, missing // call
      throw new RunTimeExceptionPlaceHolder("Email address already in use!!"); // call
    }

    ErrorResponse errorResponse = ErrorResponse.builder() // call
        .uuid(UUID.randomUUID()) // call
        .errors(new ArrayList<>()) // call
        .build(); // call

    List<Role> validRoles = new ArrayList<>();

    createUserRequest.getRoleNames().forEach(roleName -> { // call

      //if role exists add to list and persist, else add to error response persist valid roles and send
      // response containing invalid roles.
      roleRepository.findByRoleName(roleName).<Runnable>map(role -> () -> validRoles.add(role)) // call, missing
          .orElse(() -> {
            Error error = Error.builder() // call
                .code("400") // call
                .message(roleName + " role doesn't exist") // call
                .build(); // call
            errorResponse.getErrors().add(error); // call
          })
          .run(); // call
    });

    User user = User.builder() // call
        .userName(createUserRequest.getUserName()) // call // call
        .email(createUserRequest.getEmail()) // call // call
        .firstName(createUserRequest.getFirstName()) // call // call
        .lastName(createUserRequest.getLastName()) // call // call
        .password(encodedPassword) // call
        .roles(new HashSet<>(validRoles)) // call
        .build(); // call

    User savedUser = userRepository.save(user); // call, missing

    if (!errorResponse.getErrors().isEmpty()) { // call
      throw new SuccessCodeWithErrorResponse(savedUser.getUserId(), errorResponse); // call // call
    }

    return savedUser.getUserId(); // call
  }

  @Override
  public GetUserResponse getUserByUserName(String userName) {

    Optional<User> userNameOrEmailOptional = userRepository
        .findByUserNameOrEmail(userName, userName); // call, missing
    User userByUserName = userNameOrEmailOptional.orElseThrow(() ->
        new RunTimeExceptionPlaceHolder("UserName or Email doesn't exist!!") // call
    );

    return GetUserResponse.builder() // call
        .userId(userByUserName.getUserId()) // call // call
        .userName(userByUserName.getUserName()) // call // call
        .firstName(userByUserName.getFirstName()) // call // call
        .lastName(userByUserName.getLastName()) // call // call
        .email(userByUserName.getEmail()) // call // call
        .roles(userByUserName.getRoles()) // call // call
        .build(); // call
  }

  @Override
  public GetUserResponse getUserByUserId(String userId) {
    Optional<User> userIdOptional = userRepository.findByUserId(userId); // call, missing
    User userById = userIdOptional.orElseThrow(() ->
        new RunTimeExceptionPlaceHolder("UserName or Email doesn't exist!!") // call
    );

    return GetUserResponse.builder() // call
        .userId(userById.getUserId()) // call // call
        .userName(userById.getUserName()) // call // call
        .firstName(userById.getFirstName()) // call // call
        .lastName(userById.getLastName()) // call // call
        .email(userById.getEmail()) // call // call
        .roles(userById.getRoles()) // call // call
        .build(); // call
  }

  @Override
  public GetUserInfoResponse getUserInfo() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userName = (String) authentication.getPrincipal();

    GetUserResponse userByUserName = getUserByUserName(userName);

    return GetUserInfoResponse.builder() // call
        .userId(userByUserName.getUserId()) // call // call
        .userName(userByUserName.getUserName()) // call // call
        .firstName(userByUserName.getFirstName()) // call // call
        .lastName(userByUserName.getLastName()) // call // call
        .email(userByUserName.getEmail()) // call // call
        .build(); // call

  }

  @Override
  public void updateUserInfo(UpdateUserRequest updateUserRequest) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userName = (String) authentication.getPrincipal();

    Optional<User> userNameOrEmailOptional = userRepository.findByUserNameOrEmail(userName, userName); // call, missing

    User userByUserName = userNameOrEmailOptional.orElseThrow(() ->
            new RunTimeExceptionPlaceHolder("UserName or Email doesn't exist!!") // call
    );

    if(updateUserRequest.getFirstName()!=null){ // call
      userByUserName.setFirstName(updateUserRequest.getFirstName()); // call // call
    }
    if(updateUserRequest.getLastName()!=null){ // call
      userByUserName.setLastName(updateUserRequest.getLastName()); // call // call
    }
    if(updateUserRequest.getPassword()!=null){ // call
      String encodedPassword = passwordEncoder.encode(updateUserRequest.getPassword()); // call // call
      userByUserName.setPassword(encodedPassword); // call
    }
    if(updateUserRequest.getEmail()!=null){ // call
      userByUserName.setEmail(updateUserRequest.getEmail()); // call // call
    }

    userRepository.save(userByUserName); // call, missing
  }

  @Override
  public void deleteUserById(String userId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userName = (String) authentication.getPrincipal();
    GetUserResponse userByUserId = getUserByUserId(userId); // call

    if(userName.equals(userByUserId.getUserName())){ // call
      throw new RunTimeExceptionPlaceHolder("You cannot delete your own account!"); // call
    }

    userRepository.deleteByUserId(userId); // call, missing
  }

  @Override
  public List<GetUserResponse> getAllUsers() {

    Iterable<User> all = userRepository.findAll(); // call, missing
    List<GetUserResponse> allUsers = new ArrayList<>();
    all.iterator().forEachRemaining(u->{
      GetUserResponse userResponse = GetUserResponse.builder() // call
              .userId(u.getUserId()) // call // call
              .userName(u.getUserName()) // call // call
              .firstName(u.getFirstName()) // call // call
              .lastName(u.getLastName()) // call // call
              .email(u.getEmail()) // call // call
              .roles(u.getRoles()) // call // call
              .build(); // call
      allUsers.add(userResponse);
    });

    return allUsers;
  }

  @Override
  public void updateUser(String userId, UpdateUserRequestFromAdmin updateUserRequestFromAdmin) {

    Optional<User> existingUser = userRepository.findByUserId(userId); // call, missing

    User user = existingUser.orElseThrow(() ->
            new RunTimeExceptionPlaceHolder("UserId doesn't exist!!") // call
    );

    if(updateUserRequestFromAdmin.getFirstName()!=null){ // call
      user.setFirstName(updateUserRequestFromAdmin.getFirstName()); // call // call
    }
    if(updateUserRequestFromAdmin.getLastName()!=null){ // call
      user.setLastName(updateUserRequestFromAdmin.getLastName()); // call // call
    }
    if(updateUserRequestFromAdmin.getEmail()!=null){ // call
      user.setEmail(updateUserRequestFromAdmin.getEmail()); // call // call
    }

    if(user.getRoles().size()>0){ // call
      MapUserToRolesRequest mapUserToRolesRequest = new MapUserToRolesRequest(); // call
      List<String> existingRoles = user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toList()); // call // call
      mapUserToRolesRequest.setRoleNames(existingRoles); // call
      userRoleService.removeRolesFromUser(user.getUserName(), mapUserToRolesRequest); // call // call
    }

    if (updateUserRequestFromAdmin.getRoles().size() > 0) { // call
      MapUserToRolesRequest mapUserToRolesRequest = new MapUserToRolesRequest(); // call
      mapUserToRolesRequest.setRoleNames(updateUserRequestFromAdmin.getRoles()); // call // call
      userRoleService.mapUserToRoles(user.getUserName(), mapUserToRolesRequest); // call // call
    }

    userRepository.save(user); // call, missing
  }

}
