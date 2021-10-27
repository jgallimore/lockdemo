package org.superbiz.lockdemo.servlet;

import org.superbiz.lockdemo.application.DatabaseService;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

@WebServlet(urlPatterns = "/update")
public class UpdateServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DatabaseService.class.getName());

    @EJB
    private DatabaseService databaseService;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info(String.format("[%d] Starting Update Servlet", Thread.currentThread().getId()));

        final int rowsUpdated = databaseService.updateAndProcess();

        final PrintWriter writer = resp.getWriter();
        writer.println(String.format("%d rows updated.", rowsUpdated));
        writer.flush();

        LOGGER.info(String.format("[%d] Finished Update Servlet", Thread.currentThread().getId()));
    }
}
