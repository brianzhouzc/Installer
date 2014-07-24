package me.codercloud.installer.install;

import java.util.ArrayList;

import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.utils.BaseUtil;
import me.codercloud.installer.utils.TaskMenu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class PluginFileSelector extends TaskMenu<InstallerPlugin> {

	private Project project;
	private Version version;
	private PluginFile[] files;
	private boolean[] selected;
	private int page = 0;
		
	
	public PluginFileSelector(Player player, Project project, Version version, PluginFile[] files) {
		super(ChatColor.GREEN + "Select Files:", player, 3);
		this.project = project;
		this.version = version;
		this.files = files;
		this.selected = new boolean[files.length];
		for(int i = 0; i<selected.length; i++)
			selected[i] = true;
		if(files.length == 0) {
			player.sendMessage(ChatColor.RED + "No plugins found");
			throw new NullPointerException();
		}
	}

	@Override
	public boolean onClickTop(InstallerPlugin plugin, int slot, Inventory inv,
			InventoryView v) {
		boolean upd = false;
		if(slot >= 0 && slot < 18) {
			int loc = (page-1)*18+slot;
			if(files.length > loc)
				selected[loc] = !selected[loc];
			upd = true;
		}
		
		if(slot == 21)
			upd = page == page--;
		
		if(slot == 23)
			upd = page == page++;
		
		if(slot == 18) {
			close();
		}
		
		if(slot == 26) {
			close();
			
			ArrayList<PluginFile> f = new ArrayList<PluginFile>();
			for(int i = 0; i<selected.length; i++)
				if(selected[i])
					f.add(files[i]);
			
			setNextTask(new PluginFileInstaller(getPlayer(), project, version, f.toArray(new PluginFile[f.size()])));
		}
		
		return upd;
	}

	@Override
	public boolean onClickBot(InstallerPlugin plugin, int slot, Inventory inv,
			InventoryView v) {
		return false;
	}

	@Override
	public boolean onClickOut(InstallerPlugin plugin, InventoryView i) {
		return false;
	}

	public void updateInventory(InstallerPlugin plugin, InventoryView v) {
		Inventory inv = v.getTopInventory();
		if(inv.getSize() != 27)
			return;
		
		inv.clear();
		
		int maxPage = (files.length+17)/18;
		if(page<1)
			page = 1;
		if(page>maxPage)
			page = maxPage;
		
		int offs = (page-1)*18;
		
		for(int i = 0; i<18 && i+offs<files.length; i++)
			inv.setItem(i, files[i+offs].asItemStack(selected[i+offs]));
		
		inv.setItem(22, getPageIndicator(page, maxPage));
		
		if(page != 1)
			inv.setItem(21, getPrevPageIndicator(page));
		if(page != maxPage)
			inv.setItem(23, getNextPageIndicator(page));
		
		inv.setItem(18, getCancelIndicator());
		inv.setItem(26, getSubmitIndicator());
	}
	
	private ItemStack getPageIndicator(int page, int maxpage) {
		return BaseUtil.setLore(BaseUtil.setName(new ItemStack(Material.NAME_TAG), ChatColor.BLUE + "Page (" + page + "/" + maxpage + ")"));
	}
	
	private ItemStack getNextPageIndicator(int current) {
		return BaseUtil.setName(new ItemStack(Material.PAPER), ChatColor.BLUE + "Next Page (" + (current+1) + ")");
	}
	
	private ItemStack getPrevPageIndicator(int current) {
		return BaseUtil.setName(new ItemStack(Material.PAPER), ChatColor.BLUE + "Previous Page (" + (current-1) + ")");
	}
	
	private ItemStack getCancelIndicator() {
		return BaseUtil.setName(new ItemStack(Material.REDSTONE_BLOCK), ChatColor.RED + "Cancel");
	}
	
	private ItemStack getSubmitIndicator() {
		int up = 0;
		int in = 0;
		for(int i = 0; i<selected.length; i++)
			if(selected[i])
				if(files[i].isUpdate())
					up++;
				else
					in++;
						
		return BaseUtil.setLore(BaseUtil.setName(new ItemStack(Material.EXP_BOTTLE), ChatColor.GREEN + "Install:"),
				in + " plugins will be installed",
				up + " plugins will be updated"
				);
	}

	@Override
	public void finish(InstallerPlugin plugin) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
