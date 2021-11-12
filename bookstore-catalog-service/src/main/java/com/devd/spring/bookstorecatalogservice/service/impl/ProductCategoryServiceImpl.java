package com.devd.spring.bookstorecatalogservice.service.impl;

import com.devd.spring.bookstorecatalogservice.repository.dao.ProductCategory;
import com.devd.spring.bookstorecatalogservice.repository.ProductCategoryRepository;
import com.devd.spring.bookstorecatalogservice.service.ProductCategoryService;
import com.devd.spring.bookstorecatalogservice.web.CreateProductCategoryRequest;
import com.devd.spring.bookstorecatalogservice.web.UpdateProductCategoryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Optional;

/**
 * @author: Devaraj Reddy,
 * Date : 2019-06-06
 */
@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {

    @Autowired
    ProductCategoryRepository productCategoryRepository;

    @Override
    public String createProductCategory(
        @Valid CreateProductCategoryRequest createProductCategoryRequest) {

        ProductCategory productCategory = ProductCategory.builder() // call
                .productCategoryName(createProductCategoryRequest.getProductCategoryName()) // call // call
                .description(createProductCategoryRequest.getDescription()) // call // call
                .build(); // call

        ProductCategory savedProductCategory = productCategoryRepository.save(productCategory); // call
        return savedProductCategory.getProductCategoryId(); // call
    }

    @Override
    public ProductCategory getProductCategory(String productCategoryId) {

        Optional<ProductCategory> productCategoryOptional = productCategoryRepository.findById(productCategoryId); // call

        ProductCategory productCategory = productCategoryOptional.orElseThrow(() -> new RuntimeException("Product Category doesn't exist!"));

        return productCategory;
    }

    @Override
    public void deleteProductCategory(String productCategoryId) {

        productCategoryRepository.deleteById(productCategoryId); // call, missing

    }

    @Override
    public void updateProductCategory(UpdateProductCategoryRequest updateProductCategoryRequest) {

        //To check weather the ProductCategory exist.
        ProductCategory getProductCategory =
                this.getProductCategory(updateProductCategoryRequest.getProductCategoryId()); // call // call

        ProductCategory productCategory = ProductCategory.builder() // call
                .productCategoryId(updateProductCategoryRequest.getProductCategoryId()) // call // call
                .productCategoryName(updateProductCategoryRequest.getProductCategoryName()) // call // call
                .description(updateProductCategoryRequest.getDescription()) // call // call
                .build(); // call

        productCategory.setCreatedAt(getProductCategory.getCreatedAt()); // call // call

        productCategoryRepository.save(productCategory); // call, missing

    }
    
    @Override
    public Page<ProductCategory> getAllProductCategories(String sort, Integer page, Integer size) {
        
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
                throw new RuntimeException("Not a valid sort value, It should be 'fieldName,direction', example : 'productCategoryName,asc");
            }
            
        }
        
        return productCategoryRepository.findAll(pageable); // call, missing
    }
}
