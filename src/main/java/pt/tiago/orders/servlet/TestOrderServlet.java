package pt.tiago.orders.servlet;

import pt.tiago.orders.entity.Order;
import pt.tiago.orders.service.OrderService;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/test-order")
public class TestOrderServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(TestOrderServlet.class.getName());

    @EJB
    private OrderService orderService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain; charset=UTF-8");

        try {
            Long itemId = Long.parseLong(req.getParameter("itemId"));
            Long userId = Long.parseLong(req.getParameter("userId"));
            int quantity = Integer.parseInt(req.getParameter("quantity"));

            log.info("Recebido pedido de criação de encomenda: userId=" 
                     + userId + ", itemId=" + itemId + ", quantity=" + quantity);

            Order order = orderService.createOrder(userId, itemId, quantity);
            resp.getWriter().println("Order criado com sucesso! ID: " + order.getId());

            log.info("Encomenda criada com sucesso. ID=" + order.getId());
        } catch (NumberFormatException e) {
            log.log(Level.WARNING, "Parâmetros inválidos recebidos: "
                    + "userId=" + req.getParameter("userId") 
                    + ", itemId=" + req.getParameter("itemId") 
                    + ", quantity=" + req.getParameter("quantity"), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parâmetros inválidos");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Erro inesperado ao criar encomenda", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro no servidor");
        }
    }
}
