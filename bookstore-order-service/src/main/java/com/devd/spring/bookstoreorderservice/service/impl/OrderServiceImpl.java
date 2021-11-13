package com.devd.spring.bookstoreorderservice.service.impl;

import com.devd.spring.bookstorecommons.exception.RunTimeExceptionPlaceHolder;
import com.devd.spring.bookstorecommons.feign.BillingFeignClient;
import com.devd.spring.bookstorecommons.feign.PaymentFeignClient;
import com.devd.spring.bookstorecommons.util.CommonUtilityMethods;
import com.devd.spring.bookstorecommons.web.CreatePaymentRequest;
import com.devd.spring.bookstorecommons.web.CreatePaymentResponse;
import com.devd.spring.bookstorecommons.web.GetAddressResponse;
import com.devd.spring.bookstorecommons.web.GetPaymentMethodResponse;
import com.devd.spring.bookstoreorderservice.repository.OrderBillingAddressRepository;
import com.devd.spring.bookstoreorderservice.repository.OrderItemRepository;
import com.devd.spring.bookstoreorderservice.repository.OrderRepository;
import com.devd.spring.bookstoreorderservice.repository.OrderShippingAddressRepository;
import com.devd.spring.bookstoreorderservice.repository.dao.Cart;
import com.devd.spring.bookstoreorderservice.repository.dao.Order;
import com.devd.spring.bookstoreorderservice.repository.dao.OrderBillingAddress;
import com.devd.spring.bookstoreorderservice.repository.dao.OrderItem;
import com.devd.spring.bookstoreorderservice.repository.dao.OrderShippingAddress;
import com.devd.spring.bookstoreorderservice.service.CartItemService;
import com.devd.spring.bookstoreorderservice.service.CartService;
import com.devd.spring.bookstoreorderservice.service.OrderService;
import com.devd.spring.bookstoreorderservice.web.Card;
import com.devd.spring.bookstoreorderservice.web.CreateOrderRequest;
import com.devd.spring.bookstoreorderservice.web.CreateOrderResponse;
import com.devd.spring.bookstoreorderservice.web.PreviewOrderRequest;
import com.devd.spring.bookstoreorderservice.web.PreviewOrderResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Devaraj Reddy,
 * Date : 2019-09-20
 */
