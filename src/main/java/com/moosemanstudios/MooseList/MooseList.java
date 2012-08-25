package com.moosemanstudios.MooseList;

import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;


public class MooseList extends JavaPlugin {
	public Logger log = Logger.getLogger("Minecraft");
	String prefix = "[MooseList] ";
	PluginDescriptionFile pdfFile = null;
	Boolean debug = false;
	WhiteListManager whitelistmanager;
	MooseListCommandExecutor commandExecutor;
	MooseListPlayerListener playerListener;
	public void onEnable() {
		
		whitelistmanager = new WhiteListManager(this.getDataFolder());
		
		// create the config file
		create_config();
		
		// load the config
		if (!load_config()) {
			return;
		}
		
		// initialize the white list manager at this point, everything should be loaded
		if (!whitelistmanager.init()) {
			log.severe(prefix + "Could not initialize the storage backend!");
		}
		
		// enable metrics tracking
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// register the command executor
		commandExecutor = new MooseListCommandExecutor(this);
		getCommand("mooselist").setExecutor(commandExecutor);
		
		// creat the player listener
		playerListener = new MooseListPlayerListener(this);
		
		// lastly inform user plugin is enabled
		pdfFile = this.getDescription();
		log.info(prefix + "version: " + pdfFile.getVersion() + " is now enabled");
	}
	
	public void onDisable() {
		log.info(prefix + "is now disabled");
	}
	
	public void create_config() {
		if (!getConfig().contains("debug")) {
			getConfig().set("debug", false);
		}
		if (!getConfig().contains("kick-message")) {
			getConfig().set("kick-message", "You are not whitelisted on this server");
		}
		if (!getConfig().contains("whitelist-enabled")) {
			getConfig().set("whitelist-enabled", true);
		}
		if (!getConfig().contains("storage")) {
			getConfig().createSection("storage");
			if (!getConfig().getConfigurationSection("storage").contains("flatfile")) {
				getConfig().getConfigurationSection("storage").createSection("flatfile");
				if (!getConfig().getConfigurationSection("storage").getConfigurationSection("flatfile").contains("enabled")) {
					getConfig().getConfigurationSection("storage").getConfigurationSection("flatfile").set("enabled", true);
				}
			}
			if (!getConfig().getConfigurationSection("storage").contains("sqlite")) {
				getConfig().getConfigurationSection("storage").createSection("sqlite");
				if (!getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").contains("enabled")) {
					getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").set("enabled", false);
				}
				if (!getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").contains("filename")) {
					getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").set("filename", "whitelist");
				}
				if (!getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").contains("table-name")) {
					getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").set("table-name", "players");
				}
			}
			if (!getConfig().getConfigurationSection("storage").contains("mysql")) {
				getConfig().getConfigurationSection("storage").createSection("mysql");
				if (!getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").contains("enabled")) {
					getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("enabled", false);
				}
				if (!getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").contains("username")) {
					getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("username", "root");
				}
				if (!getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").contains("password")) {
					getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("password", "password");
				}
				if (!getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").contains("host")) {
					getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("host", "localhost");
				}
				if (!getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").contains("port")) {
					getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("port", 3306);
				}
				if (!getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").contains("database")) {
					getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("database", "minecraft");
				}
				if (!getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").contains("table")) {
					getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").set("table", "whitelist");
				}
			}
		}
		
		saveConfig();
	}
	
	public Boolean load_config() {
		debug = getConfig().getBoolean("debug");
		
		if (debug) {
			log.info(prefix + "debug mode enabled");
		}
		
		whitelistmanager.setKickMessage(getConfig().getString("kick-message"));
		
		if (debug) {
			log.info(prefix + "Kick-message: " + whitelistmanager.getKickMessage());
		}
		
		whitelistmanager.setWhiteListEnabled(getConfig().getBoolean("whitelist-enabled"));
		if (debug) {
			log.info(prefix + "Whitelisting mode: " + getConfig().getBoolean("whitelist-enabled"));
		}
		
		// initial check to make sure that only one storage type is enabled
		int i = 0;
		if (getConfig().getConfigurationSection("storage").getConfigurationSection("flatfile").getBoolean("enabled")) {
			i++;
		}
		if (getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").getBoolean("enabled")) {
			i++;
		}
		if (getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").getBoolean("enabled")) {
			i++;
		}
		
		if (i == 0) {
			log.severe(prefix + "At least one storage method must be enabled.");
			getServer().getPluginManager().disablePlugin(this);
			return false;
		}
		if (i != 1) {
			log.severe(prefix + "More than one storage method enabled, only one can be used at a time.");
			log.severe(prefix + "plugin is disabling.");
			getServer().getPluginManager().disablePlugin(this);
			return false;
		}
		
		// get which type of backend the player specified
		if (getConfig().getConfigurationSection("storage").getConfigurationSection("flatfile").getBoolean("enabled")) {
			 if (whitelistmanager.setStorage("flatfile")) {
				 if (debug) {
					 log.info(prefix + "Storage method: flatfile");
				 }
			 }
		} else if (getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").getBoolean("enabled")) {
			if (whitelistmanager.setStorage("sqlite")) {
				if (debug) {
					log.info(prefix + "Storage method: sqlite");
					String filename = getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").getString("filename");
					String table = getConfig().getConfigurationSection("storage").getConfigurationSection("sqlite").getString("table-name");
					whitelistmanager.setSqliteProperties(filename, table);
				}
			}
		} else if (getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").getBoolean("enabled")) {
			if (whitelistmanager.setStorage("mysql")) {
				if (debug) {
					log.info(prefix + "Storage method: mysql");
					String username = getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").getString("username");
					String password = getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").getString("password");
					String host = getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").getString("host");
					String port = getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").getString("port");
					String database = getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").getString("database");
					String table = getConfig().getConfigurationSection("storage").getConfigurationSection("mysql").getString("table");
					whitelistmanager.setMysqlProperties(username, password, host, port, database, table);
				}
			}
		}
		
		return true;
		
	}
}