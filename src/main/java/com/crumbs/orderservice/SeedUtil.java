package com.crumbs.orderservice;

import com.crumbs.lib.repository.UserDetailsRepository;
import com.crumbs.orderservice.entity.*;
import com.crumbs.orderservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class SeedUtil implements ApplicationRunner {
    @Autowired CategoryRepository categoryRepository;
    @Autowired RestaurantRepository restaurantRepository;
    @Autowired MenuItemRepository menuItemRepository;
    @Autowired RestaurantCategoryRepository restaurantCategoryRepository;

    private final UserDetailsRepository userDetailsRepository;

    @Autowired
    SeedUtil(UserDetailsRepository userDetailsRepository){
        this.userDetailsRepository = userDetailsRepository;
    }

    private void makeRestaurants(){
        List<Category> categories = makeCategories();
        for (int i = 0; i < 10; i++) {
            Restaurant restaurant = Restaurant.builder()
                    .name("Restaurant-" + i)
                    .build();

            restaurant = restaurantRepository.save(restaurant);
            makeMenuItems(restaurant);

            for (int k = 0; k < 3; k++){
                int rand = new Random().nextInt(categories.size());
                RestaurantCategory restaurantCategory = new RestaurantCategory(categories.get(rand), restaurant);
                restaurantCategoryRepository.save(restaurantCategory);
            }
            restaurantRepository.save(restaurant);
        }
    }

    private void makeMenuItems(Restaurant restaurant){
        for (int i = 0; i < 10; i++){
            BigDecimal bd = BigDecimal.valueOf((i + 1F) * (float) Math.random() + 3)
                    .setScale(2, RoundingMode.HALF_UP);
            Float price = bd.floatValue();

            MenuItem menuItem = MenuItem.builder()
                    .name("MenuItem-"+i)
                    .price(price)
                    .description("Menu Item for a restaurant")
                    .build();

            menuItem.setRestaurant(restaurant);
            menuItemRepository.save(menuItem);
            restaurantRepository.save(restaurant);
        }
    }
    
    private List<Category> makeCategories(){
        Category burger = Category.builder().name("burger").build();
        Category wings = Category.builder().name("wings").build();
        Category chicken = Category.builder().name("chicken").build();
        Category chinese = Category.builder().name("chinese").build();
        Category seafood = Category.builder().name("seafood").build();
        Category vegan = Category.builder().name("vegan").build();

        categoryRepository.save(burger);
        categoryRepository.save(wings);
        categoryRepository.save(chicken);
        categoryRepository.save(chinese);
        categoryRepository.save(seafood);
        categoryRepository.save(vegan);

        return Arrays.asList(burger, wings, chicken, chinese, seafood, vegan);
    }

    private void makeCustomer(){
        UserDetails user = UserDetails.builder()
                .email("1@1.com")
                .firstName("1")
                .lastName("1")
                .username("hehe")
                .password("hehehe")
                .build();
        Customer customer = Customer.builder().userDetails(user).build();
        user.setCustomer(customer);
        userDetailsRepository.save(user);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        makeRestaurants();
        makeCustomer();
    }
}
