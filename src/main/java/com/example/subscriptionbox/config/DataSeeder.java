package com.example.subscriptionbox.config;
import com.example.subscriptionbox.model.*;import com.example.subscriptionbox.repository.*;
import org.springframework.boot.CommandLineRunner;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
@Configuration
public class DataSeeder {
 @Bean CommandLineRunner seed(CategoryRepository categories,ProductRepository products,UserRepository users,PasswordEncoder encoder){return args->{
  if(categories.count()==0){
   Category books=categories.save(new Category("Books","Fiction, non-fiction and learning books"));
   Category snacks=categories.save(new Category("Snacks","Tasty snacks for your monthly box"));
   Category skincare=categories.save(new Category("Skincare","Everyday skin care essentials"));
   products.save(product("Mystery Novel","A page-turning mystery story",12,"mystery,fiction,book",books));
   products.save(product("Productivity Journal","Plan goals and daily habits",9,"journal,productivity,stationery",books));
   products.save(product("Short Story Collection","A collection of short fiction",15,"fiction,stories,book",books));
   products.save(product("Trail Mix","Nuts and dried fruit snack",6,"healthy,nuts,snack",snacks));
   products.save(product("Dark Chocolate Bites","Rich dark chocolate",8,"chocolate,sweet,snack",snacks));
   products.save(product("Roasted Chickpeas","Crunchy savoury snack",5,"healthy,savoury,protein",snacks));
   products.save(product("Gentle Cleanser","Daily gentle face cleanser",11,"gentle,cleanser,sensitive",skincare));
   products.save(product("Hydrating Moisturizer","Lightweight daily hydration",14,"hydrating,moisturizer,dry",skincare));
   products.save(product("SPF 30 Sunscreen","Daily sun protection",13,"spf,sunscreen,protection",skincare));
  }
  if(!users.existsByEmail("admin@box.com")){AppUser a=new AppUser();a.setName("Demo Admin");a.setEmail("admin@box.com");a.setPassword(encoder.encode("Admin@123"));a.setRole(Role.ADMIN);users.save(a);}
 };}
 private Product product(String n,String d,double price,String tags,Category c){Product p=new Product();p.setName(n);p.setDescription(d);p.setPrice(BigDecimal.valueOf(price));p.setTags(tags);p.setCategory(c);return p;}
}
