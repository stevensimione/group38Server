/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package group38server;

/**
 *
 * 
 */
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.Date;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Group38Server {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    Connection conn = createDbTableStudents();
    int clientNumber = 0;
    try
    {
      ServerSocket serverSocket = new ServerSocket(8000,2);     
      while( true ) { 
        Socket connectToClient = serverSocket.accept();
        System.out.println("Start serving client # "+
                                    (++clientNumber));
        InetAddress clientInet = 
                    connectToClient.getInetAddress();
        String clientHostName = clientInet.getHostName();
        System.out.println("Client "+clientNumber+
          ", Host Name: "+clientHostName+
          ", IP Address: "+clientInet);
        ServeOneClientThreadStatus socT = 
          new ServeOneClientThreadStatus(
                connectToClient,conn,clientNumber);
        socT.start();
      }
    } catch(IOException e) {
       System.out.println(e);
    }
 }
 private static Connection createDbTableStudents() {
   Connection conn = null;
   String path = "c:\\derby\\";
   String driver = "derby";
   String dbName = "Assignment";
   String connectionURL="jdbc:"+driver+":"+
                 path+dbName+";create=true";
   try
   {  
      Class.forName("com.mysql.jdbc.Driver");
      conn = DriverManager.getConnection(connectionURL);
      if (conn != null)
     {
        Statement statement = conn.createStatement();
        System.out.println("\nSUCCESS: "+
                      "Connected to database: "+dbName);
        createUsersTable(conn);
        System.out.println("\nSUCCESS: "+
                      "Created Users table");
        createPostsTable(conn);
        System.out.println("\nSUCCESS: "+
                      "Created Posts table");
        createGroupsTable(conn);
        System.out.println("\nSUCCESS: "+
                      "Created Groups table");
      }
      }catch (SQLException ex) {
         System.out.println("SQLException: "+ 
                            ex.getMessage());
         ex.printStackTrace();
      } catch (Exception ex) {
         System.out.println("Exception: "+ 
                           ex.getMessage());
         ex.printStackTrace();
      }
    return conn;
  }
  private static void createUsersTable(Connection con)
                               throws SQLException {
     String sql = 
         "CREATE TABLE USERS " +
         "(userID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
         "uName VARCHAR (255) NOT NULL  )";         
     Statement stmt = null;
     try {
        stmt = con.createStatement();
        stmt.executeUpdate(sql);
     } catch (SQLException e) {
        System.out.println("SQLException: " + e.getMessage());
        e.printStackTrace();    } finally {
        if (stmt != null) { stmt.close(); }
    }
     /*
     sql = 
         "CREATE TABLE USERS " +
         "(userID INTEGER NOT NULL, " +
         "uName VARCHAR (255) NOT NULL  )";         
     stmt = null;
     try {
        stmt = con.createStatement();
        stmt.executeUpdate(sql);
     } catch (SQLException e) {
        System.out.println("SQLException: " + e.getMessage());
        e.printStackTrace();    } finally {
        if (stmt != null) { stmt.close(); }
    }
     */
   }
  private static void createPostsTable(Connection con)
                               throws SQLException {
     String sql = 
         "CREATE TABLE POSTS " +
         "(postID INTEGER NOT NULL, " +
         "Item VARCHAR (255) NOT NULL  )";         
     Statement stmt = null;
     try {
        stmt = con.createStatement();
        stmt.executeUpdate(sql);
     } catch (SQLException e) {
        System.out.println("SQLException: " + e.getMessage());
        e.printStackTrace();    } finally {
        if (stmt != null) { stmt.close(); }
    }
   }
  private static void createGroupsTable(Connection con)
                               throws SQLException {
     String sql = 
         "CREATE TABLE GROUPS " +
         "(groupID INTEGER NOT NULL, " +
         "gName VARCHAR (255) NOT NULL  )";         
     Statement stmt = null;
     try {
        stmt = con.createStatement();
        stmt.executeUpdate(sql);
     } catch (SQLException e) {
        System.out.println("SQLException: " + e.getMessage());
        e.printStackTrace();    } finally {
        if (stmt != null) { stmt.close(); }
    }
   }
}
    
