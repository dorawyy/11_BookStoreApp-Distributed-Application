package com.devd.spring.bookstoreorderservice.service.impl;

import com.devd.spring.bookstoreorderservice.repository.dao.Cart;
import com.devd.spring.bookstoreorderservice.repository.dao.CartItem;
import com.devd.spring.bookstoreorderservice.repository.CartRepository;
import com.devd.spring.bookstoreorderservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

/**
 * @author: Devaraj Reddy,
 * Date : 2019-07-08
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Override
    public Cart getCart() {
    
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = (String) authentication.getPrincipal();
    
        Cart cartByUserName = cartRepository.findCartByUserName(userName); // call, missing

        synchronized (CartServiceImpl.class){
            if (cartByUserName == null) {
                createCart(); // call
                cartByUserName = cartRepository.findCartByUserName(userName); // call, missing
            }
        }
    
        double totalPrice = cartByUserName.getCartItems() // call
                                          .stream()
                                          .mapToDouble(CartItem::getExtendedPrice) // call
                                          .sum();
    
        cartByUserName.setTotalPrice(totalPrice); // call
    
        return cartByUserName;
    }
    
    @Override
    public Cart getCartByCartId(String cartId) {
        Optional<Cart> byCartId = cartRepository.findByCartId(cartId); // call, missing
        return byCartId.orElseThrow(() -> new RuntimeException("Cart doesn't exist!!"));
    }

    @Override
    public String createCart() {

        //Get the userName from the token.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = (String) authentication.getPrincipal();

        Cart cartByUserName = cartRepository.findCartByUserName(userName); // call, missing

        if (cartByUserName != null) {
            return cartByUserName.getCartId(); // call
        }

        Cart cart = Cart.builder() // call
                .cartItems(new ArrayList<>()) // call
                .userName(userName) // call
                .build(); // call

        Cart savedCart = cartRepository.save(cart); // call, missing

        return savedCart.getCartId(); // call

    }


    public Cart getCartByUserName(String userName) {

        Cart cartByUserName = cartRepository.findCartByUserName(userName); // call, missing
        return cartByUserName;
    }
}
