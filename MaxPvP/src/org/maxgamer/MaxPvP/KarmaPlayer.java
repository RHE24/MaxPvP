package org.maxgamer.MaxPvP;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class KarmaPlayer {
	private String name;
	private int karma;
	private HashMap<String, Long> recentKills = new HashMap<String, Long>(3);
	private int totalKills;
	private int totalDeaths;
	private MaxPvP plugin;
	private boolean isNew = false;
	private int killStreak = 0;
	long lastKill = 0;
	
	private long lastAttack = 0;
	private long lastHit = 0;
	
	public static final int ATTACK_COOLDOWN = 30000;
	
	public KarmaPlayer(MaxPvP plugin, String name, int totalKills, int totalDeaths, int karma){
		this.name = name.toLowerCase();
		this.totalKills = totalKills;
		this.totalDeaths = totalDeaths;
		this.karma = karma;
		this.plugin = plugin;
	}
	public KarmaPlayer(MaxPvP plugin, String name, int totalKills, int totalDeaths, int karma, boolean isNew){
		this(plugin, name, totalKills, totalDeaths, karma);
		this.isNew = isNew;
	}
	public int getKarma(){
		return this.karma;
	}
	public void setKillStreak(int killStreak){
		this.killStreak = killStreak;
	}
	public int getKillStreak(){
		return this.killStreak;
	}
	public Player getPlayer(){
		return Bukkit.getPlayerExact(name);
	}
	public HashMap<String, Long> getRecentKills(){
		return this.recentKills;
	}
	public void setHostile(){
		this.lastAttack = System.currentTimeMillis();
	}
	public void setInCombat(){
		this.lastHit = System.currentTimeMillis();
	}
	public void setOutOfCombat(){
		this.lastHit = 0;
	}
	public boolean isHostile(){
		return this.lastAttack + ATTACK_COOLDOWN > System.currentTimeMillis();
	}
	public boolean isInCombat(){
		return this.lastHit + ATTACK_COOLDOWN > System.currentTimeMillis();
	}
	public void setFriendly(){
		this.lastAttack = 0;
	}
	public void addKill(Player p){
		recentKills.put(p.getName().toLowerCase(), System.currentTimeMillis());
		totalKills++;
		
		if(lastKill + 60000 > System.currentTimeMillis()){
			this.killStreak++;
			StreakReward reward = plugin.getReward(killStreak);
			if(reward != null){
				reward.give(this.getPlayer());
			}
			
		}
		else{
			this.setKillStreak(1); //Killstreak expired.
		}
		
		lastKill = System.currentTimeMillis();
	}
	public long lastKillTime(){
		return this.lastKill;
	}
	public boolean isValidKill(Player p){
		String pName = p.getName().toLowerCase();
		
		if(this.recentKills.containsKey(pName)){
			long lastKill = recentKills.get(pName);
			if(lastKill + 60000 > System.currentTimeMillis()){
				//Killed them in the last 60 seconds.
				return false;
			}
		}
		
		Player m = this.getPlayer();
		if(p.getAddress() == null || m.getAddress().getAddress().getHostAddress().equals(p.getAddress().getAddress().getHostAddress())){
			return false;
		}
		
		KarmaPlayer vic = plugin.getKarmaPlayer(p);
		//If my kdr is over 3x their kdr, and i have 5 more kills than them at least, its not valid.
		if(vic.getKDR() * 3 < this.getKDR() && this.getTotalKills() - 5 > vic.getTotalKills()){
			return false;
		}
		
		return true;
	}
	public int getTotalKills(){
		return this.totalKills;
	}
	
	public void addDeath(){
		totalDeaths++;
		this.lastKill = 0; 
		this.setKillStreak(0);
	}
	public int getTotalDeaths(){
		return this.totalDeaths;
	}
	public double getKDR(int decimals){
		double kills = totalKills;
		double deaths = totalDeaths;
		if(kills == 0) kills = 1;
		if(deaths == 0) deaths = 1;
		
		BigDecimal bd = new BigDecimal(kills/deaths);
	    BigDecimal rounded = bd.setScale(decimals, RoundingMode.HALF_UP);
	    return rounded.doubleValue();
	}
	public double getKDR(){
		return getKDR(2);
	}
	
	public String getName(){
		return this.name;
	}
	
	/**
	 * Queues this player for updating in the database.
	 * Used the buffer.
	 */
	public void update(){
		String query;
		if(isNew){
			query = "INSERT INTO stats (name, kills, deaths, karma) VALUES ('"+this.getName()+"', '"+this.totalKills+"', '"+this.totalDeaths+"', '"+this.karma+"')";
			isNew = false;
		}
		else{
			query = "UPDATE stats SET kills = '"+totalKills+"', deaths = '"+totalDeaths+"', karma = '"+karma+"' WHERE name = '"+this.getName()+"'";
		}
		plugin.getDB().getBuffer().addQuery(query);
	}
	
	/**
	 * Nicely human readable name with capital first letter
	 * @return Nicely human readable name with capital first letter
	 */
	public String getFormattedName(){
		return this.getName().substring(0, 1).toUpperCase() + this.getName().substring(1, this.getName().length());
	}
	
}