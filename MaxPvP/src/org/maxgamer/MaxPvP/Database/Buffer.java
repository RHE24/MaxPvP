package org.maxgamer.MaxPvP.Database;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.maxgamer.MaxPvP.MaxPvP;

public class Buffer{
	private List<String> queries = new ArrayList<String>(30);
	public boolean isLocked = false;
	private BufferWatcher bufferWatcher;
	
	private MaxPvP plugin;
	public Buffer(MaxPvP plugin, Database db){
		this.plugin = plugin;
		this.bufferWatcher = new BufferWatcher(plugin, db, this);
		
		this.bufferWatcher.setTask(Bukkit.getScheduler().runTaskLater(plugin, this.bufferWatcher, 300));
	}
	/**
	 * @return Returns the buffer List with all queries
	 */
	public List<String> getQueries(){
		return this.queries;
	}
	
	public void addQuery(final String s){
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable(){
			public void run(){
				waitForUnlock();
				setLocked(true);
				queries.add(s);
				setLocked(false);
			}
		});
	}
	
	public BufferWatcher getWatcher(){
		return this.bufferWatcher;
	}
	
	public void waitForUnlock(){
		while(this.isLocked){
			//Nothing
		}
	}
	
	public boolean isLocked(){
		return this.isLocked;
	}
	public void setLocked(boolean lock){
		this.isLocked = lock;
	}
	public void setLocked(){
		this.setLocked(true);
	}
	public void clear(){
		this.queries.clear();
	}
}