class ServeOneClientThreadStatus extends Thread {
  private Socket connectToClient;
  Connection conn;
  int clientNumber;
  public ServeOneClientThreadStatus( Socket socket,
                  Connection con, int clientNo) {
     connectToClient = socket;
     conn = con;
     clientNumber = clientNo;
  }
  public void run() {
    int status = 0;  String reply="";     int uic = 0;  int dontAllowReply = 0; int dontActivate = 0;
    // uic stands for user index count and checks the index for each user
    // dontAllowReply is simply a way of allowing or denying replys from the client whenever necessary
    // dontActivate is a switch that prevents the program from going into 'invalid entry' early and causing an infinite loop
    int clientMinusOne = clientNumber - 1;  //unused
    String userName = "";   //keeps track of the current users name
    String[] commandAndArgs;    // CEdit 2
    String nameRequest = "\nType in your User Name:";
    try{
     DataInputStream isFromClient = new DataInputStream(
        connectToClient.getInputStream());
     DataOutputStream osToClient = new DataOutputStream(
        connectToClient.getOutputStream());
     String stringToClient="";
     while (true) 
     {
         System.out.println("status = "+status);    // Confirm the status number for current loop
       if( status == 0 ) {
          reply = reply.trim().toUpperCase();
          stringToClient = nameRequest;
          status = 1;
       }
       else if( status == 1 ) {  //Beginning of Log-In stuff          
         if( !reply.equalsIgnoreCase(""))  { 
             /*
            String sql = "SELECT COUNT(*) AS total FROM USERS";
            try{
               Statement statement = conn.createStatement();              
               ResultSet rs1 = statement.executeQuery(sql);
               uic = rs1.getInt("total");
            } catch( SQLException e ){System.out.println(e+" sql1 failed");}  
             */
            int control = 0;
            //System.out.println("uic = "+uic);
            int uic1 = uic+1;
            if( uic != 0 || clientNumber != 1){
            String sql1 = "SELECT * FROM USERS WHERE uName = '"+reply+"' ";
            try{
                Statement statement = conn.createStatement();
                ResultSet rs2 = statement.executeQuery(sql1);
                while( rs2.next() ) {               
                    String i = rs2.getString("uName");               
                    if( Objects.equals(i, reply)){
                        control = 1;
                    }
                }
                } catch( SQLException e ){System.out.println(e+" sql2 failed");} 
                // I added 'sqln failed' messages for debugging
                if(control == 0){
                    String sql2 = "INSERT INTO USERS (uName) VALUES ( '"+ reply +"')";
                    try{
                        Statement statement = conn.createStatement();              
                        statement.executeUpdate(sql2);
                        uic++;
                    } catch( SQLException e ){System.out.println(e+" sql3 failed");}
                    System.out.println("Created");  //Confirmation that the server created the user
                }
            }
            if(uic == 0 && control == 0){
                String sql2 = "INSERT INTO USERS (uName) VALUES ( '"+ reply +"')";
                try{
                    Statement statement = conn.createStatement();              
                    statement.executeUpdate(sql2);
                    uic++;
                } catch( SQLException e ){System.out.println(e+" sql4 failed");}
                System.out.println("Created");  //Confirmation that the server created the user
            }            
         }
         userName = reply;
         dontActivate = 1;
         dontAllowReply = 1;
         status = 3;    //End of Log-In stuff (go to 'else if' with status 3)
       }
       else if( status == 3 ){
           //Where commands will go
           System.out.println("Now in status 3");   // For Debugging        
           stringToClient = "What would you like to do "+userName+"?: ";
           reply = reply.trim().toUpperCase();
           // CEdit 2 Beggining            
           commandAndArgs = reply.split("\\s+");       // Splits by space; Command is 0 while 1+ are args   
           if( commandAndArgs[0].equalsIgnoreCase("LOGIN") ){   // CEdit 2 End
               status = 0;
               dontAllowReply = 1;
           }
           // CEdit 2 Begginging
           else if( commandAndArgs[0].equalsIgnoreCase("SG") ){
               status = 6;
               dontAllowReply = 1;
           }
           // CEdit 2 End
           else if(dontActivate == 0){
               stringToClient = "INVALID ENTRY.";
               stringToClient += "\nWhat would you like to do "+userName+"?: ";
               status = 3;
           }
           dontActivate = 0;
       }
       // CEdit 2 Beggining
       else if(status == 6){
           reply = "NOSG";
           System.out.print("Nothing in command SG yet.");
           dontActivate = 1;
           dontAllowReply = 1;
           status = 3;
       }
       // CEdit 2 End
       
       //The code below sends and recieves things to and from the client.
       if(dontAllowReply == 0){
       osToClient.writeUTF(stringToClient);
       osToClient.flush();
       }
       if ( status == 2 ) {
         connectToClient.close();  //Where the connection terminates
       }
       if(dontAllowReply == 0){
       reply = isFromClient.readUTF(); 
       }
       dontAllowReply = 0;
       
       System.out.println("String received from "+
             "client "+clientNumber+": " + reply);
     }
    }
    catch(java.net.SocketTimeoutException e){System.out.println("Timed out trying to read from socket");}
    catch(IOException ex){System.err.println(ex);}  
    }
    
}
