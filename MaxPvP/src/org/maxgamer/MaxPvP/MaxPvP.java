package org.maxgamer.MaxPvP;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.maxgamer.MaxPvP.Command.PvP;
import org.maxgamer.MaxPvP.Database.Database;
import org.maxgamer.MaxPvP.Database.TrieSet;

public class MaxPvP extends JavaPlugin{
	public HashMap<String, KarmaPlayer> karmaPlayers = new HashMap<String, KarmaPlayer>(30);
	private TrieSet names = new TrieSet();
	
	public int karmaPerKill;
	private Database db;
	private boolean enabled = false;
	private PlayerListener deathListener;
	private HashMap<Integer, StreakReward> rewards = new HashMap<Integer, StreakReward>(5);
	
	public HashSet<World> neverHostileWorlds = new HashSet<World>();
	
	public static MaxPvP instance;
	
	public StreakReward getReward(int streak){
		return this.rewards.get(streak);
	}
	
	public HashSet<World> getNeverHostileWorlds(){
		return this.neverHostileWorlds;
	}
	
	@Override
	public void reloadConfig(){
		super.reloadConfig();
		
		List<String> safe = getConfig().getStringList("neverhostileworlds");
		neverHostileWorlds.clear();
		
		for(String w : safe){
			World world = Bukkit.getWorld(w);
			if(world == null) continue;
			neverHostileWorlds.add(world);
		}
	}
	
	public void onEnable(){
		instance = this;
		getLogger().info("MaxPvP starting");
		saveDefaultConfig();
		reloadConfig();
		
		File pf = this.getDataFolder().getAbsoluteFile();
		if(pf == null || !pf.exists()){
			pf.mkdir();
		}
		
		this.db = new Database(this, "stats");
		
		try{
			if(!db.hasTable()){
				db.createTable();
			}
		}
		catch(SQLException e){
			e.printStackTrace();
			getLogger().severe(ChatColor.RED + "Could not create DB table.");
		}
		
		try {
			PreparedStatement ps = db.getConnection().prepareStatement("SELECT * FROM stats");
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()){
				String name = rs.getString("name");
				int kills = rs.getInt("kills");
				int deaths = rs.getInt("deaths");
				int karma = rs.getInt("karma");
				
				KarmaPlayer kp = new KarmaPlayer(this, name, kills, deaths, karma);
				this.addKarmaPlayer(kp);
				this.names.add(name);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			getLogger().severe("Could not load players from database!");
		}
		getLogger().info(karmaPlayers.size() + " players loaded.");
		
		getCommand("pvp").setExecutor(new PvP(this));
		
		if(!enabled){
			//Register the death listener
			this.deathListener = new PlayerListener();
			Bukkit.getServer().getPluginManager().registerEvents(this.deathListener, this);
			enabled = true;
		}
		
		getLogger().info("Generating rewards...");
		
		this.rewards.put(2, new StreakReward(){
			@Override
			public void give(Player p){
				PotionEffect pot = new PotionEffect(PotionEffectType.JUMP, 6000, 1);
				p.addPotionEffect(pot, false);
				p.playEffect(p.getLocation(), Effect.POTION_BREAK, 0);
				p.sendMessage(ChatColor.GOLD + "[PvP] " + ChatColor.GREEN + "Jump: II Granted!");
				
			}
		});
		
		this.rewards.put(3, new StreakReward(){
			@Override
			public void give(Player p){
				PotionEffect pot = new PotionEffect(PotionEffectType.REGENERATION, 6000, 2);
				p.addPotionEffect(pot, false);
				p.playEffect(p.getLocation(), Effect.POTION_BREAK, 0);
				p.sendMessage(ChatColor.GOLD + "[PvP] " + ChatColor.GREEN + "Regen: II Granted!");
				Bukkit.broadcastMessage(ChatColor.GOLD + "[PvP] " + ChatColor.GREEN + p.getName() + " has reached a 3 kill streak!");
			}
		});
		
		this.rewards.put(4, new StreakReward(){
			@Override
			public void give(Player p){
				PotionEffect pot = new PotionEffect(PotionEffectType.SPEED, 18000, 3);
				p.addPotionEffect(pot, false);
				p.playEffect(p.getLocation(), Effect.POTION_BREAK, 0);
				p.sendMessage(ChatColor.GOLD + "[PvP] " + ChatColor.GREEN + "Speed: III Granted!");
			}
		});
		
		this.rewards.put(5, new StreakReward(){
			@Override
			public void give(Player p){
				p.setExp(p.getExp() + 200);
				p.sendMessage(ChatColor.GOLD + "[PvP] " + ChatColor.GREEN + "200 Exp Granted!");
				Bukkit.broadcastMessage(ChatColor.GOLD + "[PvP] " + ChatColor.GREEN + p.getName() + " has reached a 5 kill streak!");
			}
		});
		
		this.rewards.put(8, new StreakReward(){
			@Override
			public void give(Player p){
				ItemStack reward = new ItemStack(Material.DIAMOND, 3);
				
				p.getInventory().addItem(reward);
				KarmaPlayer kp = getKarmaPlayer(p);
				kp.setKillStreak(0);
				p.sendMessage(ChatColor.GOLD + "[PvP] " + ChatColor.GREEN + "Three Diamonds Granted!");
				
				StringBuilder sb = new StringBuilder(p.getName() + " killed ");
				for(String s : kp.getRecentKills().keySet()){
					sb.append(s + ", ");
				}
				getLogger().info("Potential abuse: " + sb.toString());
				
				Bukkit.broadcastMessage(ChatColor.GOLD + "[PvP] " + ChatColor.GREEN + p.getName() + " has reached an 8 kill streak!");
			}
		});
		
		getLogger().info("Loading Complete.");
	}
	public void onDisable(){
		getLogger().info("Flushing buffer...");
		db.getBuffer().getWatcher().stop();
		getLogger().info("MaxPvP Disabled");
	}
	
