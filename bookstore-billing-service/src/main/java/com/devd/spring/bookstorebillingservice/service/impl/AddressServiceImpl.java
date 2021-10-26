package com.devd.spring.bookstorebillingservice.service.impl;

import com.devd.spring.bookstorebillingservice.repository.AddressRepository;
import com.devd.spring.bookstorebillingservice.repository.dao.AddressDao;
import com.devd.spring.bookstorebillingservice.service.AddressService;
import com.devd.spring.bookstorebillingservice.web.CreateAddressRequest;
import com.devd.spring.bookstorebillingservice.web.GetAddressResponse;
import com.devd.spring.bookstorebillingservice.web.UpdateAddressRequest;
import com.devd.spring.bookstorecommons.util.CommonUtilityMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author: Devaraj Reddy, Date : 2019-09-20
 */
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    AddressRepository addressRepository;

    @Override
    public void createAddress(CreateAddressRequest createAddressRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdFromToken = CommonUtilityMethods.getUserIdFromToken(authentication); // call 

        AddressDao addressDao = AddressDao.builder() // call 
                .addressLine1(createAddressRequest.getAddressLine1()) // call // call 
                .addressLine2(createAddressRequest.getAddressLine2()) // call // call 
                .city(createAddressRequest.getCity()) // call // call 
                .country(createAddressRequest.getCountry()) // call // call 
                .phone(createAddressRequest.getPhone()) // call // call 
                .postalCode(createAddressRequest.getPostalCode()) // call // call 
                .state(createAddressRequest.getState()) // call // call 
                .userId(userIdFromToken) // call 
                .build(); // call 

        addressRepository.save(addressDao); // call 

    }


    @Override
    public List<GetAddressResponse> getAddress() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdFromToken = CommonUtilityMethods.getUserIdFromToken(authentication); // call 

        Optional<List<AddressDao>> addresses = addressRepository
                .findByUserId(userIdFromToken); // call 

        List<GetAddressResponse> responseList = new ArrayList<>();

        if (addresses.isPresent()) {
            addresses.get().forEach(address -> {
                responseList.add(GetAddressResponse.builder() // call 
                        .addressId(address.getAddressId()) // call // call 
                        .addressLine1(address.getAddressLine1()) // call // call 
                        .addressLine2(address.getAddressLine2()) // call // call 
                        .city(address.getCity()) // call // call
                        .country(address.getCountry()) // call // call
                        .phone(address.getPhone()) // call // call
                        .postalCode(address.getPostalCode()) // call // call
                        .state(address.getState()) // call // call
                        .userId(address.getUserId()) // call // call
                        .build()); // call
            });

            return responseList;
        }

        return new ArrayList<>();
    }

    @Override
    public void updateAddress(UpdateAddressRequest updateAddressRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdFromToken = CommonUtilityMethods.getUserIdFromToken(authentication); // call

        Optional<AddressDao> addressFromDb = addressRepository.findByAddressId(updateAddressRequest.getAddressId()); // call // call

        if (addressFromDb.isPresent()) {
            if (!userIdFromToken.equals(addressFromDb.get().getUserId())) { // call
                throw new RuntimeException("UnAuthorized!");
            }
        } else {
            throw new RuntimeException("Address doesn't exist!");
        }

        AddressDao addressDao = AddressDao.builder() // call
                .addressId(updateAddressRequest.getAddressId()) // call // call
                .addressLine1(updateAddressRequest.getAddressLine1()) // call // call
                .addressLine2(updateAddressRequest.getAddressLine2()) // call // call
                .city(updateAddressRequest.getCity()) // call // call
                .country(updateAddressRequest.getCountry()) // call // call
                .phone(updateAddressRequest.getPhone()) // call // call
                .postalCode(updateAddressRequest.getPostalCode()) // call // call
                .state(updateAddressRequest.getState()) // call // call
                .userId(userIdFromToken) // call
                .build(); // call

        addressRepository.save(addressDao); // call
    }

    @Override
    public GetAddressResponse getAddressById(String addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdFromToken = CommonUtilityMethods.getUserIdFromToken(authentication); // call

        Optional<AddressDao> addressOptional = addressRepository.findByAddressId(addressId); // call

        if (addressOptional.isPresent()) {
            AddressDao address = addressOptional.get();

            if (!address.getUserId().equals(userIdFromToken)) { // call
                throw new RuntimeException("UnAuthorized");
            }

            return GetAddressResponse.builder() // call
                    .addressId(address.getAddressId()) // call // call
                    .addressLine1(address.getAddressLine1()) // call // call
                    .addressLine2(address.getAddressLine2()) // call // call
                    .city(address.getCity()) // call // call
                    .country(address.getCountry()) // call // call
                    .phone(address.getPhone()) // call // call
                    .postalCode(address.getPostalCode()) // call // call
                    .state(address.getState()) // call // call
                    .userId(address.getUserId()) // call // call
                    .build(); // call
        }

        throw new RuntimeException("Address doesn't exist");
    }

    @Override
    public void deleteAddressById(String addressId) {
        getAddressById(addressId); // call
        addressRepository.deleteById(addressId); // call
    }
}

