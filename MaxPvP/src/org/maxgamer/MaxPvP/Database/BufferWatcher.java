package org.maxgamer.MaxPvP.Database;

import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.MaxPvP.MaxPvP;

public class BufferWatcher implements Runnable{
	private Database db;
	private Buffer buffer;
	private MaxPvP plugin;
	
	private boolean isStopped = false;
	private BukkitTask task;
	
	public BufferWatcher(MaxPvP plugin, Database db, Buffer buffer){
		this.db = db;
		this.buffer = buffer;
		this.plugin = plugin;
	}

	@Override
	public void run() {
		if(buffer.getQueries().size() > 0){
			buffer.waitForUnlock();
			buffer.setLocked(true);
			try {
				Statement st = db.getConnection().createStatement();
				for(String q : buffer.getQueries()){
					st.addBatch(q);
				}
				buffer.clear();
				buffer.setLocked(false);
				
				st.executeBatch();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
				buffer.setLocked(false);
			}
		}
		if(this.isStopped) return;
		this.setTask(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, 300));
	}
	
	public void setTask(BukkitTask task){
		this.task = task;
	}
	/**
	 * Safely stops the buffer and empties it.
	 * Cancels the Scheduler task.
	 */
	public void stop(){
		this.isStopped = true;
		//Bukkit.getScheduler().cancelTask(this.taskId);
		task.cancel();
		this.run();
	}
}