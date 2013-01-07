package org.maxgamer.MaxPvP;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class ValidKillEvent extends PlayerEvent implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	private Player killer;
	private Player victim;
	private KarmaPlayer kKiller;
	private KarmaPlayer kVictim;
	private boolean cancel;
	
	public ValidKillEvent(Player killer, Player victim, KarmaPlayer kKiller, KarmaPlayer kVictim){
		super(killer);
		this.killer = killer;
		this.victim = victim;
		this.kKiller = kKiller;
		this.kVictim = kVictim;
	}
	public ValidKillEvent(Player killer, Player victim){
		this(killer, victim, MaxPvP.instance.getKarmaPlayer(killer), MaxPvP.instance.getKarmaPlayer(victim));
	}
	@Override
	public boolean isCancelled(){
		return cancel;
	}
	@Override
	/** Cancels this event. Does not cancel the player being killed, only the stat modification and messages */
	public void setCancelled(boolean cancel){
		this.cancel = cancel;
	}
	public Player getKiller(){
		return killer;
	}
	public Player getVictim(){
		return victim;
	}
	public KarmaPlayer getKarmaKiller(){
		return kKiller;
	}
	public KarmaPlayer getKarmaVictim(){
		return kVictim;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}