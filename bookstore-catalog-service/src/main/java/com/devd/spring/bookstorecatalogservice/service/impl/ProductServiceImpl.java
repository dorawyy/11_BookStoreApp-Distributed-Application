package com.devd.spring.bookstorecatalogservice.service.impl;

import com.devd.spring.bookstorecatalogservice.repository.ProductCategoryRepository;
import com.devd.spring.bookstorecatalogservice.repository.ProductRepository;
import com.devd.spring.bookstorecatalogservice.repository.ReviewRepository;
import com.devd.spring.bookstorecatalogservice.repository.dao.Product;
import com.devd.spring.bookstorecatalogservice.repository.dao.ProductCategory;
import com.devd.spring.bookstorecatalogservice.repository.dao.Review;
import com.devd.spring.bookstorecatalogservice.service.ProductService;
import com.devd.spring.bookstorecatalogservice.service.ReviewService;
import com.devd.spring.bookstorecatalogservice.web.CreateProductRequest;
import com.devd.spring.bookstorecatalogservice.web.ProductResponse;
import com.devd.spring.bookstorecatalogservice.web.UpdateProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * @author: Devaraj Reddy,
 * Date : 2019-06-06
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public String createProduct(@Valid CreateProductRequest createProductRequest) {

        Optional<ProductCategory> productCategoryOptional =
                productCategoryRepository.findById(createProductRequest.getProductCategoryId()); // call, missing // call

        ProductCategory productCategory = productCategoryOptional.orElseThrow(() -> new RuntimeException("ProductCategory doesn't exist!"));

        Product product = Product.builder() // call 
                .productName(createProductRequest.getProductName()) // call // call 
                .description(createProductRequest.getDescription()) // call // call 
                .availableItemCount(createProductRequest.getAvailableItemCount()) // call // call 
                .price(createProductRequest.getPrice()) // call // call
                .productCategory(productCategory) // call 
                .imageId(createProductRequest.getImageId()) // call // call
                .build(); // call


        Product savedProduct = productRepository.save(product); // call, missing
        return savedProduct.getProductId(); // call
    }

    @Override
    public ProductResponse getProduct(String productId) {
        Optional<Product> productOptional =
                productRepository.findById(productId); // call, missing

        Product product = productOptional.orElseThrow(() -> new RuntimeException("Product Id doesn't exist!"));
        ProductResponse productResponse = objectMapper.convertValue(product, ProductResponse.class);
        populateRatingForProduct(productId, productResponse); // call
        return productResponse;
    }

    //This way of setting rating for productResponse is not okay, But this is okay for now.
    private void populateRatingForProduct(String productId, ProductResponse productResponse) {
        List<Review> reviewsForProduct = reviewService.getReviewsForProduct(productId); // call
        if (reviewsForProduct.size() > 0) {
            double sum = reviewsForProduct.stream().mapToDouble(Review::getRatingValue).sum(); // call
            double rating = sum / reviewsForProduct.size();
            productResponse.setAverageRating(rating); // call
        }

        productResponse.setNoOfRatings(Math.toIntExact(reviewRepository.countAllByProductId(productId))); // call // call, missing
    }

    @Override
    public void deleteProduct(String productId) {

        productRepository.deleteById(productId); // call

    }

    @Override
    public void updateProduct(UpdateProductRequest updateProductRequest) {

        Optional<Product> productOptional =
                productRepository.findById(updateProductRequest.getProductId()); // call, missing // call

        //check weather product exists
        final Product productExisting = productOptional.orElseThrow(() -> new RuntimeException("Product Id doesn't exist!"));

        productExisting.setProductId(updateProductRequest.getProductId()); // call // call 

        if (updateProductRequest.getProductName() != null) { // call 
            productExisting.setProductName(updateProductRequest.getProductName()); // call // call
        }

        if (updateProductRequest.getDescription() != null) { // call 
            productExisting.setDescription(updateProductRequest.getDescription()); // call // call
        }

        if (updateProductRequest.getPrice() != null) { // call 
            productExisting.setPrice(updateProductRequest.getPrice()); // call // call
        }

        if (updateProductRequest.getImageId() != null) { // call 
            productExisting.setImageId(updateProductRequest.getImageId()); // call // call
        }

        if (updateProductRequest.getProductCategoryId() != null) { // call
            Optional<ProductCategory> productCategoryOptional =
                    productCategoryRepository.findById(updateProductRequest.getProductCategoryId()); // call, missing // call

            //check weather product category exists
            ProductCategory productCategory = productCategoryOptional.orElseThrow(() -> new RuntimeException("ProductCategory doesn't exist!"));
            productExisting.setProductCategory(productCategory); // call
        }

        if (updateProductRequest.getAvailableItemCount() != null) { // call
            productExisting.setAvailableItemCount(updateProductRequest.getAvailableItemCount()); // call // call
        }

        productExisting.setCreatedAt(productExisting.getCreatedAt()); // call // call

        productRepository.save(productExisting); // call, missing
    }

    @Override
    public Page<Product> findAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable); // call, missing
    }
    
    @Override
    public Page<ProductResponse> getAllProducts(String sort, Integer page, Integer size) {
        
        //set defaults
        if (size == null || size == 0) {
            size = 20;
        }
        
        //set defaults
        if (page == null || page == 0) {
            page = 0;
        }
        
        Pageable pageable;
        
        if (sort == null) {
            pageable = PageRequest.of(page, size);
        } else {
            Sort.Order order;
            
            try {
                String[] split = sort.split(",");
                
                Sort.Direction sortDirection = Sort.Direction.fromString(split[1]);
                order = new Sort.Order(sortDirection, split[0]).ignoreCase();
                pageable = PageRequest.of(page, size, Sort.by(order));
                
            } catch (Exception e) {
                throw new RuntimeException("Not a valid sort value, It should be 'fieldName,direction', example : 'productName,asc");
            }
            
        }
        Page<Product> allProducts = productRepository.findAll(pageable); // call, missing
        Page<ProductResponse> allProductsResponse = allProducts.map(Product::fromEntity); // call 
        allProductsResponse.forEach(productResponse -> populateRatingForProduct(productResponse.getProductId(), productResponse)); // call // call

        return allProductsResponse;
    }
}
