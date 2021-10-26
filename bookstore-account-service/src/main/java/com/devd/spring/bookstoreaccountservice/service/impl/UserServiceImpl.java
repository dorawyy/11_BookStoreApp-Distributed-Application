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

    String encodedPassword = passwordEncoder.encode(createUserRequest.getPassword()); // call, missing

    if (userRepository.existsByUserName(createUserRequest.getUserName())) { // call, missing // call, missing
      throw new RunTimeExceptionPlaceHolder("Username is already taken!!"); // call, missing
    }

    if (userRepository.existsByEmail(createUserRequest.getEmail())) { // call, missing // call, missing
      throw new RunTimeExceptionPlaceHolder("Email address already in use!!"); // call, missing
    }

    ErrorResponse errorResponse = ErrorResponse.builder() // call, missing
        .uuid(UUID.randomUUID()) // call, missing
        .errors(new ArrayList<>()) // call, missing
        .build(); // call, missing

    List<Role> validRoles = new ArrayList<>();

    createUserRequest.getRoleNames().forEach(roleName -> { // call, missing

      //if role exists add to list and persist, else add to error response persist valid roles and send
      // response containing invalid roles.
      roleRepository.findByRoleName(roleName).<Runnable>map(role -> () -> validRoles.add(role)) // call, missing
          .orElse(() -> {
            Error error = Error.builder() // call, missing
                .code("400") // call, missing
                .message(roleName + " role doesn't exist") // call, missing
                .build(); // call, missing
            errorResponse.getErrors().add(error); // call, missing
          })
          .run(); // call, missing
    });

    User user = User.builder() // call, missing
        .userName(createUserRequest.getUserName()) // call, missing // call, missing
        .email(createUserRequest.getEmail()) // call, missing // call, missing
        .firstName(createUserRequest.getFirstName()) // call, missing // call, missing
        .lastName(createUserRequest.getLastName()) // call, missing // call, missing
        .password(encodedPassword) // call, missing
        .roles(new HashSet<>(validRoles)) // call, missing
        .build(); // call, missing

    User savedUser = userRepository.save(user); // call, missing

    if (!errorResponse.getErrors().isEmpty()) { // call, missing
      throw new SuccessCodeWithErrorResponse(savedUser.getUserId(), errorResponse); // call, missing // call, missing
    }

    return savedUser.getUserId(); // call, missing
  }

  @Override
  public GetUserResponse getUserByUserName(String userName) {

    Optional<User> userNameOrEmailOptional = userRepository
        .findByUserNameOrEmail(userName, userName); // call, missing
    User userByUserName = userNameOrEmailOptional.orElseThrow(() ->
        new RunTimeExceptionPlaceHolder("UserName or Email doesn't exist!!") // call, missing
    );

    return GetUserResponse.builder() // call, missing
        .userId(userByUserName.getUserId()) // call, missing // call, missing
        .userName(userByUserName.getUserName()) // call, missing // call, missing
        .firstName(userByUserName.getFirstName()) // call, missing // call, missing
        .lastName(userByUserName.getLastName()) // call, missing // call, missing
        .email(userByUserName.getEmail()) // call, missing // call, missing
        .roles(userByUserName.getRoles()) // call, missing // call, missing
        .build(); // call, missing
  }

  @Override
  public GetUserResponse getUserByUserId(String userId) {
    Optional<User> userIdOptional = userRepository.findByUserId(userId); // call, missing
    User userById = userIdOptional.orElseThrow(() ->
        new RunTimeExceptionPlaceHolder("UserName or Email doesn't exist!!") // call, missing
    );

    return GetUserResponse.builder() // call, missing
        .userId(userById.getUserId()) // call, missing // call, missing
        .userName(userById.getUserName()) // call, missing // call, missing
        .firstName(userById.getFirstName()) // call, missing // call, missing
        .lastName(userById.getLastName()) // call, missing // call, missing
        .email(userById.getEmail()) // call, missing // call, missing
        .roles(userById.getRoles()) // call, missing // call, missing
        .build(); // call, missing
  }

  @Override
  public GetUserInfoResponse getUserInfo() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userName = (String) authentication.getPrincipal();

    GetUserResponse userByUserName = getUserByUserName(userName);

    return GetUserInfoResponse.builder() // call, missing
        .userId(userByUserName.getUserId()) // call, missing // call, missing
        .userName(userByUserName.getUserName()) // call, missing // call, missing
        .firstName(userByUserName.getFirstName()) // call, missing // call, missing
        .lastName(userByUserName.getLastName()) // call, missing // call, missing
        .email(userByUserName.getEmail()) // call, missing // call, missing
        .build(); // call, missing

  }

  @Override
  public void updateUserInfo(UpdateUserRequest updateUserRequest) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userName = (String) authentication.getPrincipal();

    Optional<User> userNameOrEmailOptional = userRepository.findByUserNameOrEmail(userName, userName); // call, missing

    User userByUserName = userNameOrEmailOptional.orElseThrow(() ->
            new RunTimeExceptionPlaceHolder("UserName or Email doesn't exist!!") // call, missing
    );

    if(updateUserRequest.getFirstName()!=null){ // call, missing
      userByUserName.setFirstName(updateUserRequest.getFirstName()); // call, missing // call, missing
    }
    if(updateUserRequest.getLastName()!=null){ // call, missing
      userByUserName.setLastName(updateUserRequest.getLastName()); // call, missing // call, missing
    }
    if(updateUserRequest.getPassword()!=null){ // call, missing
      String encodedPassword = passwordEncoder.encode(updateUserRequest.getPassword()); // call, missing // call, missing
      userByUserName.setPassword(encodedPassword); // call, missing
    }
    if(updateUserRequest.getEmail()!=null){ // call, missing
      userByUserName.setEmail(updateUserRequest.getEmail()); // call, missing // call, missing
    }

    userRepository.save(userByUserName); // call, missing
  }

  @Override
  public void deleteUserById(String userId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userName = (String) authentication.getPrincipal();
    GetUserResponse userByUserId = getUserByUserId(userId); // call, missing

    if(userName.equals(userByUserId.getUserName())){ // call, missing
      throw new RunTimeExceptionPlaceHolder("You cannot delete your own account!"); // call, missing
    }

    userRepository.deleteByUserId(userId); // call, missing
  }

  @Override
  public List<GetUserResponse> getAllUsers() {

    Iterable<User> all = userRepository.findAll(); // call, missing
    List<GetUserResponse> allUsers = new ArrayList<>();
    all.iterator().forEachRemaining(u->{
      GetUserResponse userResponse = GetUserResponse.builder() // call, missing
              .userId(u.getUserId()) // call, missing // call, missing
              .userName(u.getUserName()) // call, missing // call, missing
              .firstName(u.getFirstName()) // call, missing // call, missing
              .lastName(u.getLastName()) // call, missing // call, missing
              .email(u.getEmail()) // call, missing // call, missing
              .roles(u.getRoles()) // call, missing // call, missing
              .build(); // call, missing
      allUsers.add(userResponse);
    });

    return allUsers;
  }

  @Override
  public void updateUser(String userId, UpdateUserRequestFromAdmin updateUserRequestFromAdmin) {

    Optional<User> existingUser = userRepository.findByUserId(userId); // call, missing

    User user = existingUser.orElseThrow(() ->
            new RunTimeExceptionPlaceHolder("UserId doesn't exist!!") // call, missing
    );

    if(updateUserRequestFromAdmin.getFirstName()!=null){ // call, missing
      user.setFirstName(updateUserRequestFromAdmin.getFirstName()); // call, missing // call, missing
    }
    if(updateUserRequestFromAdmin.getLastName()!=null){ // call, missing
      user.setLastName(updateUserRequestFromAdmin.getLastName()); // call, missing // call, missing
    }
    if(updateUserRequestFromAdmin.getEmail()!=null){ // call, missing
      user.setEmail(updateUserRequestFromAdmin.getEmail()); // call, missing // call, missing
    }

    if(user.getRoles().size()>0){ // call, missing
      MapUserToRolesRequest mapUserToRolesRequest = new MapUserToRolesRequest(); // call, missing
      List<String> existingRoles = user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toList()); // call, missing // call, missing
      mapUserToRolesRequest.setRoleNames(existingRoles); // call, missing
      userRoleService.removeRolesFromUser(user.getUserName(), mapUserToRolesRequest); // call, missing // call, missing
    }

    if (updateUserRequestFromAdmin.getRoles().size() > 0) { // call, missing
      MapUserToRolesRequest mapUserToRolesRequest = new MapUserToRolesRequest(); // call, missing
      mapUserToRolesRequest.setRoleNames(updateUserRequestFromAdmin.getRoles()); // call, missing // call, missing
      userRoleService.mapUserToRoles(user.getUserName(), mapUserToRolesRequest); // call, missing // call, missing
    }

    userRepository.save(user); // call, missing
  }

}
