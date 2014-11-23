package uk.ac.cam.sy321.fjava.tick5;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.ac.cam.cl.fjava.messages.RelayMessage;

public class Database {

	private Connection connection;

	public Database(String databasePath) throws SQLException{
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		connection = DriverManager.getConnection("jdbc:hsqldb:file:"
				+ databasePath, "SA", "");
		Statement delayStmt = connection.createStatement();
		try {
			delayStmt.execute("SET WRITE_DELAY FALSE");
		} //Always update data on disk
		finally {
			delayStmt.close();
		}

		connection.setAutoCommit(false);

		Statement sqlStmt = connection.createStatement();
		try {
			sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"+
					"message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
		} catch (SQLException e) {
			System.out.println("Warning: Database table \"messages\" already exists.");
		} finally {
			sqlStmt.close();
		}

		Statement sqlStmt2 = connection.createStatement();
		try {
			sqlStmt2.execute("CREATE TABLE statistics(key VARCHAR(255),value INT)");
		} catch (SQLException e) {
			System.out.println("Warning: Database table \"statistics\" already exists.");
		} finally {
			sqlStmt2.close();
		}
		connection.commit();
	}

	public void close() throws SQLException{
		connection.close();
	}

	public void incrementLogins() throws SQLException{
		String stmt = "UPDATE statistics SET value = value+1 WHERE key='Total logins'";
		Statement sqlStmt = connection.createStatement();
		try {
			sqlStmt.execute(stmt);
		} finally {
			sqlStmt.close();
		}
		connection.commit();
	}

	public void addMessage(RelayMessage m) throws SQLException{
		String statStmt = "UPDATE statistics SET value = value+1 WHERE key='Total messages'";
		String msgStmt = "INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
		Statement sqlStmt = connection.createStatement();
		PreparedStatement insertMessage = connection.prepareStatement(msgStmt);
		try {
			insertMessage.setString(1, m.getFrom()); //set value of first "?" to "Alastair"
			insertMessage.setString(2, m.getMessage());
			insertMessage.setLong(3, m.getCreationTime().getTime());
			insertMessage.executeUpdate();
			sqlStmt.execute(statStmt);

		} finally { //Notice use of finally clause here to finish statement
			insertMessage.close();
			sqlStmt.close();
		}
		connection.commit();
	}

	public List<RelayMessage> getRecent() throws SQLException{
		ArrayList<RelayMessage> result = new ArrayList<RelayMessage>();
		String stmt = "SELECT nick,message,timeposted FROM messages "+
				"ORDER BY timeposted DESC LIMIT 10";
		PreparedStatement recentMessages = connection.prepareStatement(stmt);
		try {
			ResultSet rs = recentMessages.executeQuery();
			try {
				while (rs.next())
					result.add(new RelayMessage(rs.getString(1), rs.getString(2),
							new Date(rs.getLong(3))));
			} finally {
				rs.close();
			}
		} finally {
			recentMessages.close();
		}
		return result;
	}

	public static void main(String[] args) throws SQLException{
		// BEGIN Command line parsing
		if(args.length!=1){
			System.err.println("Usage: java uk.ac.cam.sy321.fjava.tick5.Database <database name>");
			return;
		}
		String databaseFilePrefix = args[0];
		// END Command line parsing

		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		Connection connection = DriverManager.getConnection("jdbc:hsqldb:file:"
				+ databaseFilePrefix, "SA", "");
		Statement delayStmt = connection.createStatement();
		try {
			delayStmt.execute("SET WRITE_DELAY FALSE");
		} //Always update data on disk
		finally {
			delayStmt.close();
		}

		connection.setAutoCommit(false);

		Statement sqlStmt = connection.createStatement();
		try {
			sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"+
					"message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
		} catch (SQLException e) {
			System.out.println("Warning: Database table \"messages\" already exists.");
		} finally {
			sqlStmt.close();
		}
		String stmt = "INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
		PreparedStatement insertMessage = connection.prepareStatement(stmt);
		try {
			insertMessage.setString(1, "Alastair"); //set value of first "?" to "Alastair"
			insertMessage.setString(2, "Hello, Andy");
			insertMessage.setLong(3, System.currentTimeMillis());
			insertMessage.executeUpdate();
		} finally { //Notice use of finally clause here to finish statement
			insertMessage.close();
		}
		stmt = "SELECT nick,message,timeposted FROM messages "+
				"ORDER BY timeposted DESC LIMIT 10";
		PreparedStatement recentMessages = connection.prepareStatement(stmt);
		try {
			ResultSet rs = recentMessages.executeQuery();
			try {
				while (rs.next())
					System.out.println(rs.getString(1)+": "+rs.getString(2)+
							" ["+rs.getLong(3)+"]");
			} finally {
				rs.close();
			}
		} finally {
			recentMessages.close();
		}
		connection.commit();
	}
}
