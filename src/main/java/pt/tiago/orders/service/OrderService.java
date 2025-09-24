package pt.tiago.orders.service;

import pt.tiago.orders.entity.Item;
import pt.tiago.orders.entity.Order;
import pt.tiago.orders.entity.StockMovement;
import pt.tiago.orders.entity.User;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.Date;

@Stateless
public class OrderService {

    @PersistenceContext(unitName = "ordersPU")
    private EntityManager em;

    @EJB
    private StockService stockService;

    public Order createOrder(Long userId, Long itemId, int quantity) {

        User user = em.getReference(User.class, userId);
        Item item = em.getReference(Item.class, itemId);

        Order order = new Order();
        order.setUser(user);
        order.setItem(item);
        order.setQuantity(quantity);
        order.setCreationDate(LocalDateTime.now());

        em.persist(order);
        em.flush();

        stockService.allocateToPendingOrders(itemId);

        return order;
    }

    public String orderStatus(Long orderId) {
        Order o = em.find(Order.class, orderId);
        if (o == null) return "Order not found";

        Long allocated = em.createQuery(
                "select coalesce(sum(m.quantity), 0) " +
                "from StockMovement m " +
                "where m.order.id = :id and m.quantity < 0", Long.class)
            .setParameter("id", orderId)
            .getSingleResult();

        boolean complete = allocated != null && allocated.intValue() >= o.getQuantity();
        return String.format("Order %d - qty=%d allocated=%d complete=%s",
                o.getId(), o.getQuantity(), allocated, complete);
    }
}
