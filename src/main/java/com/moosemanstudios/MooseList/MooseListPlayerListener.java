package com.moosemanstudios.MooseList;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class MooseListPlayerListener implements Listener{
	MooseList plugin = null;
	
	MooseListPlayerListener(MooseList instance) {
		plugin = instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (!plugin.whitelistmanager.isWhitelisted(event.getPlayer().getDisplayName())) {
			event.setKickMessage(plugin.whitelistmanager.getKickMessage());
			event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
		}
	}
}
