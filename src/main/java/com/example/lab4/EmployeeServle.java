package com.example.lab4;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "EmployeeServleServlet", value = "/EmployeeServlet")
public class EmployeeServle extends HttpServlet {

    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("EmployeePU");
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        String action = request.getParameter("action");
        if (action != null && action.equals("add")) {
            String name = request.getParameter("name");
            String surname = request.getParameter("surname");
            String position = request.getParameter("position");
            Employee newEmployee = new Employee(name, surname, position);
            em.persist(newEmployee);
        }
        if (action != null && action.equals("update")) {
            response.getWriter().println("<p>Nie ma takiego pracownika</p>");
            doPut(request,response);
        }

        tx.commit();
        em.close();
        response.sendRedirect("EmployeeServlet");
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        Employee empToUpdate = em.find(Employee.class, id);

        if (empToUpdate != null) {
            String newName = request.getParameter("name");
            String newSurname = request.getParameter("surname");
            String newPosition = request.getParameter("position");
            empToUpdate.setName(newName);
            empToUpdate.setSurname(newSurname);
            empToUpdate.setPosition(newPosition);
            try {
                tx.begin();
                em.merge(empToUpdate);
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                e.printStackTrace();
            } finally {
                em.close();
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("<p>Nie ma takiego pracownika</p>");
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        EntityManager em = emf.createEntityManager();
        String action = request.getParameter("action");
        List<Employee> employees = em.createQuery("SELECT e FROM Employee e", Employee.class).getResultList();
        if (action == null) {
            out.println("<ul>");
            out.println("<li><a href='EmployeeServlet?action=getall'>Wszystkie pracowniki</a></li>");
            out.println("<li>");
            out.println("<form action='EmployeeServlet' method='get'>");
            out.println("Pokaż pracownika o ID: ");
            out.println("<input type='number' name='id' min='1'>");
            out.println("<input type='hidden' name='action' value='get'>");
            out.println("<input type='submit' value='Pokaż'>");
            out.println("</form>");
            out.println("</li>");
            out.println("<li><a href='/Lab4_war_exploded/add.jsp'>Dodaj pracownika</a></li>");
            out.println("<li>");
            out.println("<form action='EmployeeServlet' method='get'>");
            out.println("Aktualizuj pracownika o ID: <input type='text' name='id'>");
            out.println("<input type='hidden' name='action' value='update'>");
            out.println("<input type='submit' value='Aktualizuj'>");
            out.println("</form>");
            out.println("</li>");
            out.println("</ul>");
        } else {
            switch (action) {
                case "get":
                    int id = Integer.parseInt(request.getParameter("id"));
                    Employee employee = em.find(Employee.class, id);
                    if (employee != null) {
                        out.println("<h2>Dane pracownika</h2>");
                        out.println("<p>ID: " + employee.getId() + "</p>");
                        out.println("<p>Imię: " + employee.getName() + "</p>");
                        out.println("<p>Nazwisko: " + employee.getSurname() + "</p>");
                        out.println("<p>Stanowisko: " + employee.getPosition() + "</p>");
                    } else {
                        out.println("<p>Niema takiego pracownika</p>");
                    }
                    break;
                case "getall":

                    out.println("<h2>Wszystkie pracowniki</h2>");
                    for (Employee emp : employees) {
                        out.println("<p>ID: " + emp.getId() + ", Name: " + emp.getName() + ", Surname: " + emp.getSurname() + ", Position: " + emp.getPosition() + "</p>");
                    }
                    break;
                case "update":
                    int updateId = Integer.parseInt(request.getParameter("id"));
                    request.setAttribute("id", updateId);
                    request.setAttribute("employees", employees);
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/add.jsp");
                    dispatcher.forward(request, response);
            }
        }

    }


}
