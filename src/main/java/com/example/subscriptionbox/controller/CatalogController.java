package com.example.subscriptionbox.controller;
import com.example.subscriptionbox.model.*;import com.example.subscriptionbox.repository.*;
import org.springframework.web.bind.annotation.*;import java.util.List;
@RestController @RequestMapping("/api")
public class CatalogController {
 private final CategoryRepository categories;private final ProductRepository products;
 public CatalogController(CategoryRepository c,ProductRepository p){categories=c;products=p;}
 @GetMapping("/categories") public List<Category> categories(){return categories.findAll();}
 @GetMapping("/products") public List<Product> products(){return products.findByActiveTrue();}
}
