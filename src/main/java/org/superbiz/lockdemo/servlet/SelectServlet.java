package org.superbiz.lockdemo.servlet;

import org.superbiz.lockdemo.application.DatabaseService;
import org.superbiz.lockdemo.model.Row;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(urlPatterns = "/select")
public class SelectServlet extends HttpServlet {

    @EJB
    private DatabaseService databaseService;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final PrintWriter writer = resp.getWriter();

        final List<Row> rows = databaseService.selectAll();

        for (final Row row : rows) {
            writer.println(row);
        }

        writer.flush();
    }
}
