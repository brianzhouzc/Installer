package me.codercloud.installer.utils.task;

import me.codercloud.installer.utils.Variable;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class Task<T extends Plugin> implements Listener {
	
	private boolean active = true;
	private Task<T> next = null;
	
	/**
	 * @param plugin
	 * @return If the task should stay up
	 */
	public abstract void run(T plugin, Variable<Boolean> canceldVar);
	
	public void tryCancel() {
		
	}
	
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
