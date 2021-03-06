package com.fawkes.plugin.monkeyvote;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.vexsoftware.votifier.model.VotifierEvent;

public class MonkeyVote extends JavaPlugin implements Listener {

		// private final long TIME_VOTE = 600;

		private static HashMap<UUID, Vote> votes = new HashMap<UUID, Vote>();

		static List<String> permissions;
		static long duration;
		static String initialMsg, extendMsg, finishMsg;

		FileConfiguration config;

		public void onEnable() {

			saveDefaultConfig();

			config = getConfig();

			permissions = config.getStringList("permissions");
			duration = config.getLong("duration");

			initialMsg = ChatColor.translateAlternateColorCodes('&', config.getString("initialmessage"));
			extendMsg = ChatColor.translateAlternateColorCodes('&', config.getString("extendmessage"));
			finishMsg = ChatColor.translateAlternateColorCodes('&', config.getString("finishmessage"));

			// restores previous time that players had before a shutdown
			/* fetch the players from "wevote" section of the config */
			Set<String> WEStrings = config.getConfigurationSection("voters").getKeys(false);

			// loops through the list
			for (String string : WEStrings) {

					UUID person = UUID.fromString(string);

					long timeLeft = config.getLong("voters." + person.toString());

					config.set("voters." + person, null);

					Vote vote = new Vote(person, System.currentTimeMillis(), timeLeft);

					votes.put(person, vote);

					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, vote, timeLeft);

			}

			// saves config
			this.saveConfig();

			// registeres eventS
			this.getServer().getPluginManager().registerEvents(this, this);

		}

		public void onDisable() {

			for (UUID puuid : votes.keySet()) {
					storeVotePlayer(puuid, votes.get(puuid).getTicksLeft(), "voters.");

			}

			this.saveConfig();

		}

		public void storeVotePlayer(UUID uuid, long timeLeft, String path) {
			config.set(path + uuid, timeLeft);

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
			Player player = Bukkit.getServer().getPlayer(playerName);

			// player doesn't exist
			if (player == null) {
					return;

			}

			UUID uuid = player.getUniqueId();

			// if player already has voted and is in the 30 min range
			if (votes.containsKey(uuid)) {

					// gets the existing VT var
					Vote existing = votes.get(uuid);

					// cancels the existing VT var so it doesn't run
					existing.cancel();

					// makes new VT
					Vote newVT = new Vote(uuid, System.currentTimeMillis(), duration + existing.getTicksLeft());

					// adds VT to list
					votes.put(uuid, newVT);

					// schedules the VT
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, newVT, duration + existing.getTicksLeft());

					// sends confirmation
					player.sendMessage(extendMsg);

					return;

			}

			/*
			 * ANNOUNCE TO LE SERVER THAT SUCH AND SUCH HAS VOTED BLA BLA YEY P.S. make sure to change the 600L to whatever time you want (in server ticks)
			 */

			// makes new Vote var
			Vote weTime = new Vote(uuid, System.currentTimeMillis(), duration);

			// puts in in le list
			votes.put(uuid, weTime);

			// 36000 ticks in 30 minutes

			// makes le Vote var run in 30 minutes
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, weTime, duration);

			for (String permission : permissions) {
					// allows the player to use WE
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "perm player " + player.getUniqueId() + " set " + permission);

			}

			// sends confirmation
			player.sendMessage(initialMsg);

		}

		// boolean if the person wants to unset the permission (for extending purposes)
		public static void removePlayer(UUID uuid, boolean unset) {

			if (unset) {

					Player player = Bukkit.getPlayer(uuid);

					if (player != null) {

						player.sendMessage(finishMsg);

					}

					for (String permission : permissions) {
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "perm player " + uuid + " unset " + permission);

					}

			}

			votes.remove(uuid);

		}

}
