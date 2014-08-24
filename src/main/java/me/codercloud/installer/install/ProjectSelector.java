package me.codercloud.installer.install;

import java.util.Arrays;
import java.util.Comparator;

import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.utils.BaseUtil;
import me.codercloud.installer.utils.task.TaskMenu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class ProjectSelector extends TaskMenu<InstallerPlugin> implements Comparator<Project> {
	
	private String search;
	private Project[] projects;
	private boolean sorted = false;
	
	private int selected = 0;
	private int page = 0;
	
	public ProjectSelector(Player p, String search, Project[] projects) {
		super(ChatColor.GREEN + "Search Results", p, 3);
		this.search = search;
		this.projects = projects;
		if(projects.length == 0) {
			p.sendMessage(ChatColor.RED + "No projects found");
			throw new NullPointerException();
		}
	}

	@Override
	public boolean onClickTop(InstallerPlugin plugin, int slot, Inventory inv,
			InventoryView v) {
		boolean upd = false;
		if(slot >= 0 && slot < 18) {
			int loc = (page-1)*18+slot;
			if(projects.length > loc)
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
			setNextTask(new VersionLoader(getPlayer(), projects[selected]));
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
		
		int maxPage = (projects.length+17)/18;
		if(page>maxPage)
			page = maxPage;
		if(page<1)
			page = 1;
		
		int offs = (page-1)*18;
		
		for(int i = 0; i<18 && i+offs<projects.length; i++)
			inv.setItem(i, projects[i+offs].asItemStack(i+offs==selected));
		
		inv.setItem(22, getPageIndicator(page, maxPage));
		
		if(page != 1)
			inv.setItem(21, getPrevPageIndicator(page));
		if(page != maxPage)
			inv.setItem(23, getNextPageIndicator(page));
		
		inv.setItem(18, getCancelIndicator());
		inv.setItem(26, getSubmitIndicator());
		
	}
	
	private void sort() {
		if(projects.length>0)
			Arrays.sort(projects, this);
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
		Project selected = projects[this.selected];
		return BaseUtil.setNameAndLore(new ItemStack(Material.EXP_BOTTLE), ChatColor.GREEN + "Select Project:",
				ChatColor.BOLD + "" + ChatColor.BLUE + selected.getPluginName(),
				"Stage: " + selected.getStage()
				);
	}

	@Override
	public void finish(InstallerPlugin plugin) {
		
		
	}

	public int compare(Project p1, Project p2) {
		return p1.compareTo(p2, search);
	}

	@Override
	public void tryCancel() {
		getPlayer().sendMessage(ChatColor.RED + "Your installation got canceled");
		close();
	}

}
