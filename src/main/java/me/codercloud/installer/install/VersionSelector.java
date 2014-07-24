package me.codercloud.installer.install;

import java.util.Arrays;
import java.util.Comparator;

import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.utils.BaseUtil;
import me.codercloud.installer.utils.TaskMenu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class VersionSelector extends TaskMenu<InstallerPlugin> implements Comparator<Version> {
	
	private Project project;
	private Version[] versions;
	private boolean sorted = false;
	
	private int selected = 0;
	private int page = 0;
	
	public VersionSelector(Player p, Project project, Version[] versions) {
		super(ChatColor.GREEN + project.getPluginName(), p, 3);
		this.project = project;
		this.versions = new Version[versions.length];
		for(int i = 0; i<versions.length; i++)
			this.versions[i] = versions[versions.length-i-1];
		if(versions.length == 0) {
			p.sendMessage(ChatColor.RED + "No versions found");
			throw new NullPointerException();
		}
	}

	@Override
	public boolean onClickTop(InstallerPlugin plugin, int slot, Inventory inv,
			InventoryView v) {
		boolean upd = false;
		if(slot >= 0 && slot < 18) {
			int loc = (page-1)*18+slot;
			if(versions.length > loc)
				selected = loc;
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
			setNextTask(new PluginFileLoader(getPlayer(), project, versions[selected]));
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

	@Override
	public void updateInventory(InstallerPlugin plugin, InventoryView v) {
		if(!sorted)
			sort();
		Inventory inv = v.getTopInventory();
		
		if(inv.getSize() != 27) {
			v.close();
			return;
		}
		
		inv.clear();
		
		int maxPage = (versions.length+17)/18;
		if(page>maxPage)
			page = maxPage;
		if(page<1)
			page = 1;
		
		int offs = (page-1)*18;
		
		for(int i = 0; i<18 && i+offs<versions.length; i++)
			inv.setItem(i, versions[i+offs].asItemStack(i+offs==selected));
		
		inv.setItem(22, getPageIndicator(page, maxPage));
		
		if(page != 1)
			inv.setItem(21, getPrevPageIndicator(page));
		if(page != maxPage)
			inv.setItem(23, getNextPageIndicator(page));
		
		inv.setItem(18, getCancelIndicator());
		inv.setItem(26, getSubmitIndicator());
		
	}
	
	private void sort() {
		if(versions.length>0)
			Arrays.sort(versions, this);
	}

	private ItemStack getPageIndicator(int page, int maxpage) {
		return BaseUtil.setName(new ItemStack(Material.NAME_TAG), ChatColor.BLUE + "Page (" + page + "/" + maxpage + ")");
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
		Version selected = versions[this.selected];
		return BaseUtil.setNameAndLore(new ItemStack(Material.EXP_BOTTLE), ChatColor.GREEN + "Select File:",
				ChatColor.BOLD + "" + ChatColor.BLUE + selected.getName(),
				"Type: " + selected.getType()
				);
	}

	@Override
	public void finish(InstallerPlugin plugin) {
		
		
	}

	public int compare(Version p1, Version p2) {
		return p1.compareTo(p2);
	}

}
