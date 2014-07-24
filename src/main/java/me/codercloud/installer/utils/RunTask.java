package me.codercloud.installer.utils;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RunTask<T extends JavaPlugin> extends BukkitRunnable {

	private T plugin;
	private boolean running = false;
	private boolean canceld = false;
	private Thread thread = null;
	private Task<T> currentTask;

	public RunTask(Task<T> initialTask, T plugin) {
		this.plugin = plugin;
		runTaskAsynchronously(this.plugin);
		currentTask = initialTask;
	}

	public void run() {
		try {
			synchronized (this) {
				if (thread != null)
					throw new IllegalStateException("Can't start same task twice");
				thread = Thread.currentThread();
				if (canceld)
					return;
				running = true;
			}

			while (currentTask != null) {
				plugin.getServer().getPluginManager().registerEvents(currentTask, plugin);
				currentTask.run(plugin);
				HandlerList.unregisterAll(currentTask);
				currentTask = currentTask.getNext();
			}
		} finally {
			synchronized (this) {
				running = false;
				canceld = true;
			}
		}
	}

	public boolean isActive() {
		synchronized (this) {
			return running;
		}
	}

}
