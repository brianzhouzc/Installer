package me.codercloud.installer.utils;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Task<T extends JavaPlugin> implements Listener {
	
	private boolean active = true;
	private Task<T> next = null;
	
	/**
	 * @param plugin
	 * @return If the task should stay up
	 */
	public abstract void run(T plugin);
		
	protected final void setNextTask(Task<T> t) {
		next = t;
	}
	
	public final boolean getActive() {
		return active;
	}
	
	public final Task<T> getNext() {
		return next;
	}
	
}