@Service
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    OrderRepository orderRepository;
    
    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    OrderBillingAddressRepository orderBillingAddressRepository;

    @Autowired
    OrderShippingAddressRepository orderShippingAddressRepository;

    @Autowired
    CartService cartService;

    @Autowired
    CartItemService cartItemService;

    @Autowired
    BillingFeignClient billingFeignClient;

    @Autowired
    PaymentFeignClient paymentFeignClient;

    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest createOrderRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdFromToken = CommonUtilityMethods.getUserIdFromToken(authentication); // call

        //TODO make transactional
        CreateOrderResponse createOrderResponse = new CreateOrderResponse(); // call

        //Get Billing Address
        GetAddressResponse billingAddress = null;
        if (createOrderRequest.getBillingAddressId() != null && !createOrderRequest.getBillingAddressId().isEmpty()) { // call // call
            billingAddress = billingFeignClient.getAddressById(createOrderRequest.getBillingAddressId()); // call, missing (s2s call) // call
            OrderBillingAddress orderBillingAddress = new OrderBillingAddress(); // call
            BeanUtils.copyProperties(billingAddress, orderBillingAddress); 
            createOrderResponse.setBillingAddress(orderBillingAddress); // call
        }

        //Get Shipping Address
        GetAddressResponse shippingAddress = null;
        if (createOrderRequest.getShippingAddressId() != null && !createOrderRequest.getShippingAddressId().isEmpty()) { // call // call
            shippingAddress = billingFeignClient.getAddressById(createOrderRequest.getShippingAddressId()); // call, missing (s2s call) // call
            billingAddress = shippingAddress;

            if (createOrderRequest.getBillingAddressId() == null) {
                OrderBillingAddress orderBillingAddress = new OrderBillingAddress(); // call
                BeanUtils.copyProperties(billingAddress, orderBillingAddress);
                createOrderResponse.setBillingAddress(orderBillingAddress); // call
            }
            OrderShippingAddress orderShippingAddress = new OrderShippingAddress(); // call
            BeanUtils.copyProperties(shippingAddress, orderShippingAddress);
            createOrderResponse.setShippingAddress(orderShippingAddress); // call
        }

        //Get Cart
        Cart cart = cartService.getCart(); // call

        if(cart.getCartItems().size()==0){ // call
            throw new RuntimeException("Cart is Empty");
        }

        Order order = new Order(); // call
        order.setUserName(cart.getUserName()); // call // call
        order.setUserId(userIdFromToken); // call

        cart.getCartItems() // call
                .forEach(cartItem -> {
                    OrderItem orderItem = new OrderItem(); // call
                    orderItem.setOrder(order); // call
                    orderItem.setOrderItemPrice(cartItem.getItemPrice()); // call // call
                    orderItem.setOrderExtendedPrice(cartItem.getExtendedPrice()); // call // call
                    orderItem.setProductId(cartItem.getProductId()); // call // call
                    orderItem.setOrderItemPrice(cartItem.getItemPrice()); // call // call
                    orderItem.setQuantity(cartItem.getQuantity()); // call // call
                    order.getOrderItems().add(orderItem); // call
                    createOrderResponse.getOrderItems().add(orderItem); // call
                });

        //HarCode to 10%
        double itemsPrice = createOrderResponse.getOrderItems().stream().mapToDouble(OrderItem::getOrderExtendedPrice).sum(); // call // call (lambda)
        createOrderResponse.setItemsTotalPrice(itemsPrice); // call
        order.setTotalItemsPrice(itemsPrice); // call

        Double taxPrice = (itemsPrice * 10) / 100;
        createOrderResponse.setTaxPrice(taxPrice); // call
        order.setTaxPrice(taxPrice); // call

        //Hardcode to 10
        Double shippingPrice = 10D;
        createOrderResponse.setShippingPrice(shippingPrice); // call
        order.setShippingPrice(shippingPrice); // call

        double totalPrice = itemsPrice + taxPrice + shippingPrice;
        createOrderResponse.setTotalPrice(totalPrice); // call
        order.setTotalOrderPrice(totalPrice); // call

        //Do Payment
        CreatePaymentRequest createPaymentRequest = new CreatePaymentRequest(); // call
        createPaymentRequest.setAmount((int)totalPrice*100); // call
        createPaymentRequest.setCurrency("USD"); // call
        createPaymentRequest.setPaymentMethodId(createOrderRequest.getPaymentMethodId()); // call // call

        CreatePaymentResponse createPaymentResponse = paymentFeignClient.doPayment(createPaymentRequest); // call, missing (s2s call)

        order.setPaid(createPaymentResponse.isCaptured()); // call // call
        order.setPaymentDate(createPaymentResponse.getPaymentDate()); // call // call
        order.setPaymentId(createPaymentResponse.getPaymentId()); // call // call
        order.setPaymentReceiptUrl(createPaymentResponse.getReceipt_url()); // call // call
        order.setPaymentMethodId(createOrderRequest.getPaymentMethodId()); // call // call
        Order save = orderRepository.save(order); // call, missing

        if (billingAddress != null) {
            OrderBillingAddress orderBillingAddress = OrderBillingAddress.builder() // call
                    .addressLine1(billingAddress.getAddressLine1()) // call // call
                    .addressLine2(billingAddress.getAddressLine2()) // call // call
                    .orderId(save.getOrderId()) // call // call
                    .city(billingAddress.getCity()) // call // call
                    .country(billingAddress.getCountry()) // call // call
                    .phone(billingAddress.getPhone()) // call // call
                    .postalCode(billingAddress.getPostalCode()) // call // call
                    .state(billingAddress.getState()) // call // call
                    .build(); // call
            orderBillingAddressRepository.save(orderBillingAddress); // call, missing
        }

        if (shippingAddress != null) {
            OrderShippingAddress orderShippingAddress = OrderShippingAddress.builder() // call
                    .addressLine1(shippingAddress.getAddressLine1()) // call // call
                    .addressLine2(shippingAddress.getAddressLine2()) // call // call
                    .orderId(save.getOrderId()) // call // call
                    .city(shippingAddress.getCity()) // call // call
                    .country(shippingAddress.getCountry()) // call // call
                    .phone(shippingAddress.getPhone()) // call // call
                    .postalCode(shippingAddress.getPostalCode()) // call // call
                    .state(shippingAddress.getState()) // call // call
                    .build(); // call
            orderShippingAddressRepository.save(orderShippingAddress); // call, missing
        }

        createOrderResponse.setOrderId(save.getOrderId()); // call // call
        createOrderResponse.setCreated_at(save.getCreatedAt()); // call // call

        //set Payment info
        createOrderResponse.setPaid(createPaymentResponse.isCaptured()); // call // call
        createOrderResponse.setPaymentDate(createPaymentResponse.getPaymentDate()); // call // call
        createOrderResponse.setPaymentReceiptUrl(createPaymentResponse.getReceipt_url()); // call // call

        //Clear cart
        cartItemService.removeAllCartItems(cart.getCartId()); // call // call
        return createOrderResponse;
    }

    @Override
    public PreviewOrderResponse previewOrder(PreviewOrderRequest previewOrderRequest) {

        PreviewOrderResponse previewOrderResponse = new PreviewOrderResponse(); // call

        if(previewOrderRequest.getBillingAddressId() != null && !previewOrderRequest.getBillingAddressId().isEmpty()){ // call // call
            GetAddressResponse billingAddress = billingFeignClient.getAddressById(previewOrderRequest.getBillingAddressId()); // call, missing (s2s call) // call
            previewOrderResponse.setBillingAddress(billingAddress); // call
        }

        if(previewOrderRequest.getShippingAddressId() != null && !previewOrderRequest.getShippingAddressId().isEmpty()){ // call // call
            GetAddressResponse shippingAddress = billingFeignClient.getAddressById(previewOrderRequest.getShippingAddressId()); // call, missing (s2s call) // call
            if (previewOrderRequest.getBillingAddressId() == null) { // call
                previewOrderResponse.setBillingAddress(shippingAddress); // call
            }
            previewOrderResponse.setShippingAddress(shippingAddress); // call
        }

        try{
            GetPaymentMethodResponse myPaymentMethodById = paymentFeignClient.getMyPaymentMethodById(previewOrderRequest.getPaymentMethodId()); // call, missing (s2s call) // call
            Card card = new Card(); // call
            card.setLast4Digits(myPaymentMethodById.getCardLast4Digits()); // call // call
            card.setCardBrand(myPaymentMethodById.getCardType()); // call // call
            card.setPaymentMethodId(myPaymentMethodById.getPaymentMethodId()); // call // call
            previewOrderResponse.setCard(card); // call
        }catch (Exception e){
            e.printStackTrace();
            throw new RunTimeExceptionPlaceHolder("Not a valid Payment Method");
        }

        Cart cart = cartService.getCart(); // call

        cart.getCartItems() // call
                .forEach(cartItem -> {
                    OrderItem orderItem = new OrderItem(); // call
                    orderItem.setOrderItemPrice(cartItem.getItemPrice()); // call // call
                    orderItem.setOrderExtendedPrice(cartItem.getExtendedPrice()); // call // call
                    orderItem.setProductId(cartItem.getProductId()); // call // call
                    orderItem.setOrderItemPrice(cartItem.getItemPrice()); // call // call
                    orderItem.setQuantity(cartItem.getQuantity()); // call // call
                    previewOrderResponse.getOrderItems().add(orderItem); // call
                });

        //HardCode to 10%
        double itemsPrice = previewOrderResponse.getOrderItems().stream().mapToDouble(OrderItem::getOrderExtendedPrice).sum(); // call // call
        previewOrderResponse.setItemsTotalPrice(itemsPrice); // call

        Double taxPrice = (itemsPrice * 10 ) / 100;
        previewOrderResponse.setTaxPrice(taxPrice); // call

        //Hardcode to 10
        Double shippingPrice = 10D;
        previewOrderResponse.setShippingPrice(shippingPrice); // call

        previewOrderResponse.setTotalPrice(itemsPrice + taxPrice + shippingPrice); // call

        return previewOrderResponse;
    }

    @Override
    public CreateOrderResponse getOrderById(String orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdFromToken = CommonUtilityMethods.getUserIdFromToken(authentication); // call

        Order order = orderRepository.findByOrderId(orderId); // call, missing
        if (order == null) {
            throw new RuntimeException("Order No Found");
        }

        if(!userIdFromToken.equals(order.getUserId())){ // call
            throw new RuntimeException("Order doesn't belong to this User! UnAuthorized!");
        }
        Card card = new Card(); // call
        try{
            GetPaymentMethodResponse myPaymentMethodById = paymentFeignClient.getMyPaymentMethodById(order.getPaymentMethodId()); // call, missing (s2s call) // call
            card.setLast4Digits(myPaymentMethodById.getCardLast4Digits()); // call // call
            card.setCardBrand(myPaymentMethodById.getCardType()); // call // call
            card.setPaymentMethodId(myPaymentMethodById.getPaymentMethodId()); // call // call
        }catch (Exception e){
            e.printStackTrace();
            throw new RunTimeExceptionPlaceHolder("Not a valid Payment Method");
        }


        OrderBillingAddress billingAddress = orderBillingAddressRepository.findByOrderId(orderId); // call, missing
        OrderShippingAddress shippingAddress = orderShippingAddressRepository.findByOrderId(orderId); // call, missing

        CreateOrderResponse createOrderResponse = CreateOrderResponse.builder() // call
                .orderId(orderId) // call
                .orderItems(order.getOrderItems()) // call // call
                .billingAddress(billingAddress) // call
                .shippingAddress(shippingAddress) // call
                .shippingPrice(order.getShippingPrice()) // call // call
                .card(card) // call
                .isDelivered(order.isDelivered()) // call // call
                .isPaid(order.isPaid()) // call // call
                .itemsTotalPrice(order.getTotalItemsPrice()) // call // call
                .paymentDate(order.getPaymentDate()) // call // call
                .paymentReceiptUrl(order.getPaymentReceiptUrl()) // call // call
                .taxPrice(order.getTaxPrice()) // call // call
                .totalPrice(order.getTotalOrderPrice()) // call // call
                .build(); // call

        return createOrderResponse;
    }

    @Override
    public List<CreateOrderResponse> getMyOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdFromToken = CommonUtilityMethods.getUserIdFromToken(authentication); // call
        List<Order> order = orderRepository.findByUserId(userIdFromToken); // call, missing

        return getCreateOrderResponses(order); // call
    }

    @Override
    public List<CreateOrderResponse> getAllOrders() {
        Iterable<Order> order = orderRepository.findAll(); // call, missing

        return getCreateOrderResponses(order); // call
    }

    private List<CreateOrderResponse> getCreateOrderResponses(Iterable<Order> order) {
        List<CreateOrderResponse> createOrderResponseList = new ArrayList<>();
        order.forEach(o->{
            String orderId = o.getOrderId(); // call
            OrderBillingAddress billingAddress = orderBillingAddressRepository.findByOrderId(orderId); // call, missing
            OrderShippingAddress shippingAddress = orderShippingAddressRepository.findByOrderId(orderId); // call, missing

            Card card = new Card(); // call
            try{
                GetPaymentMethodResponse myPaymentMethodById = paymentFeignClient.getMyPaymentMethodById(o.getPaymentMethodId()); // call, missing (s2s call) // call
                card.setLast4Digits(myPaymentMethodById.getCardLast4Digits()); // call // call
                card.setCardBrand(myPaymentMethodById.getCardType()); // call // call
                card.setPaymentMethodId(myPaymentMethodById.getPaymentMethodId()); // call // call
            }catch (Exception e){
                e.printStackTrace();
                throw new RunTimeExceptionPlaceHolder("Not a valid Payment Method");
            }

            CreateOrderResponse createOrderResponse = CreateOrderResponse.builder() // call
                    .orderId(orderId) // call
                    .orderItems(o.getOrderItems()) // call // call
                    .billingAddress(billingAddress) // call
                    .shippingAddress(shippingAddress) // call
                    .shippingPrice(o.getShippingPrice()) // call // call
                    .card(card) // call
                    .isDelivered(o.isDelivered()) // call // call
                    .isPaid(o.isPaid()) // call // call
                    .itemsTotalPrice(o.getTotalItemsPrice()) // call // call
                    .paymentDate(o.getPaymentDate()) // call // call
                    .paymentReceiptUrl(o.getPaymentReceiptUrl()) // call // call
                    .taxPrice(o.getTaxPrice()) // call // call
                    .totalPrice(o.getTotalOrderPrice()) // call // call
                    .created_at(o.getCreatedAt()) // call // call
                    .build(); // call
            createOrderResponseList.add(createOrderResponse);
        });

        return createOrderResponseList;
    }
}
