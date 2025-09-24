package pt.tiago.orders.servlet;

import pt.tiago.orders.entity.StockMovement;
import pt.tiago.orders.service.OrderService;
import pt.tiago.orders.service.StockService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/test-stock")
public class TestStockServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(TestStockServlet.class.getName());

    @Inject
    private StockService stockService;

    @Inject
    private OrderService orderService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null) action = "help";

        resp.setContentType("text/plain; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            switch (action) {
                case "add": {
                    Long itemId = parseLong(req.getParameter("itemId"));
                    Integer qty = parseInt(req.getParameter("qty"));
                    if (itemId == null || qty == null) {
                        log.warning("Parâmetros inválidos em 'add': itemId=" + req.getParameter("itemId")
                                + ", qty=" + req.getParameter("qty"));
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.println("Uso: /test-stock?action=add&itemId=1&qty=5");
                        break;
                    }
                    log.info("Entrada de stock: itemId=" + itemId + ", qty=" + qty);
                    StockMovement sm = stockService.addInbound(itemId, qty);
                    log.info("Movimento criado id=" + sm.getId() + " para itemId=" + itemId);
                    out.printf("OK: entrada de stock item=%d qty=%d%n", itemId, qty);
                    break;
                }

                case "orderStatus": {
                    Long orderId = parseLong(req.getParameter("orderId"));
                    if (orderId == null) {
                        log.warning("Parâmetro inválido em 'orderStatus': orderId=" + req.getParameter("orderId"));
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.println("Uso: /test-stock?action=orderStatus&orderId=1");
                        break;
                    }
                    String status = orderService.orderStatus(orderId);
                    log.info("orderStatus pedido: orderId=" + orderId + " -> " + status);
                    out.println(status);

                    List<StockMovement> allocs = stockService.movementsByOrder(orderId);
                    out.println("Movimentos da encomenda:");
                    for (StockMovement sm : allocs) {
                        out.printf("  %tF %<tT  qty=%d%n", sm.getCreationDate(), sm.getQuantity());
                    }
                    break;
                }

                case "freeBalance": {
                    Long itemId = parseLong(req.getParameter("itemId"));
                    if (itemId == null) {
                        log.warning("Parâmetro inválido em 'freeBalance': itemId=" + req.getParameter("itemId"));
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.println("Uso: /test-stock?action=freeBalance&itemId=1");
                        break;
                    }
                    int bal = stockService.freeBalance(itemId);
                    log.info("freeBalance: itemId=" + itemId + " -> " + bal);
                    out.printf("Saldo livre do item %d = %d%n", itemId, bal);
                    break;
                }

                default: {
                    out.println("Ações disponíveis:");
                    out.println("  /test-stock?action=add&itemId=1&qty=5");
                    out.println("  /test-stock?action=orderStatus&orderId=1");
                    out.println("  /test-stock?action=freeBalance&itemId=1");
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Erro no TestStockServlet (action=" + action + ")", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Erro: " + e.getMessage());
        }
    }

    private Long parseLong(String s) {
        try { return (s == null) ? null : Long.valueOf(s); } catch (Exception e) { return null; }
    }

    private Integer parseInt(String s) {
        try { return (s == null) ? null : Integer.valueOf(s); } catch (Exception e) { return null; }
    }
}
