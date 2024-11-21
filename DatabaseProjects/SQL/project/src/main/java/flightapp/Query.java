package flightapp;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Runs queries against a back-end database
 */
public class Query extends QueryAbstract {
  //
  // Canned queries
  //
  private static final String FLIGHT_CAPACITY_SQL = "SELECT capacity FROM Flights WHERE fid = ?";
  private static final String FIND_USER = "SELECT * FROM Users_fardeena WHERE username = ?";
  private PreparedStatement findUsers;
  private PreparedStatement flightCapacityStmt;
  private static final String FIND_RESERVATION = "SELECT * FROM Reservations_fardeena WHERE username = ? AND id = ?";
  private PreparedStatement findReservation;
  private static final String FLIGHT_COST = "SELECT price FROM Flights WHERE fid = ?";
  private PreparedStatement findCost;

  //
  // Instance variables
  //
  String loginCheck;
  List<Itinerary> list;

  
  protected Query() throws SQLException, IOException {
    prepareStatements();
  }

  /**
   * Clear the data in any custom tables created.
   * 
   * WARNING! Do not drop any tables and do not clear the flights table.
   */
  public void clearTables() {
    try {
      String rawQuery1 = "DELETE FROM Reservations_fardeena";
      PreparedStatement ps1 = conn.prepareStatement(rawQuery1);
      ps1.clearParameters();
      ps1.executeUpdate();
 
      String rawQuery2 = "DELETE FROM Users_fardeena";
      PreparedStatement ps2 = conn.prepareStatement(rawQuery2);
      ps2.clearParameters();
      ps2.executeUpdate();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * prepare all the SQL statements in this method.
   */
  private void prepareStatements() throws SQLException {
    flightCapacityStmt = conn.prepareStatement(FLIGHT_CAPACITY_SQL);
    findUsers = conn.prepareStatement(FIND_USER);
    findReservation = conn.prepareStatement(FIND_RESERVATION);
    findCost = conn.prepareStatement(FLIGHT_COST);
    
    // TODO: YOUR CODE HERE
    loginCheck = null;
  }









  /* See QueryAbstract.java for javadoc */
  public String transaction_login(String username, String password) {
    // Makes sure no one with the same username is logged in
    if(loginCheck != null) {
      return "User already logged in\n";
    } 
    try {
      // Retrieves user data from table
      findUsers.clearParameters();
      findUsers.setString(1,username);
      ResultSet rs = findUsers.executeQuery();
      // Checks if password match
      while(rs.next()) {
        boolean passwordTest = PasswordUtils.plaintextMatchesSaltedHash(password,rs.getBytes("password"));
        if(!passwordTest) {
          rs.close();
          return "Login failed\n";
        } else {
          loginCheck = username.toLowerCase();
          rs.close();
          return "Logged in as " + username + "\n";
        }
      }
      rs.close();
      // Case when there wasn't a user with that username in the database
      return "Login failed\n";
    } catch(Exception e) {
        e.printStackTrace();
        // Case for when there is an error
        return "Login failed\n";
    }    
  }








  /* See QueryAbstract.java for javadoc */
  public String transaction_createCustomer(String username, String password, int initAmount) {
    if(initAmount < 0 || username.length() > 20 || password.length() > 20) {
      return "Failed to create user\n";
    }
    try {
      conn.setAutoCommit(false);
      //Checks if username already exists
      findUsers.clearParameters();
      findUsers.setString(1,username);
      ResultSet rs = findUsers.executeQuery();
      // If user already exists, then don't create another user.
      if(rs.next()) {
        conn.rollback();
        conn.setAutoCommit(true);
        rs.close();
        return "Failed to create user\n";
      }
      // Creates the encrypted password, and then inserts new user into 
      // Users table.
      byte[] passwordHash = PasswordUtils.saltAndHashPassword(password);
      String insertNewUser = "INSERT INTO Users_fardeena VALUES (?,?,?)";
      PreparedStatement ps4 = conn.prepareStatement(insertNewUser);
      ps4.clearParameters();
      ps4.setString(1,username);
      ps4.setBytes(2,passwordHash);
      ps4.setInt(3, initAmount);
      ps4.executeUpdate();
      conn.commit();
      conn.setAutoCommit(true); 
      return "Created user " + username + "\n"; 
    } catch(Exception e) {
      if(e instanceof SQLException) {
        try {
          conn.rollback();
          conn.setAutoCommit(true);
          if(isDeadlock((SQLException)e)) {
            return transaction_createCustomer(username,password,initAmount);
          }
        } catch (SQLException e1) {
          // TODO Auto-generated catch block
        }
      }
      e.printStackTrace(); 
      return "Failed to create user\n";
    } 
   }


  // Takes in a row from ResultSet, and uses that to create Itinerary objects.
  /*
   * @param rs   Used to retrieve information about flights from the results of 
   *             a query
   * @param direct_flag      Determines of an Itinerary obj should account for it
   *                         being part of an indirect flight
   * @param itineraryID      The ID of the itinerary
   * @param tableName        The alias used for the table 
   * 
   * @return     Returns an Itinerary object. If the direct_flag was false(i.e. this
   *             was a indirect flight) we attach the second flight to the first and
   *             return them together. Otherwise, we don't attach it and just return
   *             an Itinerary object with inforamtion about 1 flight. 
   */
  private Itinerary create(ResultSet rs2, boolean direct_flag, int tableIndex) {
    try {
      Itinerary next = null;
      if(!direct_flag) {
        // If this is an indirect flight, we will have next equal the second flight.
        // To save reduncancy in retrieving information from the ResultSet, we can 
        // reuse the same code but with different table names(to get the same information, 
        // but for different flights). By storing this second flight inside the first, we 
        // can sort and print them together. 
        next = create(rs2,true, 9);
     } 
     // If direct, we would just use F1. But if indirect, we need information from both F1
     // and F3(these are alias of Flights table where F1 represents the first flight and
     // F3 represents the second flight)
  
     int result_dayOfMonth = rs2.getInt(2 + tableIndex);
     String result_carrierId = rs2.getString(3 + tableIndex);
     int result_fid = rs2.getInt(1 + tableIndex);
     int result_flightNum = rs2.getInt(4 + tableIndex);
     String result_originCity = rs2.getString(5 + tableIndex);
     String result_destCity = rs2.getString(6 + tableIndex);
     int result_time = rs2.getInt(7 + tableIndex);
     int result_capacity = rs2.getInt(8 + tableIndex);
     int result_price = rs2.getInt(9 + tableIndex);
     Itinerary obj = new Itinerary(
          result_dayOfMonth, result_carrierId,result_fid, 
          result_flightNum, result_originCity, 
          result_destCity, result_price, result_time, 
          result_capacity, next
        );
      return obj;
    } catch(Exception e) {
      e.printStackTrace();
      return null;
    }    
  }




  /* See QueryAbstract.java for javadoc */
  public String transaction_search(String originCity, String destinationCity, 
                                   boolean directFlight, int dayOfMonth,
                                   int numberOfItineraries) {
    // WARNING: the below code is insecure (it's susceptible to SQL injection attacks) AND only
    // handles searches for direct flights.  We are providing it *only* as an example of how
    // to use JDBC; you are required to replace it with your own secure implementation.
    //
    // TODO: YOUR CODE HERE
    if(numberOfItineraries < 0) {
      return "Failed to search\n";
    }
    try {
      String directSearch = "SELECT TOP (?) F1.fid, F1.day_of_month, F1.carrier_id, F1.flight_num, " +
      "F1.origin_city, F1.dest_city, F1.actual_time, F1.capacity, F1.price "
        + "FROM FLIGHTS AS F1 WHERE F1.origin_city = ? AND F1.dest_city = ? " +
        "AND F1.day_of_month = ? AND F1.canceled = 0 ORDER BY F1.actual_time ASC";

      PreparedStatement ps = conn.prepareStatement(directSearch);
      ps.clearParameters();
      ps.setInt(1,numberOfItineraries);
      ps.setString(2,originCity);
      ps.setString(3, destinationCity);
      ps.setInt(4,dayOfMonth);
      ResultSet rs = ps.executeQuery();
      // Used to store Itinerary objects, which we can then sort and print
      List<Itinerary> temp = new ArrayList<>();
      while (rs.next()) {
        Itinerary obj = create(rs,true, 0);
        temp.add(obj);
      }
      rs.close();
      // Finds indirect flights, if the user also wanted indirect flights
      if(numberOfItineraries != temp.size() && !directFlight) {
        String indirectSearch = "SELECT TOP (?) F1.fid,F1.day_of_month,F1.carrier_id,F1.flight_num,F1.origin_city, " +
                       "F1.dest_city,F1.actual_time,F1.capacity,F1.price, " +
                        "F3.fid,F3.day_of_month,F3.carrier_id,F3.flight_num,F3.origin_city, " +
                        "F3.dest_city,F3.actual_time,F3.capacity,F3.price "+
                        "FROM FLIGHTS AS F1, FLIGHTS AS F3 " +
                       "WHERE F1.origin_city = ? AND " +
                        "F1.dest_city = F3.origin_city AND F3.dest_city = ? AND " +
                        "F1.canceled = 0 AND F3.canceled = 0 AND F1.day_of_month = F3.day_of_month " +
                        "AND F1.day_of_month = ? ORDER BY F1.actual_time + F3.actual_time";  
      PreparedStatement ps2 = conn.prepareStatement(indirectSearch);
      ps2.clearParameters();
      ps2.setInt(1,numberOfItineraries - temp.size());
      ps2.setString(2,originCity);
      ps2.setString(3,destinationCity);
      ps2.setInt(4,dayOfMonth);
      ResultSet rs2 = ps2.executeQuery();
      while(rs2.next()) {    
        Itinerary obj = create(rs2,false, 0);
        temp.add(obj);        
       }
       rs2.close();
      }
      list = temp;
      if(list.size() == 0) {
        return "No flights match your selection\n";
      }
      // Sorts all the intineraries
      Collections.sort(list);
      // Outputs the string results of the Itinerary
      String result = "";
      for(int i = 0; i<list.size(); i++) {
        int itineraryID = i;
        int totalMin = list.get(i).time;
        int totalFlights = 1;
        list.get(i).itineraryID = i;
        if(list.get(i).next != null) {
          totalMin += list.get(i).next.time;
          totalFlights++;
        }
        String intro = "Itinerary " + itineraryID + ": " + totalFlights + 
      " flight(s), " + totalMin + " minutes\n";
        result += intro + list.get(i).toString();
      } 
      if(loginCheck == null) {
        list = null;
      }
      return result;
    } catch (SQLException e) {
      e.printStackTrace();
      return "Failed to search\n";
    }
  }



  /* See QueryAbstract.java for javadoc */
  public String transaction_book(int itineraryId) {
    if(loginCheck == null) {
      return "Cannot book reservations, not logged in\n";
    }
    int indexOfItin = binarySearch(itineraryId);
    if (list == null || indexOfItin == -1) {
      return "No such itinerary " + itineraryId + "\n";
    }
    boolean check = false;
    int countReservations = 1;
    int flight1Reserves = 0;
    int flight2Reserves = 0;
    int FID1 = list.get(indexOfItin).fid;
    Integer FID2 = null;
    if(list.get(indexOfItin).next != null) {
      FID2 = list.get(indexOfItin).next.fid;
    }
    try {
      // –--------------------------------------------------------
        // Surrounding each collection of SQL statements which
        // constitute a single logical transaction:

        // Disable the one-statement-per-transaction behavior:
          conn.setAutoCommit(false);
          String getReservation = "SELECT * FROM Reservations_fardeena";
          PreparedStatement ps = conn.prepareStatement(getReservation);
        //
        // ... execute your updates and queries ...
        // 

        ResultSet rs = ps.executeQuery();
         
        

        
      while(rs.next()) {
        // Test case to make sure two reservations aren't booked on the same day
        if(rs.getString("username").toLowerCase().equals(loginCheck.toLowerCase())) {
          String getDay = "SELECT * FROM FLIGHTS WHERE fid = ?";
          PreparedStatement ps2 = conn.prepareStatement(getDay);
          ps2.setInt(1,rs.getInt("flight1_id"));
          ResultSet rs2 = ps2.executeQuery();
          while(rs2.next()) {
            if(rs2.getInt("day_of_month") == list.get(indexOfItin).dayOfMonth) {
              rs.close();
              rs2.close();
              conn.rollback();
              conn.setAutoCommit(true);
              return "You cannot book two flights in the same day\n";
            }
          }
          rs2.close();
        }
        // Used to keep track of total reservations for flights inside our itinerary
        if(rs.getInt("flight1_id") == FID1 || rs.getInt("flight2_id") == FID1) {
          flight1Reserves++;
        }
        if(FID2 != null && (rs.getInt("flight1_id") == FID2 || rs.getInt("flight2_id") == FID2)) {
          flight2Reserves++;
        }
        countReservations++;
      }
      rs.close();
      // Checks to make sure there is capacity/room in the booked flights
      check = checkCapacityForItinerary(FID1, FID2, flight1Reserves, flight2Reserves);
      if(!check) {
        conn.rollback();
        conn.setAutoCommit(true);
        return "Booking failed\n";
      }
      // Past this point, booking is allowed
      String insertReservations = "INSERT INTO Reservations_fardeena VALUES (?,?,?,?,?)";
      PreparedStatement ps3 = conn.prepareStatement(insertReservations);
      ps3.clearParameters();
      ps3.setInt(1,countReservations);
      ps3.setString(2, loginCheck);
      ps3.setInt(3,0);
      ps3.setInt(4, FID1);
      if(FID2 != null) {
        ps3.setInt(5, FID2);
      } else {
         ps3.setNull(5, java.sql.Types.INTEGER);
      }
      ps3.executeUpdate();
       // If a reservation id already exists, include countreservations
         // return transaction_book(itineraryId + 1);
        
      conn.commit();
      // Undo the changes to your transaction settings; future SQL
      // statements will execute as individual transactions
      conn.setAutoCommit(true); 
      return "Booked flight(s), reservation ID: " + countReservations + "\n";
    } catch (Exception e) { 
      if(e instanceof SQLException) {
        try {
          conn.rollback();
          conn.setAutoCommit(true);
          if(isDeadlock((SQLException) e)) {
           return transaction_book(itineraryId);
          } 
        } catch (SQLException e1) {
          return "Booking failed\n";
        }
      }
        e.printStackTrace();
        return "Booking failed\n";
    }
    
  }


// helper method to clean up code
  private boolean checkCapacityForItinerary(int fid1, Integer fid2, 
                  int flight1Reserve, int flight2Reserve) throws SQLException {
    try {
      int flight1Capacity = checkFlightCapacity(fid1);
      // Used Integer.MAX_Value since I know no flight capacity is this large
      int flight2Capacity = Integer.MAX_VALUE;
      if(fid2 != null) {
       flight2Capacity = checkFlightCapacity(fid2);
      } 
      if(flight1Reserve >= flight1Capacity ||  flight2Reserve >= flight2Capacity) {
        return false;
     }
      return true;
    } catch(Exception e) {
      e.printStackTrace();
      /* 
      conn.rollback();
      conn.setAutoCommit(true);
      */
      return false;
    }
}






  

  /* See QueryAbstract.java for javadoc */
  public String transaction_pay(int reservationId) {
    try {   
      if(loginCheck == null) {
        return "Cannot pay, not logged in\n";
      }
      // Gets the reservation
      findReservation.clearParameters();
      findReservation.setString(1,loginCheck);
      findReservation.setInt(2, reservationId);
      conn.setAutoCommit(false);
      ResultSet rs = findReservation.executeQuery();
      // Gets user info
      findUsers.clearParameters();
      findUsers.setString(1,loginCheck);
      ResultSet rs2 = findUsers.executeQuery();
      // If reservation doesn't exist or is already paid
      if(!rs.next() || rs.getInt(3) == 1) {
        rs.close();
        rs2.close();
        conn.rollback();
        conn.setAutoCommit(true);
        return "Cannot find unpaid reservation " + reservationId + " under user: " + loginCheck + "\n";
      }
      // If user doesn't exist
      if(!rs2.next()) {
        rs.close();
        rs2.close();
        conn.rollback();
        conn.setAutoCommit(true);
       return "Failed to pay for reservation " + reservationId + "\n";
      } 
      // Begins calculating price of itinerary 
      int balance = rs2.getInt(3);
      int FID1 = rs.getInt(4); // Stores id for first flight
      int FID2 = rs.getInt(5); // Stores id for second flight, if exists
      rs.close();
      rs2.close();
      int F1Cost = 0; // Stores cost of first flight
      int F2Cost = 0; //  Stores cost of second flight, if exists
      findCost.clearParameters();
      findCost.setInt(1,FID1);
      ResultSet rs3 = findCost.executeQuery();
      // Safe check, pretty sure don't need if-statement
      // Gets cost of first flight
      if(rs3.next()) {
        F1Cost = rs3.getInt(1);        
      }
      rs3.close();
      // Gets cost of second flight, if there is a second flight in the itinerary
      if(FID2 != 0) {
        findCost.clearParameters();
        findCost.setInt(1,FID2);          
        ResultSet rs4 = findCost.executeQuery();
        if(rs4.next()) {
          F2Cost = rs4.getInt(1);
        }
        rs4.close();
       }
       // Case when price of itinerary exceeds users balance
       if((F1Cost + F2Cost) > balance ) {
        conn.rollback();
        conn.setAutoCommit(true);
        return "User has only " + balance + " in account but itinerary costs " + (F1Cost + F2Cost) + "\n";
       } 

       // Begin updating balance for user
        String updateBalance = "UPDATE Users_fardeena SET balance = ? WHERE username = ?";
        PreparedStatement ps = conn.prepareStatement(updateBalance);
        ps.clearParameters();
        ps.setInt(1, balance - (F1Cost + F2Cost));
        ps.setString(2, loginCheck);
        // Indicate reservation has been paid for
        String updateReservation = "UPDATE Reservations_fardeena SET paid_flag = ? WHERE id = ?";
        PreparedStatement ps2 = conn.prepareStatement(updateReservation);
        ps2.clearParameters();
        ps2.setInt(1, 1);
        ps2.setInt(2, reservationId);
        // Execute our updates
        ps2.executeUpdate();
        ps.executeUpdate();
        conn.commit();
        conn.setAutoCommit(true);

       return "Paid reservation: " + reservationId + " remaining balance: " + (balance - (F1Cost + F2Cost)) + "\n";  
    } catch(Exception e) {
      if(e instanceof SQLException) {
        try {
          conn.rollback();
          conn.setAutoCommit(true); 
          if(isDeadlock((SQLException) e)) {
           return transaction_pay(reservationId);
          }
        } catch (SQLException e1) {
      
        }
      }
      e.printStackTrace();
      return "Failed to pay for reservation " + reservationId + "\n";
    }
  }







  /* See QueryAbstract.java for javadoc */
  public String transaction_reservations() {
    try {
      if(loginCheck == null) {
        return "Cannot view reservations, not logged in\n";
      }
      String userReservation = "SELECT * FROM Reservations_fardeena where username = ?";
      PreparedStatement ps = conn.prepareStatement(userReservation);
      ps.clearParameters();
      ps.setString(1,loginCheck);
      ResultSet rs = ps.executeQuery();
      String output = "";
      while(rs.next()) {
        output += "Reservation " + rs.getInt("id") + " paid: ";
        if(rs.getInt("paid_flag") == 0) {
          output += "false:\n";
        } else {
          output += "true:\n";
        }
        Itinerary flightInfo = storeFlight(rs.getInt("flight1_id"),rs.getInt("flight2_id"));
        output += flightInfo.toString();
      }
      rs.close();
      if(output.equals("")) {
        return "No reservations found\n";
      }
      return output;
    } catch(Exception e) {
      e.printStackTrace();
      return "Failed to retrieve reservations\n";
    }
    
  }



// Method used to take flight ids from a reservation and convert it to Itinerary object. 
  private Itinerary storeFlight(int fid1, int fid2) {
    try {
      // Recursive call to create Itinerary obj for second flight
      Itinerary next = null;
      if(fid2 != 0) {
        next = storeFlight(fid2, 0);
      }
      // Turns flight to Itinerary object
      String getFlightInfo = "SELECT * FROM FLIGHTS WHERE fid = ?";
      PreparedStatement ps2 = conn.prepareStatement(getFlightInfo);
      ps2.clearParameters();
      ps2.setInt(1, fid1);
      ResultSet rs = ps2.executeQuery();
      Itinerary obj = null;
      if(rs.next()) {
         obj = new Itinerary(
            rs.getInt("day_of_month"), rs.getString("carrier_id"), 
            rs.getInt("fid"), rs.getInt("flight_num"),
            rs.getString("origin_city"), rs.getString("dest_city"),
            rs.getInt("price"), rs.getInt("actual_time"), 
            rs.getInt("capacity"), next);
      }
      rs.close();
      return obj;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }








  /**
   * Example utility function that uses prepared statements
   */
  private int checkFlightCapacity(int fid) throws SQLException {
    flightCapacityStmt.clearParameters();
    flightCapacityStmt.setInt(1, fid);

    ResultSet results = flightCapacityStmt.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();

    return capacity;
  }

  // Uses binary search to find an itinerary inside of the global variable "list"
  public int binarySearch(int id) {
      if(list == null || list.size() == 0) {
        return -1;
      }
      int right = list.size() - 1;
      int left = 0;
      while(left <= right) {
        int m = (left + right) / 2;
        if (list.get(m).itineraryID == id) {
          return m;
        }
        if (list.get(m).itineraryID < id) {
          left = m + 1;
        } else {
          right = m - 1;
        }
      }
      return -1;
    }


  /**
   * Utility function to determine whether an error was caused by a deadlock
   */
  private static boolean isDeadlock(SQLException e) {
    return e.getErrorCode() == 1205;
  }



  class Itinerary implements Comparable<Itinerary>{
    public int itineraryID;
    public int time;
    public int dayOfMonth;
    public String carrierID;
    public int fid;
    public int flight_num;
    // If indirect flight, we connect with the first flight with the second flight
    public Itinerary next;
    public String originCity;
    public String destCity;
    public int capacity;
    public int price;
    // Only used later when comparing two Itineraries in the compareTo()
    public int totalTime;
    Itinerary(int day_of_month, String carrier_id, int fid, int flight_num,String origin_city,
                  String dest_city, int price, int time, int capacity, Itinerary next) {
      this.time = time;
      this.dayOfMonth = day_of_month;
      this.carrierID = carrier_id;
      this.fid = fid;
      this.flight_num = flight_num;
      this.originCity = origin_city;
      this.destCity = dest_city;
      this.capacity = capacity;
      this.price = price;
      this.time = time;  
      this.next = next;

      this.totalTime = time;
      if(next != null) {
        totalTime = this.time + next.time;
      }  
    }

    @Override
    public int compareTo(Itinerary obj) {
      if(this.totalTime < obj.totalTime) {
        return -1;
      } 
      if(this.totalTime > obj.totalTime) {
        return 1;
      }
      if(this.fid < obj.fid) {
        return -1;
      }
      if(this.fid > obj.fid) {
        return 1;
      }
      if(this.next != null && obj.next != null) {
        if(this.next.fid < obj.next.fid) {
          return -1;
        }
        if(this.next.fid > obj.next.fid) {
          return 1;
        }
      }
     return 0;
    }

    @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("ID: " + this.fid + " Day: " + this.dayOfMonth + " Carrier: " + this.carrierID + " Number: "
          + this.flight_num + " Origin: " + this.originCity + " Dest: " + this.destCity + " Duration: " + this.time
          + " Capacity: " + this.capacity + " Price: " + this.price + "\n");
    if(this.next != null) {
      sb.append(this.next.toString());
    }
    return sb.toString();
  }
 }

  
/**
   * A class to store information about a single flight
   *
   * TODO(hctang): move this into QueryAbstract
   */
  class Flight {
    public int itineraryID;
    public int fid;
    public int dayOfMonth;
    public String carrierId;
    public String flightNum;
    public String originCity;
    public String destCity;
    public int time;
    public int capacity;
    public int price;
    public Flight next;

    Flight(int id, int day, String carrier, String fnum, String origin, String dest, int tm,
           int cap, int pri, Flight next) {
      fid = id;
      dayOfMonth = day;
      carrierId = carrier;
      flightNum = fnum;
      originCity = origin;
      destCity = dest;
      time = tm;
      capacity = cap;
      price = pri;
      this.next = next;
    }
    
    @Override
    public String toString() {
      return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId + " Number: "
          + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time
          + " Capacity: " + capacity + " Price: " + price;
    }

  }
}