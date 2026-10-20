package com.example.subscriptionbox.controller;
import com.example.subscriptionbox.model.*;import com.example.subscriptionbox.repository.*;import com.example.subscriptionbox.service.*;
import jakarta.validation.constraints.*;import org.springframework.http.HttpStatus;import org.springframework.web.bind.annotation.*;import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;import java.time.LocalDate;import java.util.*;import java.util.stream.Collectors;
@RestController @RequestMapping("/api")
public class CustomerController {
 private final CurrentUser current;private final CategoryRepository categories;private final PreferenceRepository preferences;private final SubscriptionRepository subscriptions;private final ProductRepository products;private final RatingRepository ratings;private final RecommendationService recommendations;private final OrderService orderService;
 public CustomerController(CurrentUser c,CategoryRepository ca,PreferenceRepository p,SubscriptionRepository s,ProductRepository pr,RatingRepository r,RecommendationService re,OrderService os){current=c;categories=ca;preferences=p;subscriptions=s;products=pr;ratings=r;recommendations=re;orderService=os;}
 public record PreferenceRequest(
		    List<Long> categoryIds,
		    Long categoryId,
		    @NotNull @DecimalMin("1.00") BigDecimal monthlyBudget,
		    String interests,
		    String avoidItems
		) {}
 public record SubscriptionRequest(@NotBlank String planName,@NotNull Long categoryId,@NotNull @DecimalMin("1.00") BigDecimal monthlyBudget){}
 public record RatingRequest(@NotNull Long productId,@Min(1) @Max(5) int score,String comment){}
 @PostMapping("/preferences")
 public Preference savePreference(@RequestBody PreferenceRequest req) {

     var user = current.get();

     List<Long> selectedIds = req.categoryIds();

     // Support the old single-category request too.
     if (selectedIds == null || selectedIds.isEmpty()) {
         if (req.categoryId() != null) {
             selectedIds = List.of(req.categoryId());
         } else {
             throw new ResponseStatusException(
                 HttpStatus.BAD_REQUEST,
                 "Please select at least one category"
             );
         }
     }

     var selectedCategories = selectedIds.stream()
         .distinct()
         .map(id -> categories.findById(id)
             .orElseThrow(() -> new ResponseStatusException(
                 HttpStatus.NOT_FOUND,
                 "Category not found: " + id
             )))
         .collect(Collectors.toSet());

     var preference = preferences.findByUserId(user.getId())
         .orElse(new Preference());

     preference.setUser(user);
     preference.setCategories(selectedCategories);

     // Keep the original category field compatible with older code.
     preference.setCategory(selectedCategories.iterator().next());

     preference.setMonthlyBudget(req.monthlyBudget());
     preference.setInterests(req.interests());
     preference.setAvoidItems(req.avoidItems());

     return preferences.save(preference);
 }
 @GetMapping("/preferences") public Preference getPreference(){return preferences.findByUserId(current.get().getId()).orElse(null);}
 @GetMapping("/recommendations") public List<Product> recommendations(){return recommendations.recommend(current.get());}
 @PostMapping("/subscriptions") public SubscriptionPlan subscribe(@RequestBody SubscriptionRequest req){
  var u=current.get();var cat=categories.findById(req.categoryId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Category not found"));
  SubscriptionPlan s=new SubscriptionPlan();s.setUser(u);s.setCategory(cat);s.setPlanName(req.planName());s.setMonthlyBudget(req.monthlyBudget());s.setNextOrderDate(LocalDate.now());s=subscriptions.save(s);orderService.generate(s);s.setNextOrderDate(LocalDate.now().plusMonths(1));return subscriptions.save(s);
 }
 @GetMapping("/subscriptions") public List<SubscriptionPlan> subscriptions(){return subscriptions.findByUserId(current.get().getId());}
 @PutMapping("/subscriptions/{id}/cancel") public SubscriptionPlan cancel(@PathVariable Long id){var s=subscriptions.findById(id).orElseThrow();if(!s.getUser().getId().equals(current.get().getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN);s.setActive(false);return subscriptions.save(s);}
 @GetMapping("/orders") public List<MonthlyOrder> orders(){return orderService.ordersFor(current.get());}
 @PostMapping("/ratings") public Rating rate(@RequestBody RatingRequest req){
  var u=current.get();var p=products.findById(req.productId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));
  var r=ratings.findByUserIdAndProductId(u.getId(),p.getId()).orElse(new Rating());r.setUser(u);r.setProduct(p);r.setScore(req.score());r.setComment(req.comment());return ratings.save(r);
 }
 @GetMapping("/ratings") public List<Rating> ratings(){return ratings.findByUserId(current.get().getId());}
}
