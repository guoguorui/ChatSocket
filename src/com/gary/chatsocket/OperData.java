package com.gary.chatsocket;
import java.sql.*;

public class OperData {

	 static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	 static final String DB_URL = "jdbc:mysql://localhost/test";
	 
	 static final String USER = "root";
	 static final String PASS = "";
	   
	 Connection conn = null;
	 Statement stmt = null;
	 ResultSet rs=null;
	 
	 public OperData() {
		 	try {
		 		Class.forName("com.mysql.jdbc.Driver");
		        conn = DriverManager.getConnection(DB_URL,USER,PASS);
		        stmt = conn.createStatement();
		    }
		   catch(Exception e) {
			   e.printStackTrace();
		   }
	 }
	   
	 public String findName() {
		 String sql = "SELECT name FROM users";
		 String name=null;
		 try {
			 rs=stmt.executeQuery(sql);;
			 while(rs.next()){
				 name = rs.getString("name");
		     }
			 cleanClose();
		}
	    catch(Exception e) {
		    e.printStackTrace();
	    }
	    return name;		   
	}
	   
	   public boolean authenticate(String name,String password) {
		   boolean flag=false;
		   String sql = "SELECT password FROM users where name='"+name+"'";
		   try{    
		       rs = stmt.executeQuery(sql);
		       while(rs.next()){
		    	   if(rs.getString("password").equals(password)) {
		        	 	flag=true;
		           }
		       }
		       cleanClose();
		   }
		   catch(Exception e){
			   e.printStackTrace();
		   }  
		   return flag;
	   }
	   
	   public void cleanClose() {
		   try{
			   rs.close();
		       stmt.close();
		       conn.close();
		   }catch(SQLException se){
		      //Handle errors for JDBC
		      se.printStackTrace();
		   }catch(Exception e){
		      //Handle errors for Class.forName
		      e.printStackTrace();
		   }finally{
		      //finally block used to close resources
		      try{
		         if(stmt!=null)
		        	 stmt.close();
		      }catch(SQLException se2){
		      }// nothing we can do
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }//end finally try
		   }//end try
	   }
}
