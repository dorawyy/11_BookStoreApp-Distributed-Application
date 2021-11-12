package com.devd.spring.bookstoreorderservice.service.impl;

import com.devd.spring.bookstorecommons.feign.AccountFeignClient;
import com.devd.spring.bookstorecommons.feign.CatalogFeignClient;
import com.devd.spring.bookstorecommons.web.GetProductResponse;
import com.devd.spring.bookstoreorderservice.repository.CartItemRepository;
import com.devd.spring.bookstoreorderservice.repository.dao.Cart;
import com.devd.spring.bookstoreorderservice.repository.dao.CartItem;
import com.devd.spring.bookstoreorderservice.service.CartItemService;
import com.devd.spring.bookstoreorderservice.service.CartService;
import com.devd.spring.bookstoreorderservice.web.CartItemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: Devaraj Reddy,
 * Date : 2019-07-13
 */
@Service
public class CartItemServiceImpl implements CartItemService {

    @Autowired
    CartService cartService;

    @Autowired
    CatalogFeignClient catalogFeignClient;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    AccountFeignClient accountFeignClient;

    @Override
    public void addCartItem(CartItemRequest cartItemRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = (String) authentication.getPrincipal();
        Cart cartByUserName = cartService.getCartByUserName(userName); // call

        synchronized (CartServiceImpl.class) {
            if (cartByUserName == null) {
                //create cart for user if not exists.
                cartService.createCart(); // call
                cartByUserName = cartService.getCartByUserName(userName); // call
            }
        }
    
        GetProductResponse getProductResponse = catalogFeignClient.getProduct(cartItemRequest.getProductId()); // call (s2s call) // call

        if (cartItemRequest.getQuantity() > getProductResponse.getAvailableItemCount()) { // call // call
            throw new RuntimeException("Quantity is greater than available item count!");
        }

        //If the product already exists in the cart, update its quantity and itemPrice.

        if (cartByUserName.getCartItems() != null) { // call 
            for (CartItem ci : cartByUserName.getCartItems()) { // call
    
                if (getProductResponse.getProductId().equals(ci.getProductId())) { // call, missing (due to the common code not belonging to this app dir) // call
                    ci.setQuantity(cartItemRequest.getQuantity()); // call // call
                    ci.setItemPrice(getProductResponse.getPrice()); // call // call
                    ci.setExtendedPrice(ci.getQuantity() * getProductResponse.getPrice()); // call // call // call
                    cartItemRepository.save(ci); // call, missing
                    return;
                }
            }
        }

        //If cart doesn't have any cartItems, then create cartItems.
        CartItem cartItem = CartItem.builder() // call
                                    .cart(cartByUserName) // call
                                    .itemPrice(getProductResponse.getPrice())  // call // call 
                                    .extendedPrice(cartItemRequest.getQuantity() * getProductResponse.getPrice()) // call // call // call
                                    .quantity(cartItemRequest.getQuantity()) // call // call 
                                    .productId(getProductResponse.getProductId()) // call // call 
                                    .productName(getProductResponse.getProductName()) // call // call 
                                    .build(); // call

        cartItemRepository.save(cartItem); // call, missing
    }

    @Override
    public void removeCartItem(String cartItemId) {
        CartItem cartItem = this.getCartItem(cartItemId); // call
        cartItemRepository.delete(cartItem); // call, missing
    }

    @Override
    public CartItem getCartItem(String cartItemId) {
        Optional<CartItem> byCartItemId = cartItemRepository.findByCartItemId(cartItemId); // call, missing
        return byCartItemId.orElseThrow(()-> new RuntimeException("CartItem doesn't exist!!"));
    }

    @Override
    public void removeAllCartItems(String cartId) {

        Cart cart = cartService.getCartByCartId(cartId); // call
        List<String> cartItemIds = cart.getCartItems().stream().map(cartItem -> cartItem.getCartItemId()).collect(Collectors.toList()); // call // call
        if (!cartItemIds.isEmpty()) {
            cartItemIds.forEach(this::removeCartItem); // call (lambda)
        }
    }
}
