/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author User
 */
@WebServlet(urlPatterns = {"/ServletServer"})
public class ServletServer extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    
    private void processRequest(HttpServletRequest request, HttpServletResponse response, String method)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter output = response.getWriter()) {
            try {
                //Make a connection with the database.
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mersakom?zeroDateTimeBehavior=CONVERT_TO_NULL", "root", "");
//                JOptionPane.showMessageDialog(null, "connected");
                
                if(request.getParameter("ID") != null && request.getParameter("password") != null) {
                    String ID = request.getParameter("ID");
                    String password = request.getParameter("password");
                    //Make SQL Query to retrieve the last access time to the server by the employee.
                    PreparedStatement queryStatement = connection.prepareStatement("Select AccessTime from Employee where ID = ? AND password = ?");
                    queryStatement.setString(1, ID);
                    queryStatement.setString(2, password);
                    ResultSet result = queryStatement.executeQuery();
                    if (result.next()) {
                        String accessTime = result.getString("AccessTime");
                        output.println(method + ": Last Accesse Time: " + accessTime + ".");
                        
                        //Update the last access time to the server to the current time.
                        Date date = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String nowTime = dateFormat.format(date);
                        Timestamp dates = Timestamp.valueOf(nowTime);
                        
                        queryStatement = connection.prepareStatement("Update Employee set AccessTime = ? where ID = ? AND password = ?");
                        queryStatement.setTimestamp(1, dates);
                        queryStatement.setString(2, ID);
                        queryStatement.setString(3, password);
                        queryStatement.executeUpdate();
                    } 
                    else {
                        output.println(method + ": Please ensure to enter valid information!");
                    }
                }
                
                else if(request.getParameter("products") != null) {
                    Statement newStatement = connection.createStatement();
                    String query = "Select * from Products";
                    ResultSet result = newStatement.executeQuery(query);
                    int hasNext = 0;
                     while(result.next()) {
                        String ID = result.getString("ID");
                        String name = result.getString("Name");
                        String amount = result.getString("Amount");
                        String price = result.getString("Price");
                        output.println(ID + ":"+ name + ":" + amount + ":" + price);
                        hasNext += 1;
                     }
                     if(hasNext == 0) {
                         output.println("Mersakom doesn't have any products yet!");
                     }
                }
                
                else if (request.getParameter("PID") != null && request.getParameter("amount") != null && request.getParameter("Insert") != null) {
                    String PID = request.getParameter("PID");
                    String newAmount = request.getParameter("amount");
                    
                    PreparedStatement queryStatement = connection.prepareStatement("Select Amount from Products where ID = ?");
                    queryStatement.setString(1, PID);
                    ResultSet result = queryStatement.executeQuery();
                    
                    if (result.next()) {
                        String oldAmount = result.getString("Amount");
                        String totalAmount = String.valueOf(Integer.parseInt(oldAmount) + Integer.parseInt(newAmount));
                        queryStatement = connection.prepareStatement("Update Products set Amount = ? where ID = ?");
                        queryStatement.setString(1, totalAmount);
                        queryStatement.setString(2, PID);
                        int isUpdated = queryStatement.executeUpdate();
                        
                        if(isUpdated == 1) {
                            output.println("The total amount is updated to be " + totalAmount + ".");
                        }
                        else {
                            output.println("The total amount can't be updated!");
                        }
                    } 
                    else {
                        output.println("There is no such product with this ID!");
                    }
                }
                else if(request.getParameter("PID") != null && request.getParameter("amount") != null && request.getParameter("Withdraw") != null) {
                    String PID = request.getParameter("PID");
                    String withdrawnAmount = request.getParameter("amount");
                    
                    PreparedStatement queryStatement = connection.prepareStatement("Select Amount from Products where ID = ?");
                    queryStatement.setString(1, PID);
                    ResultSet result = queryStatement.executeQuery();
                    
                    if (result.next()) {
                        String oldAmount = result.getString("Amount");
                        if (Integer.parseInt(withdrawnAmount) <= Integer.parseInt(oldAmount)) {
                            String totalAmount = String.valueOf(Integer.parseInt(oldAmount) - Integer.parseInt(withdrawnAmount));
                            queryStatement = connection.prepareStatement("Update Products set Amount = ? where ID = ?");
                            queryStatement.setString(1, totalAmount);
                            queryStatement.setString(2, PID);
                            int isUpdated = queryStatement.executeUpdate();

                            if (isUpdated == 1) {
                                output.println("The total amount is updated to be " + totalAmount + ".");
                            } else {
                                output.println("The total amount can't be updated!");
                            }
                        } 
                        else {
                            output.println("The withdrawn process can't be completed!");
                        }
                    }
                    else {
                        output.println("There is no such product with this ID!");
                    }
                }
                
                else if(request.getParameter("PID") != null) {
                    String ProdID = request.getParameter("PID");
                    PreparedStatement queryStatement = connection.prepareStatement("Select * from Products where ID = ?");
                    queryStatement.setString(1, ProdID);
                    ResultSet result = queryStatement.executeQuery();
                    
                    if (result.next()) {
                        String name = result.getString("Name");
                        String amount = result.getString("Amount");
                        String price = result.getString("Price");
                        output.println(name + ":" + amount + ":" + price);
                    } 
                    else {
                        output.println("There is no such product with this ID!");
                    }
                }
            }
            catch (ClassNotFoundException ex) {
                Logger.getLogger(ServletServer.class.getName()).log(Level.SEVERE, null, ex);
            } 
            catch (SQLException ex) {
                Logger.getLogger(ServletServer.class.getName()).log(Level.SEVERE, null, ex);
            }    
        }
    }
        
        
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response, "Get");
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response, "Post");
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
