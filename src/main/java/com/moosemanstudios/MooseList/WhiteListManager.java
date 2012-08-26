package com.moosemanstudios.MooseList;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.alta189.simplesave.Database;
import com.alta189.simplesave.DatabaseFactory;
import com.alta189.simplesave.exceptions.ConnectionException;
import com.alta189.simplesave.exceptions.TableRegistrationException;
import com.alta189.simplesave.mysql.MySQLConfiguration;
import com.alta189.simplesave.sqlite.SQLiteConfiguration;

public class WhiteListManager {
	// the purpose of this class is to handle all whitelisting functions
	// such as adding and removing players from the whitelist, checking
	// if a player is whitelisted, and deals with storage of the whitelist
	
	// backend types include flatfile(built into server), mysql, and sqlite
	
	private File mainDirectory;
	private String kickMessage;
	private enum storage { FLATFILE, SQLITE, MYSQL };
	private storage storageMethod;
	private String mysqlTable, sqliteTable;
	private MySQLConfiguration mysqlConfig = null;
	private SQLiteConfiguration sqliteConfig = null;
	private Database db = null;
	private Logger log = Logger.getLogger("minecraft");
	private Boolean whiteListEnabled;
	
	public WhiteListManager(File file) {
		mainDirectory = file;
	}
	
	public void setKickMessage(String message) {
		kickMessage = message;
	}
	
	public String getKickMessage() {
		return kickMessage;
	}

	public Boolean setStorage(String string) {
		if (string.equalsIgnoreCase("flatfile")) {
			storageMethod = storage.FLATFILE;
			return true;
		} else if (string.equalsIgnoreCase("sqlite")) {
			storageMethod = storage.SQLITE;
			return true;
		} else if (string.equalsIgnoreCase("mysql")) {
			storageMethod = storage.MYSQL;
			return true;
		} else {
			// should never reach this
			log.severe("[MooseList] Never should have reached this point. Please report to moose517");
			return false;
		}
	}
	
	public String getStorage() {
		return storageMethod.toString();
	}

	public void setSqliteProperties(String filename, String table) {
		sqliteConfig = new SQLiteConfiguration();
		sqliteConfig.setPath(filename);
		sqliteTable = table;
	
	}
	
	public void setMysqlProperties(String username, String password, String host, String port, String database, String table) {
		mysqlConfig = new MySQLConfiguration();
		mysqlConfig.setDatabase(database);
		mysqlConfig.setHost(host);
		mysqlConfig.setPassword(password);
		mysqlConfig.setUser(username);
		mysqlConfig.setPort(Integer.parseInt(port));
		mysqlTable = table;
	}
	
	public Boolean init() {
		// this is where file creation takes place as well as setting up database links if need be based on the storage
		switch(storageMethod) {
		case FLATFILE:
			// nothing needed for flatfile, all done with built in methods
			return true;
		case SQLITE:
			db = DatabaseFactory.createNewDatabase(sqliteConfig);
			
			try {
				db.registerTable(sqlTable.class);
			} catch (TableRegistrationException e) {
				e.printStackTrace();
			}
			break;
		case MYSQL:
			db = DatabaseFactory.createNewDatabase(mysqlConfig);
			
			try {
				db.registerTable(sqlTable.class);
			} catch (TableRegistrationException e) {
				e.printStackTrace();
			}
			break;
		default: 
			return false;
		}
		
		try {
			db.connect();
			return true;
		} catch (ConnectionException e) {
			e.printStackTrace();
			return false;
		}
 	}
	
	public Boolean shutdown() {
		try {
			db.close();
			return true;
		} catch (ConnectionException e) {
			e.printStackTrace();
			return false;
		}
	}
	public Boolean isWhitelisted(String player) {
		// see if whitelisting is enabled first of all
		if (whiteListEnabled) {
			switch (storageMethod) {
			case FLATFILE:
				// TODO: fix, don't want API specifics here
				if (Bukkit.getServer().getOfflinePlayer(player).isWhitelisted()) {
					return true;
				} else {
					return false;
				}
			case SQLITE:
				ResultSet result = sqlite.query("SELECT * FROM " + sqliteTable + " WHERE player='" + player + "'");
	
				try {
					Boolean found = false;
					while (result.next()) {
						if (result.getString("player").equalsIgnoreCase(player)) {
							found = true;
						}
					}
					
					if (found) {
						return true;
					} else {
						return false;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			case MYSQL:
				// TODO: implement
				return false;
			default:
				return false;
			}
		} else {
			return true;
		}
	}
	
	public Boolean addWhiteListPlayer(String player) {
		switch(storageMethod) {
		case FLATFILE:
			Bukkit.getServer().getOfflinePlayer(player).setWhitelisted(true);
			return true;
		case SQLITE:
			sqlite.query("INSERT INTO " + sqliteTable + " ('player') VALUES ('" + player + "');");
			return true;
		case MYSQL:
			return false;
			// TODO: implement
		default:
			return false;
		}
	}
	
	public Boolean removeWhiteListPlayer(String player) {
		switch(storageMethod) {
		case FLATFILE:
			Bukkit.getServer().getOfflinePlayer(player).setWhitelisted(false);
		case SQLITE:
			sqlite.query("DELETE FROM " + sqliteTable + " WHERE player='" + player + "';");
		case MYSQL:
			// TODO: implement
		}		
		return true;
	}
	
	public Set<String> getWhitelistedPlayers() {
		Set<String>players = new HashSet<String>();
		
		switch (storageMethod) {
		case FLATFILE:
			// TODO: split this out, don't want API specific code in this class!
			Set<OfflinePlayer> offlinePlayers = Bukkit.getServer().getWhitelistedPlayers();
			for (OfflinePlayer player : offlinePlayers) {
				players.add(player.getName());
			}
		case SQLITE:
			// TODO: implement
		case MYSQL:
			// TODO: implement
		}
		return players;
	}
	
	public void setWhiteListEnabled(Boolean enable) {
		whiteListEnabled = enable;
	}
	
	public Boolean getWhiteListEnabled() {
		return whiteListEnabled;
	}
	

}
