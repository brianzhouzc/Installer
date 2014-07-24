package me.codercloud.installer.install;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import me.codercloud.installer.utils.BaseUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;

public class PluginFile {
	private final YamlConfiguration pluginyml;
	private final byte[] data;
	private final String name;
	private final String version;
	
	public PluginFile(byte[] file) throws InvalidPluginException {
		try {
			data = file;
			ZipInputStream s = new ZipInputStream(new ByteArrayInputStream(file));
			ZipEntry e;
			while ((e = s.getNextEntry()) != null && !e.getName().equals("plugin.yml"));
			
			if (e == null)
				throw new InvalidPluginException();
			pluginyml = YamlConfiguration.loadConfiguration(new InputStreamReader(s));
			
			if(pluginyml == null)
				throw new InvalidPluginException();
			
			name = pluginyml.getString("name");
			version = pluginyml.getString("version");
			
			if(name == null || version == null)
				throw new InvalidPluginException();
		} catch (Exception e) {
			throw new InvalidPluginException();
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public boolean isUpdate() {
		return Bukkit.getServer().getPluginManager().getPlugin(name) != null;
	}

	public ItemStack asItemStack(boolean install) {
		Plugin current = Bukkit.getServer().getPluginManager().getPlugin(name);
		boolean update = current != null;
		ItemStack i = new ItemStack(Material.INK_SACK);
		
		String version = ChatColor.BLUE + (update ? "from v" + current.getDescription().getVersion() + " to v" + this.version : ("v" + this.version));
		String status = (install ? "Will":"Won't") + " get " + (update ? "updated":"installed") + " (Click to change)";
		
		if(install && update) {
			i.setDurability((short) 5);
			BaseUtil.setName(i, ChatColor.LIGHT_PURPLE + name);
		} else if(install) {
			i.setDurability((short) 10);
			BaseUtil.setName(i, ChatColor.GREEN + name);
		} else {
			i.setDurability((short) 1);
			BaseUtil.setName(i, ChatColor.RED + name);
		}
		
		BaseUtil.setLore(i, version, status);
		
		return i;
	}
	
}
