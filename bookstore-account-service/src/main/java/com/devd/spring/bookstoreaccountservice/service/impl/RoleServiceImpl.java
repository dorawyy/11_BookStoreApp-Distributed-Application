package com.devd.spring.bookstoreaccountservice.service.impl;

import com.devd.spring.bookstoreaccountservice.repository.RoleRepository;
import com.devd.spring.bookstoreaccountservice.repository.dao.Role;
import com.devd.spring.bookstoreaccountservice.service.RoleService;
import com.devd.spring.bookstoreaccountservice.web.CreateRoleRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Devaraj Reddy, Date : 2019-06-30
 */
@Service
public class RoleServiceImpl implements RoleService {

  @Autowired
  RoleRepository roleRepository;


  @Override
  public String createRole(CreateRoleRequest createRoleRequest) {

    Role role = Role.builder() // call
        .roleName(createRoleRequest.getRoleName()) // call // call
        .roleDescription(createRoleRequest.getRoleDescription()) // call // call
        .build(); // call

    Role savedRole = roleRepository.save(role); // call, missing (interface)
    return savedRole.getId(); // call
  }

  @Override
  public List<Role> getAllRoles() {
    List<Role> allRoles = roleRepository.findAll(); // call, missing (interface)
    return allRoles;
  }
}
