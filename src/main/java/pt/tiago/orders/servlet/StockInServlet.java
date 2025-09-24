package pt.tiago.orders.servlet;

import pt.tiago.orders.entity.StockMovement;
import pt.tiago.orders.service.StockService;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/stock-in")
public class StockInServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(StockInServlet.class.getName());

    @EJB
    private StockService stockService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain; charset=UTF-8");
        try {
            Long itemId = Long.parseLong(req.getParameter("itemId"));
            int qty = Integer.parseInt(req.getParameter("qty"));

            log.info("Recebido pedido de stock-in: itemId=" + itemId + ", qty=" + qty);

            StockMovement m = stockService.addInbound(itemId, qty);
            resp.getWriter().println("Stock movement id=" + m.getId() + " criado; qty=" + qty);

            log.info("Stock movement criado com sucesso: id=" + m.getId());
        } catch (NumberFormatException e) {
            log.log(Level.WARNING, "Parâmetros inválidos recebidos: itemId=" 
                    + req.getParameter("itemId") + ", qty=" + req.getParameter("qty"), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parâmetros inválidos");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Erro ao criar movimento de stock", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro no servidor");
        }
    }
}
