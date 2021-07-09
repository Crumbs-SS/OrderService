//package com.crumbs.orderservice;
//
//import com.crumbs.lib.entity.*;
//import com.crumbs.lib.repository.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Random;
//
//@Component
//public class SeedUtil implements ApplicationRunner {
//
//    private final UserDetailsRepository userDetailsRepository;
//    private final MenuItemRepository menuItemRepository;
//    private final LocationRepository locationRepository;
//    private final RestaurantCategoryRepository restaurantCategoryRepository;
//    private final RestaurantRepository restaurantRepository;
//    private final CategoryRepository categoryRepository;
//
//
//    SeedUtil(UserDetailsRepository userDetailsRepository,
//             MenuItemRepository menuItemRepository,
//             LocationRepository locationRepository,
//             RestaurantCategoryRepository restaurantCategoryRepository,
//             RestaurantRepository restaurantRepository,
//             CategoryRepository categoryRepository){
//
//        this.userDetailsRepository = userDetailsRepository;
//        this.menuItemRepository = menuItemRepository;
//        this.locationRepository = locationRepository;
//        this.restaurantCategoryRepository = restaurantCategoryRepository;
//        this.restaurantRepository = restaurantRepository;
//        this.categoryRepository = categoryRepository;
//    }
//
//    private void makeRestaurants(UserDetails user){
//        List<Category> categories = makeCategories();
//        for (int i = 0; i < 10; i++) {
//            Location location = Location.builder()
//                    .street(i + "- Lane")
//                    .city(i + "- City")
//                    .state(i + "- State")
//                    .zipCode("12345")
//                    .build();
//
//
//            locationRepository.save(location);
//            Restaurant restaurant = Restaurant.builder()
//                    .name("Restaurant-" + i)
//                    .restaurantOwner(user.getOwner())
//                    .location(location)
//                    .restaurantStatus(RestaurantStatus.builder().status("REGISTERED").build())
//                    .build();
//
//            restaurant = restaurantRepository.save(restaurant);
//            makeMenuItems(restaurant);
//
//            for (int k = 0; k < 3; k++){
//                int rand = new Random().nextInt(categories.size());
//                RestaurantCategory restaurantCategory = new RestaurantCategory(categories.get(rand), restaurant);
//                restaurantCategoryRepository.save(restaurantCategory);
//            }
//            restaurantRepository.save(restaurant);
//        }
//    }
//
//    private void makeMenuItems(Restaurant restaurant){
//        for (int i = 0; i < 10; i++){
//            BigDecimal bd = BigDecimal.valueOf((i + 1F) * (float) Math.random() + 3)
//                    .setScale(2, RoundingMode.HALF_UP);
//            Float price = bd.floatValue();
//
//            MenuItem menuItem = MenuItem.builder()
//                    .name("MenuItem-"+i)
//                    .price(price)
//                    .description("Menu Item for a restaurant")
//                    .build();
//
//            menuItem.setRestaurant(restaurant);
//            menuItemRepository.save(menuItem);
//            restaurantRepository.save(restaurant);
//        }
//    }
//
//    private List<Category> makeCategories(){
//        Category burger = Category.builder().name("burger").build();
//        Category wings = Category.builder().name("wings").build();
//        Category chicken = Category.builder().name("chicken").build();
//        Category chinese = Category.builder().name("chinese").build();
//        Category seafood = Category.builder().name("seafood").build();
//        Category vegan = Category.builder().name("vegan").build();
//
//        categoryRepository.save(burger);
//        categoryRepository.save(wings);
//        categoryRepository.save(chicken);
//        categoryRepository.save(chinese);
//        categoryRepository.save(seafood);
//        categoryRepository.save(vegan);
//
//        return Arrays.asList(burger, wings, chicken, chinese, seafood, vegan);
//    }
//
//    private UserDetails makeCustomer(){
//        UserDetails user = UserDetails.builder()
//                .email("0@0.com")
//                .firstName("0")
//                .lastName("0")
//                .username("user0")
//                .password("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefgh")
//                .phone("1122334455")
//                .build();
//
//        UserStatus userStatus = UserStatus.builder().status("REGISTERED").build();
//
//        Customer customer = Customer.builder().userDetails(user).userStatus(userStatus).build();
//        Owner owner = Owner.builder().userDetails(user).userStatus(userStatus).build();
//
//        user.setOwner(owner);
//        user.setCustomer(customer);
//
//        userDetailsRepository.save(user);
//
//        return user;
//    }
//
//    public void dropSchema(){
//        categoryRepository.deleteAll();
//        userDetailsRepository.deleteAll();
//    }
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        dropSchema();
//        UserDetails user = makeCustomer();
//        makeRestaurants(user);
//    }
//}
