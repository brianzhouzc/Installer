package me.codercloud.installer.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class TaskMenu<T extends JavaPlugin> extends Task<T> {
	
	private Player player;
	private Inventory i;
	private T plugin;
	private boolean close = false;
	
	
	public TaskMenu(String title, Player p, int rows) {
		this.player = p;
		this.i = Bukkit.createInventory(null, 9*rows, title.length()>32?title.substring(0, 29) + "..." : title);
	}
	
	@EventHandler
	public final void menuClickEvent(InventoryClickEvent e) {
		Inventory top = e.getView().getTopInventory();
		if(top.equals(i)) {
			e.setCancelled(true);
			if(e.getWhoClicked() == player) {
				if(e.getRawSlot()<0) {
					if(onClickOut(plugin, e.getView()))
						updateInventory(plugin, e.getView());
				} else if(e.getRawSlot()<e.getView().getTopInventory().getSize()) {
					if(onClickTop(plugin, e.getSlot(), e.getView().getTopInventory(), e.getView()))
						updateInventory(plugin, e.getView());
				} else {
					if(onClickBot(plugin, e.getSlot(), e.getView().getBottomInventory(), e.getView()))
						updateInventory(plugin, e.getView());
				}
			} else {
				return;
			}
		}
	}
	
	@EventHandler
	public final void menuOpenEvent(InventoryOpenEvent e) {
		Inventory top = e.getView().getTopInventory();
		if(top.equals(i)) {
			if(e.getPlayer() != player) {
				e.setCancelled(true);
				return;
			}
			updateInventory(plugin, e.getView());
		}
	}
	
	public abstract boolean onClickTop(T plugin, int slot, Inventory inv, InventoryView v);
	public abstract boolean onClickBot(T plugin, int slot, Inventory inv, InventoryView v);
	public abstract boolean onClickOut(T plugin, InventoryView i);
	public abstract void updateInventory(T plugin, InventoryView v);
	public abstract void finish(T plugin);
	
	public final void close() {
		this.close = true;
	}
	
	public final Player getPlayer() {
		return player;
	}
	
	public final T getPlugin() {
		return plugin;
	}
	
	@Override
	public final void run(T plugin) {
		this.plugin = plugin;
		this.close = false;
		player.openInventory(i);
		while(!this.close && i.getViewers().contains(player))
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		finish(plugin);
		if(i.getViewers().contains(player))
			player.closeInventory();
		i.clear();
		this.plugin = null;
	}
	
}
