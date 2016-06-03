package com.fawkes.plugin.monkeyvote;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.vexsoftware.votifier.model.VotifierEvent;

public class MonkeyVote extends JavaPlugin implements Listener {

	// private final long TIME_VOTE = 36000L;

	private final long TIME_VOTE = 600;

	private static HashMap<UUID, Vote> votes = new HashMap<UUID, Vote>();

	public void onEnable() {

		saveDefaultConfig();

		// restores previous time that players had before a shutdown
		/* fetch the players from "wevote" section of the config */
		Set<String> WEStrings = getConfig().getConfigurationSection("wevote").getKeys(false);

		// loops through the list
		for (String string : WEStrings) {

			UUID person = UUID.fromString(string);

			long timeLeft = getConfig().getLong("wevote." + person.toString());

			getConfig().set("wevote." + person, null);

			Vote vote = new Vote(person, System.currentTimeMillis(), timeLeft);

			votes.put(person, vote);

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, vote, timeLeft);

		}

		// saves config
		this.saveConfig();

	}

	public void onDisable() {

		for (UUID puuid : votes.keySet()) {
			storeVotePlayer(puuid, votes.get(puuid).getTicksLeft(), "wevote.");

		}

		this.saveConfig();

	}

	public void storeVotePlayer(UUID uuid, long timeLeft, String path) {
		getConfig().set(path + uuid, timeLeft);

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length < 1) {
			return false;

		}

		vote(args[0]);

		return true;

	}

	@EventHandler
	public void onVote(VotifierEvent e) {

		vote(e.getVote().getUsername());

	}

	public void vote(String playerName) {

		@SuppressWarnings("deprecation")
		Player oplayer = Bukkit.getServer().getPlayer(playerName);

		// player doesn't exist
		if (oplayer == null) {
			return;

		}

		UUID uuid = oplayer.getUniqueId();

		Player player = (Player) oplayer;

		// if player already has voted and is in the 30 min range
		if (votes.containsKey(uuid)) {

			// gets the existing VT var
			Vote existing = votes.get(uuid);

			// cancels the existing VT var so it doesn't run
			existing.cancel();

			// makes new VT
			Vote newVT = new Vote(uuid, System.currentTimeMillis(), TIME_VOTE + existing.getTicksLeft());

			// removes old VT
			votes.remove(uuid);

			// adds VT to list
			votes.put(uuid, newVT);

			// schedules the VT
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, newVT, TIME_VOTE + existing.getTicksLeft());

			// sends confirmation
			player.sendMessage(ChatColor.GOLD + "30 minutes have been added onto your remaining WorldEdit time!");

			return;

		}

		/*
		 * ANNOUNCE TO LE SERVER THAT SUCH AND SUCH HAS VOTED BLA BLA YEY P.S.
		 * make sure to change the 600L to whatever time you want (in server
		 * ticks)
		 */

		// makes new Vote var
		Vote weTime = new Vote(uuid, System.currentTimeMillis(), TIME_VOTE);

		// puts in in le list
		votes.put(uuid, weTime);

		// 36000 ticks in 30 minutes

		// makes le Vote var run in 30 minutes
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, weTime, TIME_VOTE);

		// allows the player to use WE
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
				"perm player " + player.getUniqueId() + " set worldedit.*");

		// sends confirmation
		player.sendMessage(ChatColor.GOLD + "You may now use WorldEdit for 30 minutes!");

	}

	public static void removePlayer(UUID uuid) {
		votes.remove(uuid);

	}

}
