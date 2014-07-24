package me.codercloud.installer.install;

import java.util.ArrayList;

import me.codercloud.installer.utils.BaseUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Version {
	
	private ProjectVersion v;
	private String name;
	private String download;
	private String type;
	
	public Version(ProjectVersion v, String name, String type, String download) {
		this.v = v;
		this.name = name;
		this.download= download;
		this.type = type.length()>0?type.substring(0, 1).toUpperCase() + type.substring(1, type.length()) : type;
	}
	
	public String getDownload() {
		return download;
	}
	
	public String getName() {
		return name;
	}
	
	public ProjectVersion getProjectVersion() {
		return v;
	}

	public int compareTo(Version p2) {
		return 0;
	}

	public String getType() {
		return type;
	}

	public ItemStack asItemStack(boolean selected) {
		ItemStack i = null;
		
		ArrayList<String> lore = new ArrayList<String>();
		
		if(selected) {
			i = new ItemStack(Material.NETHER_STAR);
			BaseUtil.setName(i, ChatColor.GREEN + name);
			lore.add(ChatColor.GREEN + "" + ChatColor.ITALIC + "SELECTED");
		} else {
			if(type.equals("Release"))
				i = BaseUtil.setName(new ItemStack(Material.GOLD_INGOT), ChatColor.YELLOW + name);
			else if(type.equals("Beta"))
				i = BaseUtil.setName(new ItemStack(Material.IRON_INGOT), ChatColor.AQUA + name);
			else
				i = BaseUtil.setName(new ItemStack(Material.NETHER_BRICK_ITEM), ChatColor.GRAY + name);
			lore.add("");
		}
		
		lore.add("Type: " + type);
		lore.add(v.getDateTxt("MM/dd yyyy"));
		
		BaseUtil.setLore(i, lore.toArray(new String[lore.size()]));
		
		return i;
	}
	
}
