package com.vidhucraft.ranktest;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class RankTest extends JavaPlugin implements Listener{
	HashMap<String, RankChanger> timers = new HashMap<String, RankChanger>();
	
	@Override
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable(){
		
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent playerQuitEvent){
		timers.remove(playerQuitEvent.getPlayer().getName()).restoreRank();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("test") && args.length == 2){
			
			//Only OPs allowed
			if(!sender.isOp()){
				sender.sendMessage(ChatColor.BLUE 
						+ "[RankTest] Only " 
						+ ChatColor.RED 
						+ " OPs " 
						+ ChatColor.BLUE 
						+ "can use this command");
				return true;
			}
			
			//only players can use this command
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "[RankTest]You need to be a player to use this");
				return true;
			}
			
			//Sanity check
			try{
				Double.parseDouble(args[1]);
			}catch(NumberFormatException ex){
				return false;
			}
			
			PermissionUser user = PermissionsEx.getUser(sender.getName());
			setTempGroup(user, args[0], Double.parseDouble(args[1]));
			sender.sendMessage(ChatColor.DARK_GREEN + "[TestRank]" + ChatColor.GREEN + "You rank has now been changed to " + args[0]);
			return true;
		}
		return false;
	}
	
	/**
	 * Set a player temporarily in a group for a period of time
	 * @param user PermissionUser user to modify
	 * @param group String group to put the user in
	 * @param time double time period
	 */
	public void setTempGroup(PermissionUser user, String group, double time){
		String oldrank = user.getGroupsNames()[0];
		user.setGroups(new String[]{group});
		long timeInTicks = (long) (time * 60 * 20);
	    timers.put(user.getName(), new RankChanger(this, user, oldrank, timeInTicks));
	}
	
	/**
	 * Time for changing rank back
	 */
	public class RankChanger extends BukkitRunnable{
		private BukkitTask bukkitTask;
		private boolean hasRun = false;
		private String oldRank;
		private PermissionUser user;
		
		public RankChanger(RankTest plugin, PermissionUser user, String oldRank, long timelimt){
			this.oldRank = oldRank;
			this.user = user;
		    this.bukkitTask = this.runTaskTimer(plugin, 0, timelimt);
		}
		
		@Override
		public void run() {
			if(hasRun)
				restoreRank();
			hasRun = true;
		}
		
		public void restoreRank(){
			this.bukkitTask.cancel();
			user.setGroups(new String[]{oldRank});
			Bukkit.getPlayer(user.getName()).sendMessage(ChatColor.DARK_GREEN
					+ "[TestRank]" + ChatColor.GREEN
					+ "You rank has now been back to "
					+ oldRank);
		}
		
	}
}
