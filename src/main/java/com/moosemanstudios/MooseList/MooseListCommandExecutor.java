package com.moosemanstudios.MooseList;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MooseListCommandExecutor implements CommandExecutor{

	private MooseList plugin;
	
	MooseListCommandExecutor(MooseList instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		String[] split = args;
		String commandName = cmd.getName().toLowerCase();
		
		if (commandName.equalsIgnoreCase("mooselist") || commandName.equalsIgnoreCase("ml")) {
			if (split.length == 0) {
				sender.sendMessage(ChatColor.RED + "Type " + ChatColor.WHITE + "/mooselist" + ChatColor.RED + " for help");
				return true;
			} else {
				if (split[0].equalsIgnoreCase("help")) {
					sender.sendMessage("MooseList V" + plugin.pdfFile.getVersion() + " help");
					sender.sendMessage("---------------------------------");
					sender.sendMessage(ChatColor.RED + "/mooselist help" + ChatColor.WHITE + ": Display this help screen");
					
					// permission specific settings
					if (sender.hasPermission("mooselist.view")) {
						sender.sendMessage(ChatColor.RED + "/mooselist view" + ChatColor.WHITE + ":  View list of whitelisted players");
					}
					if (sender.hasPermission("mooselist.add")) {
						sender.sendMessage(ChatColor.RED + "/mooselist add [player]" + ChatColor.WHITE + ": Add player to whitelist");
					}
					if (sender.hasPermission("mooselist.remove")) {
						sender.sendMessage(ChatColor.RED + "/mooselist remove [player]" + ChatColor.WHITE + ": Remove player from whitelist");
					}
					if (sender.hasPermission("mooselist.message")) {
						sender.sendMessage(ChatColor.RED + "/mooselist message [new message]" + ChatColor.WHITE + ": Change kick message to new message");
					}
					if (sender.hasPermission("mooselist.enable")) {
						sender.sendMessage(ChatColor.RED + "/mooselist disable" + ChatColor.WHITE + ": Enable whitelisting");
						sender.sendMessage(ChatColor.RED + "/mooselist enable" + ChatColor.WHITE + ": Disable whitelisting");
					}
					if (sender.hasPermission("mooselist.reload")) {
						sender.sendMessage(ChatColor.RED + "/mooselist reload" + ChatColor.WHITE + ": Reload the plugin");
					}
				}
				if (split[0].equalsIgnoreCase("view")) {
					if (sender.hasPermission("mooselist.view")) {
						sender.sendMessage("MooseList whitelisted players");
						sender.sendMessage("---------------------------");
						
						Set<String> players = plugin.whitelistmanager.getWhitelistedPlayers();
						for (String player : players) {
							sender.sendMessage(player);							
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Missing permission node required: mooselist.view");
					}
				}
				if (split[0].equalsIgnoreCase("add")) {
					if (sender.hasPermission("mooselist.add")) {
						if (split.length == 2) {
							if (plugin.whitelistmanager.addWhiteListPlayer(split[1])) {
								sender.sendMessage(split[1] + " added to the whitelist");
							} else {
								sender.sendMessage(split[1] + " unable to be added to whitelist");
							}
						} else {
							sender.sendMessage("Please specify player to add to the whitelist");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Missing permission node required: mooselist.add");
					}
				}
				if (split[0].equalsIgnoreCase("remove")) {
					if (sender.hasPermission("mooselist.remove")) {
						if (split.length == 2) {
							if (plugin.whitelistmanager.removeWhiteListPlayer(split[1])) {
								sender.sendMessage(split[1] + " removed from whitelist");
							} else {
								sender.sendMessage(split[1] + " unable to be removed from whitelist");
							}
						} else {
							sender.sendMessage("please specify player to remove from the whitelist");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Missing permissions node required: mooselist.remove");
					}
				}
				if (split[0].equalsIgnoreCase("message")) {
					if (sender.hasPermission("mooselist.message")) {
						StringBuilder newMessage = new StringBuilder();
						for (String part : split) {
							if (part.equalsIgnoreCase("message")) {
								continue;
							} else {
								newMessage.append(part + " ");
							}
						}
						plugin.whitelistmanager.setKickMessage(newMessage.toString());
						plugin.getConfig().set("kick-message", newMessage.toString());
						plugin.saveConfig();
						sender.sendMessage("New kick message set");
					} else {
						sender.sendMessage(ChatColor.RED + "Missing permissiosn node required: mooselist.message");
					}
				}
				if (split[0].equalsIgnoreCase("enable")) {
					if (sender.hasPermission("mooselist.enable")) {
						plugin.whitelistmanager.setWhiteListEnabled(true);
						plugin.getConfig().set("whitelist-enabled", true);
						plugin.saveConfig();
						sender.sendMessage("Whitelisting mode enabled");
					} else {
						sender.sendMessage(ChatColor.RED + "Missing permission node required: mooselist.enable");
					}
				}
				if (split[0].equalsIgnoreCase("disable")) {
					if (sender.hasPermission("mooselist.enable")) {
						plugin.whitelistmanager.setWhiteListEnabled(false);
						plugin.getConfig().set("whitelist-enabled", false);
						plugin.saveConfig();
						sender.sendMessage("Whitelisting mode disabled");
					} else {
						sender.sendMessage(ChatColor.RED + "Missing permission node required: mooselist.enable");
					}
				}
				if (split[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("mooselist.reload")) {
						plugin.load_config();
						sender.sendMessage("Mooselist config reloaded");
					} else {
						sender.sendMessage(ChatColor.RED + "Missing permission node required: mooselist.reload");
					}
				}
			}
			return true;
		}
		
		return false;
	}
}
