package com.glovoapp.backender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class CourierService {
    private CourierRepository courierRepository;

    @Value("${order.foodInBox}")
    private String foodInBox;

    @Value("${courier.highDistanceDefaultValue}")
    private double highDistanceDefaultValue;
    @Value("${courier.distanceRangeGrouping}")
    private double distanceRange;

    @Value("${order.customCompare}")
    private String customCompare;

    private static final Logger logger = Logger.getLogger("CourierService");

    @Autowired
    CourierService(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }


    public boolean canCarryBoxedItems(Courier courier, Order order) {
        if (null == courier || order == null)
            return false;

        List<String> foodInBoxList = Arrays.asList(foodInBox.split(","));

        boolean hasFoodInBox = foodInBoxList.stream().anyMatch(order.getDescription().toLowerCase()::contains);

        logger.info(String.format("canCarryBoxedItems:: Courier ID: %s | Order ID: %s | Order Description: %s | hasFoodInBox: %b | Courier has box: %b",
                courier.getId(), order.getId(), order.getDescription(), hasFoodInBox, courier.getBox()));

        return !hasFoodInBox || courier.getBox();
    }

    public boolean canMoveHighDistances(Courier courier, Order order) {
        if (null == courier || null == order)
            return false;

        List<Vehicle> moveHighDistanceVehicles = Arrays.asList(Vehicle.MOTORCYCLE, Vehicle.ELECTRIC_SCOOTER);

        double distance = DistanceCalculator.calculateDistance(courier.getLocation(), order.getPickup());
        boolean isHighDistance = distance > highDistanceDefaultValue;

        logger.info(String.format("canMoveHighDistances:: Courier ID: %s | Order ID: %s | Order Pickup: %s | Courier Location: %s | distance: %f | highDistanceDefaultValue: %f | isHighDistance: %b | Courier vehicle: %s",
                courier.getId(), order.getId(), order.getPickup(), courier.getLocation(), distance, highDistanceDefaultValue, isHighDistance, courier.getVehicle()));

        return !isHighDistance || moveHighDistanceVehicles.contains(courier.getVehicle());

    }

    public List<OrderVM> findAvailableOrdersByCourier(Courier courier, List<Order> orderList) {
        return orderList.stream()
                .filter(order -> canCarryBoxedItems(courier, order)) // If description contains pizza, cake or flamingo - validate Glovo Box
                .filter(order -> canMoveHighDistances(courier, order)) // If the order is further than 5km to the courier - only courier with motorcycle or electric scooter
                .sorted((o1, o2) -> this.customCompare(courier, o1, o2))
                .map(order -> new OrderVM(order.getId(), order.getDescription()))
                .collect(Collectors.toList());
    }

    public Courier findCourierById(String courierId) {
        return courierRepository.findById(courierId);
    }

    public int customCompare(Courier c, Order o, Order o1) {
        List<String> orderCompare = Arrays.asList(customCompare.split(","));

        int res = 0;

        double d = DistanceCalculator.calculateDistance(c.getLocation(), o.getPickup());
        double d1 = DistanceCalculator.calculateDistance(c.getLocation(), o1.getPickup());

        for (String order : orderCompare) {
            switch (order.toLowerCase()) {
                case "vip":
                    if (res == 0) res = customCompare_boolean(o1.getVip(), o.getVip()); // Invert result
                    break;
                case "range":
                    if (res == 0) res = customCompare_range(d, d1);
                    break;
                case "food":
                    if (res == 0) res = customCompare_boolean(o1.getFood(), o.getFood()); // Invert result
                    break;
                default:
                    if (res == 0) res = customCompare_double(d, d1);
            }
        }

        return res;
    }

    private int customCompare_double(double d, double d1) {
        return Double.compare(d, d1);
    }

    private int customCompare_range(double d, double d1) {
        int val = (int) (d / distanceRange);
        int val1 = (int) (d1 / distanceRange);

        return Integer.compare(val, val1);
    }

    private int customCompare_boolean(Boolean b, Boolean b1) {
        return Boolean.compare(b, b1);
    }

}
