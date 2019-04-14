package com.glovoapp.backender;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableConfigurationProperties
@ExtendWith(SpringExtension.class)
public class CourierServiceTest {
    private OrderRepository orderRepository = new OrderRepository();
    private CourierRepository courierRepository = new CourierRepository();

    @Autowired
    private CourierService courierService;

    private List<Order> orders = orderRepository.findAll();
    private List<Courier> couriers = courierRepository.findAll();

    @Test
    void getCourierById() {
        Courier expected = couriers.get(0);

        assertEquals(expected, courierService.findCourierById(expected.getId()));
    }

    @Test
    void canCarryBoxedItems_nullOrder() {
        Order order = null;
        Courier courier = null;

        assertFalse(courierService.canCarryBoxedItems(courier, order));
    }

    @Test
    void canCarryBoxedItems_CourierWithBox_PizzaOrder_CanDelivery() {
        Courier courier = courierRepository.findById("courier-1");

        List<String> orderStringList = Collections.singletonList("order-1");
        Order order = getSpecificOrders(orderStringList).get(0);

        assertTrue(courierService.canCarryBoxedItems(courier, order));
    }

    @Test
    void canCarryBoxedItems_CourierWithBox_CakeOrder_CanDelivery() {
        Courier courier = courierRepository.findById("courier-1");

        List<String> orderStringList = Collections.singletonList("order-2");
        Order order = getSpecificOrders(orderStringList).get(0);

        assertTrue(courierService.canCarryBoxedItems(courier, order));
    }

    @Test
    void canCarryBoxedItems_CourierWithBox_FlamingoOrder_CanDelivery() {
        Courier courier = courierRepository.findById("courier-1");

        List<String> orderStringList = Collections.singletonList("order-3");
        Order order = getSpecificOrders(orderStringList).get(0);

        assertTrue(courierService.canCarryBoxedItems(courier, order));
    }

    @Test
    void canCarryBoxedItems_CourierWithBox_OtherOrder_CanDelivery() {
        Courier courier = courierRepository.findById("courier-1");

        List<String> orderStringList = Collections.singletonList("order-4");
        Order order = getSpecificOrders(orderStringList).get(0);

        assertTrue(courierService.canCarryBoxedItems(courier, order));
    }

    @Test
    void canCarryBoxedItems_CourierWithoutBox_CakeOrder_CannotDelivery() {
        Courier courier = courierRepository.findById("courier-2");

        List<String> orderStringList = Collections.singletonList("order-2");
        Order order = getSpecificOrders(orderStringList).get(0);

        assertFalse(courierService.canCarryBoxedItems(courier, order));
    }

    @Test
    void canCarryBoxedItems_CourierWithoutBox_OtherOrder_CanDelivery() {
        Courier courier = courierRepository.findById("courier-1");

        List<String> orderStringList = Collections.singletonList("order-4");
        Order order = getSpecificOrders(orderStringList).get(0);

        assertTrue(courierService.canCarryBoxedItems(courier, order));
    }

    @Test
    void canMoveHighDistances_CourierByMotorcycle_HighDistance_CanDelivery() {
        Courier courier = courierRepository.findById("courier-1");

        List<String> orderStringList = Collections.singletonList("order-4");
        Order order = getSpecificOrders(orderStringList).get(0);

        assertTrue(courierService.canMoveHighDistances(courier, order));
    }

    @Test
    void canMoveHighDistances_CourierByMotorcycle_LowDistance_CanDelivery() {
        Courier courier = courierRepository.findById("courier-1");

        List<String> orderStringList = Collections.singletonList("order-3");
        Order order = getSpecificOrders(orderStringList).get(0);

        assertTrue(courierService.canMoveHighDistances(courier, order));
    }

    @Test
    void canMoveHighDistances_CourierByBicycle_LowDistance_CanDelivery() {
        Courier courier = courierRepository.findById("courier-3");

        List<String> orderStringList = Collections.singletonList("order-3");
        Order order = getSpecificOrders(orderStringList).get(0);

        assertTrue(courierService.canMoveHighDistances(courier, order));
    }

    @Test
    void canMoveHighDistances_CourierByBicycle_HighDistance_CannotDelivery() {
        Courier courier = courierRepository.findById("courier-2");

        List<String> orderStringList = Collections.singletonList("order-4");
        Order order = getSpecificOrders(orderStringList).get(0);

        assertFalse(courierService.canMoveHighDistances(courier, order));
    }

    @Test
    void findAvailableOrdersByCourier_CourierWithoutBox_CannotDeliveryCakeOrder() {
        Courier courier = courierRepository.findById("courier-2");

        List<String> orderStringList = Arrays.asList("order-2", "order-4", "order-5", "order-6");
        List<Order> orderList = getSpecificOrders(orderStringList);

        List<OrderVM> availableOrders = courierService.findAvailableOrdersByCourier(courier, orderList);

        assertEquals(4, orderList.size());
        assertEquals(1, availableOrders.size());
    }

    @Test
    void findAvailableOrdersByCourier_CourierWithBox_CanDeliveryEverything() {
        Courier courier = courierRepository.findById("courier-1");

        List<String> orderStringList = Arrays.asList("order-2", "order-4", "order-5");
        List<Order> orderList = getSpecificOrders(orderStringList);

        List<OrderVM> availableOrders = courierService.findAvailableOrdersByCourier(courier, orderList);

        assertEquals(3, orderList.size());
        assertEquals(3, availableOrders.size());
    }

    @Test
    void findAvailableOrdersByCourier_CourierWithBox_CanDeliveryEverything_sorted() {
        Courier courier = courierRepository.findById("courier-1");

        List<String> orderStringList = Arrays.asList("order-2", "order-4", "order-5");
        List<Order> orderList = getSpecificOrders(orderStringList);

        List<OrderVM> availableOrders = courierService.findAvailableOrdersByCourier(courier, orderList);

        assertEquals(3, availableOrders.size());
        assertEquals("order-5", availableOrders.get(0).getId());
        assertEquals("order-2", availableOrders.get(1).getId());
        assertEquals("order-4", availableOrders.get(2).getId());
    }


    private List<Order> getSpecificOrders(List<String> list) {
        List<Order> orderList = new ArrayList<>();

        for (String s : list)
            orderList.add(orderRepository.findById(s));

        return orderList;
    }
}
