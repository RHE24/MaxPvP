package org.maxgamer.MaxPvP.Command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.MaxPvP.KarmaPlayer;
import org.maxgamer.MaxPvP.MaxPvP;

public class PvP implements CommandExecutor{
	MaxPvP plugin;
	public PvP(MaxPvP plugin){
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(sender.hasPermission("maxpvp.pvp")){
			if(args.length <= 0 || args[0].equalsIgnoreCase("kills")){
				sender.sendMessage(ChatColor.GOLD + "+---------------------------------------+");
				sender.sendMessage(ChatColor.GOLD + "| " + ChatColor.GREEN + "Lead Killers:");
				KarmaPlayer[] killers = plugin.getTopKillers(10);
				for(int i = 0; i < killers.length; i++){
					if(killers[i] == null) break; //The list isnt that big
					sender.sendMessage(ChatColor.GOLD + "| " + killers[i].getFormattedName()+ " - Kills: " + ChatColor.GREEN + killers[i].getTotalKills() + ChatColor.GOLD + " Deaths: " + ChatColor.GREEN  + killers[i].getTotalDeaths());
				}
				sender.sendMessage(ChatColor.GOLD + "+---------------------------------------+");
				if(sender instanceof Player){
					KarmaPlayer player = plugin.getKarmaPlayer((Player) sender);
					if(player == null) return true;
					sender.sendMessage(ChatColor.GOLD + "| Your Kills: " + ChatColor.GREEN + player.getTotalKills() + ChatColor.GOLD +  " Your Deaths: " + ChatColor.GREEN + player.getTotalDeaths() + ChatColor.GOLD +  " Your Kill:Death Ratio: " + ChatColor.GREEN + player.getKDR());
				}
				return true;
			}
			else if(args[0].equalsIgnoreCase("deaths")){
				sender.sendMessage(ChatColor.GOLD + "+---------------------------------------+");
				sender.sendMessage(ChatColor.GOLD + "| " + ChatColor.GREEN + "Lead Killers:");
				KarmaPlayer[] diers = plugin.getTopDeaths(10);
				for(int i = 0; i < diers.length; i++){
					if(diers[i] == null) break; //The list isnt that big
					sender.sendMessage(ChatColor.GOLD + "| " + diers[i].getFormattedName()+ " - Deaths: " + ChatColor.GREEN + diers[i].getTotalDeaths() + ChatColor.GOLD + " Kills: " + ChatColor.GREEN  + diers[i].getTotalKills());
				}
				sender.sendMessage(ChatColor.GOLD + "+---------------------------------------+");
				if(sender instanceof Player){
					KarmaPlayer player = plugin.getKarmaPlayer((Player) sender);
					if(player == null) return true;
					sender.sendMessage(ChatColor.GOLD + "| Your Kills: " + ChatColor.GREEN + player.getTotalKills() + ChatColor.GOLD +  " Your Deaths: " + ChatColor.GREEN + player.getTotalDeaths() + ChatColor.GOLD +  " Your Kill:Death Ratio: " + ChatColor.GREEN + player.getKDR());
				}
			}
			else if(args[0].equalsIgnoreCase("kdr")){
				sender.sendMessage(ChatColor.GOLD + "+---------------------------------------+");
				sender.sendMessage(ChatColor.GOLD + "| " + ChatColor.GREEN + "Lead Killers:");
				KarmaPlayer[] kdrs = plugin.getTopKDR(10);
				for(int i = 0; i < kdrs.length; i++){
					if(kdrs[i] == null) break; //The list isnt that big
					sender.sendMessage(ChatColor.GOLD + "| " + kdrs[i].getFormattedName()+ " - KDR: " + ChatColor.GREEN + kdrs[i].getKDR());
				}
				sender.sendMessage(ChatColor.GOLD + "+---------------------------------------+");
				if(sender instanceof Player){
					KarmaPlayer player = plugin.getKarmaPlayer((Player) sender);
					if(player == null) return true;
					sender.sendMessage(ChatColor.GOLD + "| Your Kills: " + ChatColor.GREEN + player.getTotalKills() + ChatColor.GOLD +  " Your Deaths: " + ChatColor.GREEN + player.getTotalDeaths() + ChatColor.GOLD +  " Your Kill:Death Ratio: " + ChatColor.GREEN + player.getKDR());
				}
			}
			else if(args[0].equalsIgnoreCase("help") || args[0].equals("?")){
				sender.sendMessage(ChatColor.GOLD + "MaxPvP Commands:");
				sender.sendMessage(ChatColor.GREEN + "/pvp " + ChatColor.GOLD + "View top killers and your stats.");
				sender.sendMessage(ChatColor.GREEN + "/pvp deaths " + ChatColor.GOLD + "View players with the most deaths.");
				sender.sendMessage(ChatColor.GREEN + "/pvp kdr " + ChatColor.GOLD + "View the players with the best Kill:Death ratio.");
				sender.sendMessage(ChatColor.GREEN + "/pvp <playerName> " + ChatColor.GOLD + "View a specific players stats.");
			}
			else{
				KarmaPlayer kp = plugin.getKarmaPlayer(args[0], true);
				if(kp == null){
					sender.sendMessage(ChatColor.RED + "No such player: " + args[0]);
				}
				else{
					sender.sendMessage(ChatColor.GREEN + kp.getFormattedName() + "'s PvP Stats:");
					sender.sendMessage(ChatColor.GOLD + "Kills: " + ChatColor.GREEN + kp.getTotalKills() + ChatColor.GOLD +  " Deaths: " + ChatColor.GREEN + kp.getTotalDeaths() + ChatColor.GOLD +  " Kill:Death Ratio: " + ChatColor.GREEN + kp.getKDR());
				}
			}
		}
		else{
			sender.sendMessage(ChatColor.RED + "No permission");
		}
		
		return true;
	}
}