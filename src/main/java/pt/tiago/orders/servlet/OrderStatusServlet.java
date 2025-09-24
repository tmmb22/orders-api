package pt.tiago.orders.servlet;

import pt.tiago.orders.entity.Order;
import pt.tiago.orders.entity.StockMovement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/order-status")
public class OrderStatusServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(OrderStatusServlet.class.getName());

    @PersistenceUnit(unitName = "ordersPU")
    private EntityManagerFactory emf;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long orderId;
        try {
            orderId = Long.valueOf(req.getParameter("id"));
            log.info(() -> "Pedido de verificação de estado da encomenda: id=" + orderId);
        } catch (Exception e) {
            log.log(Level.WARNING, "Parâmetro inválido recebido: id=" + req.getParameter("id"), e);
            resp.sendError(400, "Parâmetro id inválido");
            return;
        }

        EntityManager em = emf.createEntityManager();
        try {
            Order o = em.find(Order.class, orderId);
            if (o == null) {
                log.warning(() -> "Encomenda não encontrada: id=" + orderId);
                resp.sendError(404, "Order not found");
                return;
            }

            List<StockMovement> movs = em.createQuery(
                    "select m from StockMovement m where m.order.id = :id order by m.creationDate",
                    StockMovement.class)
                .setParameter("id", orderId)
                .getResultList();

            int allocated = movs.stream()
                    .filter(m -> m.getQuantity() < 0)
                    .mapToInt(m -> -m.getQuantity())
                    .sum();

            boolean complete = allocated >= o.getQuantity();

            log.info(() -> String.format(
                    "Order id=%d verificada: qty=%d, allocated=%d, complete=%s",
                    o.getId(), o.getQuantity(), allocated, complete));

            resp.setContentType("text/plain; charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("Order " + o.getId() + " - qty=" + o.getQuantity());
            out.println("Allocated=" + allocated + " | complete=" + complete);
            movs.forEach(m -> out.println(
                    String.format("  [%s] movement id=%d qty=%d",
                            String.valueOf(m.getCreationDate()), m.getId(), m.getQuantity())));
        } finally {
            em.close();
        }
    }
}
