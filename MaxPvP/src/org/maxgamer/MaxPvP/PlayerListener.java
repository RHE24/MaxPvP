package org.maxgamer.MaxPvP;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerListener implements Listener{
	Random r = new Random();
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(EntityDeathEvent e){
		if(!(e.getEntity() instanceof Player)) return;
		Player victim = (Player) e.getEntity();
		if(!(victim.getKiller() instanceof Player)) return;
		Player killer = victim.getKiller();
		
		KarmaPlayer kVic = MaxPvP.instance.getKarmaPlayer(victim);
		KarmaPlayer kKil = MaxPvP.instance.getKarmaPlayer(killer);
		if(kVic == null){
			kVic = new KarmaPlayer(MaxPvP.instance, victim.getName(), 0, 0, 0, true);
			MaxPvP.instance.addKarmaPlayer(kVic);
		}
		if(kKil == null){
			kKil = new KarmaPlayer(MaxPvP.instance, killer.getName(), 0, 0, 0, true);
			MaxPvP.instance.addKarmaPlayer(kKil);
		}
		if(kKil.isValidKill(victim)){//cases issue
			ValidKillEvent vke = new ValidKillEvent(killer, victim, kKil, kVic);
			Bukkit.getPluginManager().callEvent(vke);
			
			if(!vke.isCancelled()){ 
				kKil.addKill(victim);
				kVic.addDeath();
				if(kVic.getRecentKills().containsKey(kKil.getName())){
					killer.sendMessage(ChatColor.GOLD + "[PvP] Revenge kill!");
				}
				killer.sendMessage(ChatColor.GOLD + "Kills: " + ChatColor.GREEN + kKil.getTotalKills() + " Deaths: " + ChatColor.GREEN + kKil.getTotalDeaths() + ChatColor.GOLD + " New KDR: " + ChatColor.GREEN + kKil.getKDR() + ChatColor.GOLD + " Killstreak: " + ChatColor.GREEN + kKil.getKillStreak());
				victim.sendMessage(ChatColor.GOLD + "Kills: " + ChatColor.GREEN + kVic.getTotalKills() + " Deaths: " + ChatColor.GREEN + kVic.getTotalDeaths() + ChatColor.GOLD + " New KDR: " + ChatColor.GREEN + kVic.getKDR());
				kVic.setOutOfCombat();
				kKil.update();
				kVic.update();
			}
		}
		else{
			killer.sendMessage(ChatColor.RED + "[PvP] That player is not worth points to you.");
		}
		
		if(r.nextDouble() < MaxPvP.instance.getConfig().getDouble("head-drop-chance")){
			ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
			SkullMeta meta = (SkullMeta) skull.getItemMeta();
			
			meta.setOwner(victim.getName());
			meta.setDisplayName(victim.getName() + "'s Head");
			skull.setItemMeta(meta);
			victim.getWorld().dropItem(victim.getLocation(), skull);
			killer.sendMessage(ChatColor.GREEN + "* " + victim.getName() + "'s head rolls off their limp shoulders *");
	}
		
		kVic.setFriendly(); //You're not so bad when you're dead.
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDamage(EntityDamageByEntityEvent e){
		if(e.isCancelled()) return;
		
		if(e.getDamage() <= 0) return; //Harmless
		if(!(e.getEntity() instanceof Player)) return; //A player was not harmed
		if(MaxPvP.instance.getNeverHostileWorlds().contains(e.getEntity().getWorld())) return;
		
		Player killer;
		if(e.getDamager() instanceof Player){
			killer = (Player) e.getDamager(); //The killer attacked the victim directly
		}
		else if(e.getDamager() instanceof Projectile){
			Projectile proj = (Projectile) e.getDamager(); //The killer shot the victim
			if(proj.getShooter() != null && proj.getShooter() instanceof Player){
				killer = (Player) proj.getShooter();
			}
			else{
				return;
			}
		}
		else{
			return;
		}
		
		if(killer == e.getEntity()) return; //Attacking yourself isn't hostile. It's just stupid.
		
		KarmaPlayer kpk = MaxPvP.instance.getKarmaPlayer(killer);
		
		if(kpk == null){
			//We don't need to put this player in the database
			kpk = new KarmaPlayer(MaxPvP.instance, killer.getName(), 0, 0, 0, true);
		}
		else if(!kpk.isHostile()){
			killer.sendMessage(ChatColor.RED + "[MaxPvP] You have been marked as hostile. You can't teleport until 30 seconds after combat.");
		}
		kpk.setHostile();
		
		Player victim = (Player) e.getEntity();
		KarmaPlayer kpv = MaxPvP.instance.getKarmaPlayer(victim);
		if(kpv == null){
			kpv = new KarmaPlayer(MaxPvP.instance, victim.getName(), 0, 0, 0, true);
			MaxPvP.instance.addKarmaPlayer(kpv);
		}
		kpv.setInCombat();
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerTeleport(PlayerTeleportEvent e){
		if(e.isCancelled()) return;
		
		if(e.getCause() != TeleportCause.COMMAND) return; //Only not allowed to use commands
		KarmaPlayer kp = MaxPvP.instance.getKarmaPlayer(e.getPlayer());
		
		if(kp == null) return; //No such pvp player - Free to tp!
		if(kp.isHostile()){
			e.getPlayer().sendMessage(ChatColor.RED + "[MaxPvP] You are marked as hostile. You may not teleport for 30 seconds.");
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent e){
		MaxPvP.instance.reloadConfig(); //Reload the worlds.
	}
}