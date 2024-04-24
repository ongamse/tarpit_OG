import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrderStatus extends javax.servlet.http.HttpServlet {

  private static final Logger LOGGER = LogManager.getLogger(OrderStatus.class);
  private Connection connection;
  private PreparedStatement preparedStatement;
  private ResultSet resultSet;

  private void getConnection() throws SQLException {
    String url = "jdbc:mysql://localhost:3306/mydb";
    String username = "root";
    String password = "password";
    connection = DriverManager.getConnection(url, username, password);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String orderId = request.getParameter("orderId");

    boolean keepOnline = (request.getParameter("keeponline") != null);

    try {

      String theUser = request.getParameter("userId");
      String thePassword = request.getParameter("password");
      request.setAttribute("callback", "/orderStatus.jsp");

      getServletContext().getRequestDispatcher("/login.jsp").forward(request, response);

      boolean loggedIn = request.isUserInRole("customer");

      if (loggedIn) {

        getConnection();

        String sql = "SELECT * FROM ORDER WHERE ORDERID = ?";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, orderId);

        resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {

          orderId = resultSet.getString("login");

          Order order = new Order(orderId,
              resultSet.getString("custId"),
              resultSet.getDate("orderDate"),
              resultSet.getString("orderStatus"),
              resultSet.getDate("shipDate"),
              resultSet.getString("creditCardNumber"),
              resultSet.getString("street"),
              resultSet.getString("city"),
              resultSet.getString("state"),
              resultSet.getString("zipCode"),
              resultSet.getString("emailAddress"));

          Cookie cookie = new Cookie("order", orderId);
          cookie.setMaxAge(864000);
          cookie.setPath("/");
          response.addCookie(cookie);

          request.setAttribute("orderDetails", order);

          LOGGER.info("Order details are " + order);

          getServletContext().getRequestDispatcher("/dashboard.jsp").forward(request, response);

        } else {

          request.setAttribute("message", "Order does not exist");

          LOGGER.info(" Order " + orderId + " does not exist ");

          getServletContext().getRequestDispatcher("/error.jsp").forward(request, response);
        }

      } else {

        getServletContext().getRequestDispatcher("/login.jsp").forward(request, response);
      }

    } catch (Exception e) {
      throw new ServletException(e);
    }

  }
}