	public Database getDB(){
		return this.db;
	}
	
	public KarmaPlayer getKarmaPlayer(Player p){
		if(p == null) return null;
		return getKarmaPlayer(p.getName());
	}
	public KarmaPlayer getKarmaPlayer(String name){
		return this.karmaPlayers.get(name.toLowerCase());
	}
	public KarmaPlayer getKarmaPlayer(String name, boolean auto){
		if(auto == false) return getKarmaPlayer(name);
		
		Player p = Bukkit.getPlayer(name);
		if(p != null){
			KarmaPlayer kp = getKarmaPlayer(p);
			if(kp == null){
				kp = new KarmaPlayer(this, p.getName(), 0, 0, 0, true);
				this.addKarmaPlayer(kp);
			}
			return kp;
		}
		name = name.toLowerCase();
		name = names.nearestKey(name);
		if(name == null) return null;
		return this.getKarmaPlayer(name);
	}
	
	public KarmaPlayer[] getTopKillers(int amount){
		KarmaPlayer[] top = new KarmaPlayer[amount];
		for(KarmaPlayer p : this.karmaPlayers.values()){
			for(int i = top.length - 1; i >= -1; i--){
				if(i == -1 || (top[i] != null && top[i].getTotalKills() > p.getTotalKills())){
					if(i == top.length - 1){
						//Poorer than the poorest rich guy
						break;
					}
					if(!p.getName().equals(p.getName().replaceAll("[^A-Za-z0-9_]", ""))){
						break;
					}
					
					shuffle(top, i + 1);
					top[i + 1] = p;
					break;
				}
			}
		}
		return top;
	}
	public KarmaPlayer[] getTopDeaths(int amount){
		KarmaPlayer[] top = new KarmaPlayer[amount];
		for(KarmaPlayer p : this.karmaPlayers.values()){
			for(int i = top.length - 1; i >= -1; i--){
				if(i == -1 || (top[i] != null && top[i].getTotalDeaths() > p.getTotalDeaths())){
					if(i == top.length - 1){
						//Poorer than the poorest rich guy
						break;
					}
					if(!p.getName().equals(p.getName().replaceAll("[^A-Za-z0-9_]", ""))){
						break;
					}
					
					shuffle(top, i + 1);
					top[i + 1] = p;
					break;
				}
			}
		}
		return top;
	}
	
	public KarmaPlayer[] getTopKDR(int amount){
		KarmaPlayer[] top = new KarmaPlayer[amount];
		for(KarmaPlayer p : this.karmaPlayers.values()){
			for(int i = top.length - 1; i >= -1; i--){
				if(i == -1 || (top[i] != null && top[i].getKDR(3) > p.getKDR(3))){
					if(i == top.length - 1){
						//Poorer than the poorest rich guy
						break;
					}
					if(!p.getName().equals(p.getName().replaceAll("[^A-Za-z0-9_]", ""))){
						break;
					}
					
					shuffle(top, i + 1);
					top[i + 1] = p;
					break;
				}
			}
		}
		return top;
	}
	
	private void shuffle(KarmaPlayer[] p, int n){
		for(int i = p.length - 1; i > n; i--){
			p[i] = p[i - 1];
		}
		p[n] = null;
	}
	public void addKarmaPlayer(KarmaPlayer kp){
		this.karmaPlayers.put(kp.getName(), kp);
		this.names.add(kp.getName());
	}
}