package org.maxgamer.MaxPvP;

import org.bukkit.entity.Player;

public interface StreakReward{
	/** Runs this reward and gives the specified player an a bonus */
	public void give(Player p);
}