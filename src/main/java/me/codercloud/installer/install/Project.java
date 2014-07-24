package me.codercloud.installer.install;


import java.util.HashMap;

import me.codercloud.installer.utils.BaseUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Project {
	
	private static final HashMap<String, Integer> states;
	
	private long curseId;
	private String pluginName, slug, stage;
	private String[] authors;
	private ProjectVersion[] versions;
	private ProjectVersion latestFile = null;
	
	public Project(long curseId, String name, String slug, String stage, String[] authors, ProjectVersion[] versions) {
		this.curseId = curseId;
		this.pluginName = name;
		this.slug = slug;
		this.stage = stage;
		this.authors = authors;
		this.versions = versions;
		for(ProjectVersion v : versions)
			if(latestFile == null || v.getDate().after(latestFile.getDate()))
				latestFile = v;
	}
	
	public String[] getAuthors() {
		return authors;
	}
	
	public ProjectVersion[] getVersions() {
		return versions;
	}
	
	public ProjectVersion getVersion(String link) {
		for(ProjectVersion v : versions)
			if(link.length()>=v.getId().length() && v.getId().equals(link.substring(link.length()-v.getId().length(), link.length())))
				return v;
		return null;
	}
	
	public long getCurseId() {
		return curseId;
	}
	
	public String getPluginName() {
		return pluginName;
	}
	
	
	public String getSlug() {
		return slug;
	}
	
	public String getStage() {
		return stage;
	}
	
	public ItemStack asItemStack(boolean selected) {
		ItemStack i = null;
		String[] lore = new String[4];
		
		lore[0] = ChatColor.BLUE + "" + ChatColor.ITALIC + "by " + BaseUtil.connect(", ", authors);
		
		if(selected) {
			i = new ItemStack(Material.LAVA_BUCKET);
			BaseUtil.setName(i, ChatColor.YELLOW + getPluginName());
			lore[1] = ChatColor.GREEN + "" + ChatColor.ITALIC + "SELECTED";
		} else {
			i = new ItemStack(Material.BUCKET);
			BaseUtil.setName(i, ChatColor.WHITE + getPluginName());
			lore[1] = "";
		}
		
		lore[2] = "Stage: " + getStage();
		lore[3] = latestFile.getDateTxt("MM/dd yyyy");
		
		BaseUtil.setLore(i, lore);
		
		return i;
	}
	
	public int compareTo(Project p, String search) {
		if(getPluginName().equalsIgnoreCase(search) && !p.getPluginName().equalsIgnoreCase(search))
			return -1;
		
		int stage = 0;
		if(states.containsKey(getStage()))
			stage += states.get(getStage());
		else
			stage += states.get(null);
		if(states.containsKey(p.getStage()))
			stage -= states.get(p.getStage());
		else
			stage -= states.get(null);
		
		return stage;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(getPluginName()).append(" by ");
		for(int i = 0; i<authors.length; i++)
			b.append(authors[i]).append(i == 0 ? "" : i == (authors.length-2) ? " and " : ", ");
		return b.toString();
	}
	
	static {
		states = new HashMap<String, Integer>();
		states.put("Release", 0);
		states.put("Mature", 0);
		states.put("Beta", 1);
		states.put("Alpha", 2);
		states.put("Planning", 3);
		states.put("Inactive", 4);
		states.put(null, 100);
	}
	
}
