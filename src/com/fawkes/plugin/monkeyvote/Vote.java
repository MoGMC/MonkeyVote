package com.fawkes.plugin.monkeyvote;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Vote implements Runnable {

	private UUID uuid;
	private long startTime, duration;
	private boolean cancelled = false;

	public Vote(UUID people, long startTime, long duration) {

		// duration in ticks
		uuid = people;

		// ehh?
		this.duration = duration;
		this.startTime = startTime;

	}

	@Override
	public void run() {

		// if cancelled do nuffin
		if (cancelled) {
			return;

		}

		OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(uuid);

		if (player != null) {

			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "perm player " + player.getName() + " unset worldedit.*");

			if (player.isOnline()) {

				Player online = (Player) player;

				online.sendMessage(ChatColor.RED + "Your WorldEdit time has ran out!");
				online.sendMessage(ChatColor.RED + "Vote again with /vote for more time!");

			}

		}

		MonkeyVote.removePlayer(uuid);

	}

	public void cancel() {
		cancelled = true;
		MonkeyVote.removePlayer(uuid);

	}

	public long getTicksLeft() {

		// 20 ticks in a second
		// 1000 millis in a second
		// therefore 20 ticks in 1000 millis
		// 1000/20 = 50
		// 50 millis per tick!

		return duration - ((System.currentTimeMillis() - startTime) / 50);

	}
}
