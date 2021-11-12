package com.devd.spring.bookstorepaymentservice.service.impl;

import com.devd.spring.bookstorecommons.exception.RunTimeExceptionPlaceHolder;
import com.devd.spring.bookstorepaymentservice.repository.UserPaymentCustomerRepository;
import com.devd.spring.bookstorepaymentservice.repository.dao.UserPaymentCustomer;
import com.devd.spring.bookstorepaymentservice.service.PaymentMethodService;
import com.devd.spring.bookstorepaymentservice.web.CreatePaymentMethodRequest;
import com.devd.spring.bookstorepaymentservice.web.GetPaymentMethodResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.devd.spring.bookstorecommons.util.CommonUtilityMethods.getUserIdFromToken;
import static com.devd.spring.bookstorecommons.util.CommonUtilityMethods.getUserNameFromToken;

/**
 * @author Devaraj Reddy, Date : 25-Jul-2020
 */
@Service
@Slf4j
public class PaymentMethodServiceImpl implements PaymentMethodService {

    @Autowired
    private UserPaymentCustomerRepository userPaymentCustomerRepository;

    public PaymentMethodServiceImpl() {
    }

    @Override
    public void createPaymentMethod(CreatePaymentMethodRequest createPaymentMethodRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdFromToken = getUserIdFromToken(authentication); // call, missing (due to class filtering)
        String userNameFromToken = getUserNameFromToken(authentication); // call, missing

        UserPaymentCustomer paymentCustomer = userPaymentCustomerRepository.findByUserId(userIdFromToken); // call, missing

        String customerId;
        if (paymentCustomer == null) {
            //Create Customer at stripe end;
            customerId = createCustomerAtStripe(); // call
            //save
            UserPaymentCustomer userPaymentCustomer = new UserPaymentCustomer(); // call
            userPaymentCustomer.setUserId(userIdFromToken); // call
            userPaymentCustomer.setUserName(userNameFromToken); // call
            userPaymentCustomer.setPaymentCustomerId(customerId); // call
            userPaymentCustomerRepository.save(userPaymentCustomer); // call, missing
        } else {
            customerId = paymentCustomer.getPaymentCustomerId(); // call
        }

        //create Payment Method
        String paymentMethod = createPaymentMethodAtStripe(createPaymentMethodRequest); // call

        //link customer and Payment Method
        linkCustomerAndPaymentMethod(paymentMethod, customerId); // call

    }

    @Override
    public List<GetPaymentMethodResponse> getAllMyPaymentMethods() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdFromToken = getUserIdFromToken(authentication); // call

        List<GetPaymentMethodResponse> list = new ArrayList<>();

        UserPaymentCustomer paymentCustomer = userPaymentCustomerRepository.findByUserId(userIdFromToken); // call, missing

        if (paymentCustomer != null) {
            PaymentMethodCollection paymentMethods = getAllPaymentMethodsForCustomerFromStripe(paymentCustomer.getPaymentCustomerId()); // call // call

            paymentMethods.getData().forEach(pm->{ // call
                GetPaymentMethodResponse getPaymentMethodResponse = GetPaymentMethodResponse.builder() // call
                        .paymentMethodId(pm.getId()) // call // call
                        .cardCountry(pm.getCard().getCountry()) // call // call
                        .cardExpirationMonth(pm.getCard().getExpMonth()) // call // call
                        .cardExpirationYear(pm.getCard().getExpYear()) // call // call
                        .cardLast4Digits(pm.getCard().getLast4()) // call // call
                        .cardType(pm.getCard().getBrand()) // call // call
                        .build(); // call

                list.add(getPaymentMethodResponse);
            });
        }

        return list;
    }

    @Override
    public GetPaymentMethodResponse getMyPaymentMethodById(String paymentMethodId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdFromToken = getUserIdFromToken(authentication); // call, missing

        UserPaymentCustomer paymentCustomer = userPaymentCustomerRepository.findByUserId(userIdFromToken); // call, missing

        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId); // call

            if(!paymentCustomer.getPaymentCustomerId().equals(paymentMethod.getCustomer())){ // call // call
                throw new RunTimeExceptionPlaceHolder("PaymentMethod doesn't belong to this User"); // call 
            }
            GetPaymentMethodResponse getPaymentMethodResponse = GetPaymentMethodResponse.builder() // call
                    .paymentMethodId(paymentMethod.getId()) // call // call
                    .cardCountry(paymentMethod.getCard().getCountry()) // call // call
                    .cardExpirationMonth(paymentMethod.getCard().getExpMonth()) // call // call
                    .cardExpirationYear(paymentMethod.getCard().getExpYear()) // call // call
                    .cardLast4Digits(paymentMethod.getCard().getLast4()) // call // call
                    .cardType(paymentMethod.getCard().getBrand()) // call // call
                    .build(); // call
            return getPaymentMethodResponse;
        } catch (StripeException e) {
            throw new RunTimeExceptionPlaceHolder("Error while fetching payment method."); // call 
        }
    }

    private PaymentMethodCollection getAllPaymentMethodsForCustomerFromStripe(String paymentCustomerId) {

        Map<String, Object> params = new HashMap<>();
        params.put("customer", paymentCustomerId);
        params.put("type", "card");

        try {
            return PaymentMethod.list(params);
        } catch (StripeException e) {
            throw new RunTimeExceptionPlaceHolder("Error while retrieving customer."); // call 
        }

    }

    private void linkCustomerAndPaymentMethod(String paymentMethodId, String customerId) {

        PaymentMethod paymentMethod = null;
        try {
            paymentMethod = PaymentMethod.retrieve(paymentMethodId);
        } catch (StripeException e) {
            throw new RunTimeExceptionPlaceHolder("Error while retrieving payment method."); // call 
        }

        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);

        try {
            PaymentMethod updatedPaymentMethod = paymentMethod.attach(params);
        } catch (StripeException e) {
            throw new RunTimeExceptionPlaceHolder("Error while attaching payment method."); // call 
        }

    }

    private String createPaymentMethodAtStripe(CreatePaymentMethodRequest createPaymentMethodRequest) {
        Map<String, Object> card = new HashMap<>();
        card.put("number", createPaymentMethodRequest.getCard().getCardNumber()); // call
        card.put("exp_month", createPaymentMethodRequest.getCard().getExpirationMonth()); // call
        card.put("exp_year", createPaymentMethodRequest.getCard().getExpirationYear()); // call
        card.put("cvc", createPaymentMethodRequest.getCard().getCvv()); // call
        Map<String, Object> params = new HashMap<>();
        params.put("type", "card");
        params.put("card", card);

        try {
            PaymentMethod paymentMethod = PaymentMethod.create(params);
            return paymentMethod.getId();
        } catch (StripeException e) {
            throw new RunTimeExceptionPlaceHolder("Error while setting up payment method."); // call 
        }
    }

    private String createCustomerAtStripe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdFromToken = getUserIdFromToken(authentication); // call, missing
        Map<String, Object> params = new HashMap<>();
        params.put(
                "description",
                "Creating Customer Account for UserId : " + userIdFromToken
        );

        try {
            return Customer.create(params).getId();
        } catch (StripeException e) {
            throw new RunTimeExceptionPlaceHolder("Error while setting up payment customer."); // call
        }

    }
}
