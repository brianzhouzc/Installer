package me.codercloud.installer.utils.task;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;

import me.codercloud.installer.utils.Variable;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RunTask<T extends Plugin> extends BukkitRunnable {

	private static final ArrayDeque<RunTask<?>> tasks = new ArrayDeque<RunTask<?>>();
	private static final Collection<RunTask<?>> safeTasks = Collections.unmodifiableCollection(tasks);
	
	public static Collection<RunTask<?>> getActiveTasks() {
		for(RunTask<?> t : tasks)
			if(t.isActive())
				while(tasks.remove(t));
		return safeTasks;
	}
	
	public static void tryCancelAll(Plugin p) {
		for(RunTask<?> t : tasks)
			if(t.plugin == p)
				t.tryCancel();
	}
	
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
				tasks.add(this);
			}

			while (currentTask != null) {
				plugin.getServer().getPluginManager().registerEvents(currentTask, plugin);
				currentTask.run(plugin, getCancelVar());
				HandlerList.unregisterAll(currentTask);
				if(canceld)
					break;
				currentTask = currentTask.getNext();
			}
		} finally {
			synchronized (this) {
				running = false;
				canceld = true;
				while(tasks.remove(this));
			}
		}
	}
	
	public void tryCancel() {
		canceld = true;
		if(currentTask != null)
			currentTask.tryCancel();
	}
	
	private Variable<Boolean> cancelVar = null;
	
	public Variable<Boolean> getCancelVar() {
		return cancelVar != null ? cancelVar : 
			(cancelVar = new Variable<Boolean>() {
				@Override
				public Boolean getValue() {
					return RunTask.this.canceld;
				}
			});
	}
	
	public boolean isActive() {
		synchronized (this) {
			return running;
		}
	}

}
