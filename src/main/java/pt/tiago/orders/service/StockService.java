package pt.tiago.orders.service;

import pt.tiago.orders.entity.Item;
import pt.tiago.orders.entity.Order;
import pt.tiago.orders.entity.StockMovement;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class StockService {

    @PersistenceContext(unitName = "ordersPU")
    private EntityManager em;

    public StockMovement addInbound(Long itemId, int qty) {
        Item item = em.find(Item.class, itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item não encontrado id=" + itemId);
        }

        StockMovement m = new StockMovement();
        m.setItem(item);
        m.setQuantity(qty);
        m.setCreationDate(LocalDateTime.now());
        em.persist(m);

        allocateToPendingOrders(itemId);

        return m;
    }

    public void allocateToPendingOrders(Long itemId) {
    	
        List<Order> pending = em.createQuery(
            "select o from Order o " +
            "where o.item.id = :itemId and o.quantity > (" +
            "  select -COALESCE(SUM(m.quantity), 0) " +
            "  from StockMovement m " +
            "  where m.order.id = o.id and m.quantity < 0" +
            ") order by o.creationDate asc",
            Order.class)
            .setParameter("itemId", itemId)
            .getResultList();

        for (Order o : pending) {
            int alreadyAllocated = allocatedQty(o.getId());
            int need = o.getQuantity() - alreadyAllocated;
            int available = freeBalance(itemId);
            if (need > 0 && available > 0) {
                int allocate = Math.min(need, available);

                StockMovement m = new StockMovement();
                m.setItem(o.getItem());
                m.setOrder(o);
                m.setQuantity(-allocate); // movimento de saída
                m.setCreationDate(LocalDateTime.now());
                em.persist(m);
            }
        }
    }

    private int allocatedQty(Long orderId) {
        Long total = em.createQuery(
            "select COALESCE(SUM(m.quantity), 0) " +
            "from StockMovement m " +
            "where m.order.id = :orderId and m.quantity < 0",
            Long.class)
            .setParameter("orderId", orderId)
            .getSingleResult();
        return total.intValue();
    }

    public int freeBalance(Long itemId) {
        Long total = em.createQuery(
            "select COALESCE(SUM(m.quantity), 0) " +
            "from StockMovement m " +
            "where m.item.id = :itemId",
            Long.class)
            .setParameter("itemId", itemId)
            .getSingleResult();
        return total.intValue();
    }

    public List<StockMovement> movementsByOrder(Long orderId) {
        return em.createQuery(
            "select m from StockMovement m where m.order.id = :orderId order by m.creationDate",
            StockMovement.class)
            .setParameter("orderId", orderId)
            .getResultList();
    }
}
