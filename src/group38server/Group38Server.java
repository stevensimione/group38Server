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
import java.nio.file.Paths;
import java.sql.*;
import java.util.Objects;

public class Group38Server {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws URISyntaxException {
		Connection conn = createDbTableStudents();
		int clientNumber = 0;
		try
		{
			ServerSocket serverSocket = new ServerSocket(8000,2);     
			while(true) { 
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
		} 
		catch(IOException e) {
			System.out.println(e);
		}
	}
	private static Connection createDbTableStudents() throws URISyntaxException {
                // CEdit5 This change is so that the path is not hard-coded
                File file = new File("derbypath.txt");
                String absolutePath = file.getAbsolutePath();
                System.out.print(absolutePath);
                String derby1 = "derby\\";
                if(absolutePath.substring(0, 1).equals("/"))
                {
                    derby1 = "derby/";
                }
                System.out.print(absolutePath.substring(0, 1));
                String aPath = absolutePath.replace("derbypath.txt", derby1);
		Connection conn = null;
		String path = aPath;    // CEdit5
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
                                insertGroups(conn);
				System.out.println("\nSUCCESS: "+
						"Inserted 5 groups");                                
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
	private static void createPostsTable(Connection con)    // CEdit4 This table has been expanded
			throws SQLException {
		String sql = 
				"CREATE TABLE POSTS " +
						"(postID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                                                "inGroup VARCHAR (255) NOT NULL, "+
                                                "subject VARCHAR (32660), "+
                                                "body VARCHAR (32660), "+
                                                "authorID INTEGER NOT NULL, "+  // It must be userID, not user name
                                                "tStamp INTEGER NOT NULL, "+
						"isNew SMALLINT NOT NULL  )";         
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
	private static void createGroupsTable(Connection con)   // CEdit4 This has also been changed somewhat
			throws SQLException {
		String sql = 
				"CREATE TABLE GROUPS " +
						"(groupID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
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
	private static void insertGroups(Connection con) // CEdit4 This will create 5 groups
			throws SQLException {
		String sql = 
		"INSERT INTO GROUPS (gName) VALUES ('comp.programming'), ('comp.os.threads'), ('comp.lang.c'), ('comp.lang.python'), ('comp.lang.javascript')";       
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
		int status = 0;  
		/*TTEdit1: Status codes for clarity:
		 * 0 - initial
		 * 1 - logging in
		 * 2 - logging out
		 * 3 - functions
		 * 4 - ag 
		 * 5 - rg
		 * 6 - sg
		 * no status needed for help.
		 */
		String reply="";     
		int uic = 0;  
		boolean writeToClient = false; //changed dontAllowReply to a boolean for clarity		
		int defaultN = 5;
		// uic stands for user index count and checks the index for each user
		// dontAllowReply is simply a way of allowing or denying replys from the client whenever necessary
		// dontActivate is a switch that prevents the program from going into 'invalid entry' early and causing an infinite loop
		String userName = "";   //keeps track of the current users name
		String[] commandAndArgs = new String[3];
		String nameRequest = "\nType in your User Name:";
		try{
			DataInputStream isFromClient = new DataInputStream(
					connectToClient.getInputStream());
			DataOutputStream osToClient = new DataOutputStream(
					connectToClient.getOutputStream());
			String stringToClient="";
			
			while(status != 2) //while not logout
			{
				System.out.println("Status is: " + status);
				if(status == 0)
				{
					System.out.println("S0");
					reply = reply.trim(); //removed uppercase, makes name uppercase.
					stringToClient = nameRequest;
					writeToClient = true;
					status = 1; //go to next status, logging in
				}
				else if(status == 1) //log in
				{
					if(!reply.equalsIgnoreCase("")) //user name can be anything, so if the user ID is not blank, we store it
					{
						System.out.println("S1");
						//below here places user into sql properly.
						int control = 0;
						//System.out.println("uic = "+uic);
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
					stringToClient = "What would you like to do "+userName+"?: ";
					writeToClient = true;
					status = 3;    //End of Log-In stuff (go to 'else if' with status 3)
				}
				else if(status == 3)
				{
					System.out.println("S3");
					//parse functions here.
					stringToClient = "What would you like to do "+userName+"?: ";
					reply = reply.trim();//I removed uppercase because the name would be uppercase in RG.					
					commandAndArgs = reply.split("\\s+"); //parses all spaces, tabs, etc.
					//if there's nothing present, this will catch it.
					try{
						commandAndArgs[0].toUpperCase();//this would set the command to be uppercase now
					}
					catch(ArrayIndexOutOfBoundsException e)
					{
						//since there's nothing present, we redo the command.
						stringToClient += "\nInvalid command, please type a valid command.";
						writeToClient = true;
						continue; //Must have as we would rewrite it later on.
					}
					if(commandAndArgs[0].equalsIgnoreCase("LOGIN")) // login command to create a new user.
					{
						status = 0;	
                                                writeToClient = false;  // CEdit5 Added this since we dont want to repeat after login has been executed
					}
					else if(commandAndArgs[0].equalsIgnoreCase("LOGOUT"))//logout and close.
                                        {
                                                stringToClient = "Have a nice day"; // CEdit3 See lines 39-41 in group38Client
						status = 2;
                                                writeToClient = true;   
                                        }
					else if(commandAndArgs[0].equalsIgnoreCase("HELP"))//print help
					{
						stringToClient = "help\n"
                                                + "ag [N]\n"
                                                + "sg [N]\n"
                                                + "rg gname [N]\n"
                                                + "logout\n";
						stringToClient += "What would you like to do "+userName+"?: ";
						writeToClient = true;
					}
					else if(commandAndArgs[0].equalsIgnoreCase("AG"))
					{
						status = 4;
						writeToClient = false;
					}
					else if(commandAndArgs[0].equalsIgnoreCase("SG"))
					{
						status = 5;
						writeToClient = false;
					}
					else if(commandAndArgs[0].equalsIgnoreCase("RG"))
					{
						status = 6;
						writeToClient = false;
					}
                                        else if(commandAndArgs[0].equalsIgnoreCase("BADREPLY")) //CEdit5 Added so that an invalid command will not loop
                                        {
                                            stringToClient = "Invalid command, please type a valid command.";
                                            stringToClient += "\nWhat would you like to do "+userName+"?: ";
                                            writeToClient = true;
                                        }
					else
					{						
						writeToClient = false;
                                                reply = "badreply"; // CEdit5 Reply will not be sent to the client
					}
				}
				else if(status == 4)
				{
					writeToClient = true;
					status = 3;
					//AG [nothing]
					try
					{
						//TTEdit2: If the command is entered with a space, then you have to enter the next line.
						//If not, then we must use the default value.
						System.out.println("ag : " + Integer.parseInt(commandAndArgs[1]));
					}
					catch(ArrayIndexOutOfBoundsException e)
					{
						//use default value for N
						System.out.println("ag : " + defaultN); //prints AG : N to client
					}
					catch(NumberFormatException e)
					{
						stringToClient += "\nPlease type a valid number for N.";
					}
				}
				else if(status == 5)
				{
					status = 3;
					writeToClient = true;
					//try SG [N]
					try
					{
						System.out.println("sg : " + Integer.parseInt(commandAndArgs[1]));
					}
					//SG [nothing]
					catch(ArrayIndexOutOfBoundsException e)
					{
						//use default value for N
						System.out.println("sg : " + defaultN); //prints AG : N to client
					}
					// SG [string]
					catch(NumberFormatException e)
					{
						stringToClient += "Please type a valid number for N.";
					}
				}
				else if(status == 6)
				{
					writeToClient = true;
					status = 3;
					try // RG totally blank
					{
						if(commandAndArgs[1] == null){}
					}
					catch(ArrayIndexOutOfBoundsException e)
					{
						stringToClient += "Please type a valid name for gname.";
					}
					try //RG Gname N
					{
						//Client.rg(commandAndArgs[1], Integer.parseInt(commandAndArgs[2]));
                                                System.out.println("rg : " + Integer.parseInt(commandAndArgs[2]));
					}
					// RG GName _
					catch(ArrayIndexOutOfBoundsException e)
					{
						//use default value for N
						//Client.rg(commandAndArgs[1], defaultN); //prints AG : N to client
                                                System.out.println("rg : " + defaultN);
					}
					// RG GName [string]
					catch(NumberFormatException e)
					{
						stringToClient += "Please type a valid number for N.";
					}
				}				
				if(writeToClient) 
				{
					osToClient.writeUTF(stringToClient);
					osToClient.flush();
					reply = isFromClient.readUTF();
					writeToClient = true;
				}
				
				System.out.println("String received from "+
						"client "+clientNumber+": " + reply);
			}
			connectToClient.close();
		}
		catch(java.net.SocketTimeoutException e){System.out.println("Timed out trying to read from socket");}
		catch(IOException ex){System.err.println(ex);}  
	}

}
