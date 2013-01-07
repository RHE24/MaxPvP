package org.maxgamer.MaxPvP.Database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.maxgamer.MaxPvP.MaxPvP;

public class Database{
	private String fileName;
	private Buffer buffer;
	private File file;
	private MaxPvP plugin;
	private boolean sqlite;
	private String host;
	private int port;
	private String dbName;
	private String pass;
	private String user;
	
	/**
	 * Creates a new database handler.  Multithreads common querying
	 * Uses SQLite
	 * @param fileName The file to call this database.
	 */
	public Database(MaxPvP plugin, String fileName){
		this.fileName = fileName;
		this.file = new File(plugin.getDataFolder(), fileName + ".db");
		this.plugin = plugin;
		this.sqlite = true;
		this.buffer = new Buffer(plugin, this);
	}
	
	public Database(MaxPvP plugin, String host, int port, String dbName, String user, String pass){
		this.plugin = plugin;
		this.host = host;
		this.port = port;
		this.dbName = dbName;
		this.pass = pass;
		this.user = user;
		this.sqlite = false;
		this.buffer = new Buffer(plugin, this);
	}
	
	/**
	 * Gets the database connection for
	 * executing queries on.
	 * @return The database connection
	 */
	public Connection getConnection(){
		if(sqlite){
			if(!this.getFile().exists()){
				plugin.getLogger().info("CRITICAL: Database does not exist");
				try {
					this.getFile().createNewFile();
					Class.forName("org.sqlite.JDBC");
					Connection dbCon = DriverManager.getConnection("jdbc:sqlite:" + this.getFile());
					return dbCon;
				} 
				catch (IOException e) {
					e.printStackTrace();
					plugin.getLogger().info("Could not create file " + this.getFile().toString());
				} 
				catch (ClassNotFoundException e) {
					e.printStackTrace();
					plugin.getLogger().info("You need the SQLite JBDC library.  Put it in MinecraftServer/lib folder.");
				} catch (SQLException e) {
					e.printStackTrace();
					plugin.getLogger().info("SQLite exception on initialize " + e);
				}
			}
			try{
				Class.forName("org.sqlite.JDBC");
				return DriverManager.getConnection("jdbc:sqlite:" + this.getFile());
			}
			catch(SQLException e){
				e.printStackTrace();
				plugin.getLogger().info("SQLite exception on initialize.");
			}
			catch(ClassNotFoundException e){
				e.printStackTrace();
				plugin.getLogger().info("SQLite library not found, was it removed?");
			}
		}
		else{
			try{
				return DriverManager.getConnection("jdbc:mysql://"+this.host+":"+this.port+"/"+this.dbName, user, pass);
			}
			catch(SQLException e){
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	/**
	 * @return The buffer object with pending queries
	 */
	public Buffer getBuffer(){
		return this.buffer;
	}
	
	public String getFileName(){
		return this.fileName;
	}
	
	public File getFile(){
		return this.file;
	}
	
	public boolean hasTable(){
		try {
			PreparedStatement ps = this.getConnection().prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name='stats';");
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				ps.close();
				return true;
			}
			ps.close();
			return false;
			
		} catch (SQLException e) {
			return false;
		}
	}
	
	public void createTable() throws SQLException{
		Statement st = getConnection().createStatement();
		String createTable;
		if(sqlite){
			createTable = 
			"CREATE TABLE \"stats\" (" + 
					"\"name\"  TEXT(20) NOT NULL, " + 
					"\"kills\"  INTEGER(32) NOT NULL DEFAULT 0, " +
					"\"deaths\"  INTEGER(32) NOT NULL DEFAULT 0, " +
					"\"karma\"  INTEGER(32) NOT NULL DEFAULT 0, " +
					"PRIMARY KEY (\"name\") " +
					");";
		}
		else{
			createTable = "CREATE TABLE `stats` ("+
			  "`name` varchar(20) NOT NULL,"+
			  "`kills` int(32,0) NOT NULL DEFAULT '0',"+
			  "`deaths` int(32,0) NOT NULL DEFAULT '0',"+
			  "`karma` int(32,0) NOT NULL DEFAULT '0',"+
			  "PRIMARY KEY (`name`)"+
			")";
		}
		st.execute(createTable);
	}
}