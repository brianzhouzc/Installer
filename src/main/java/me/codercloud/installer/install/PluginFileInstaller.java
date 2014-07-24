package me.codercloud.installer.install;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.utils.Task;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginFileInstaller extends Task<InstallerPlugin> {
	
	private Player p;
	@SuppressWarnings("unused")
	private Project project;
	@SuppressWarnings("unused")
	private Version version;
	private PluginFile[] files;
	
	public PluginFileInstaller(Player p, Project project, Version version, PluginFile[] files) {
		this.p = p;
		this.project = project;
		this.version = version;
		this.files = files;
	}
	
	@Override
	public void run(InstallerPlugin plugin) {
		if(files.length == 0) {
			p.sendMessage(ChatColor.LIGHT_PURPLE + "No plugin to install selected");
			return;
		}
		if(files.length == 1)
			p.sendMessage(ChatColor.BLUE + "Starting instalation of 1 file");
		else
			p.sendMessage(ChatColor.BLUE + "Starting instalation of " + files.length + " files");
		
		final File pluginDir = plugin.getFile().getParentFile();
		
		HashMap<String, String> installed = new HashMap<String, String>();
		boolean reloadNeeded = false;
		
		ArrayList<Plugin> toEnable = new ArrayList<Plugin>();
		
		for(PluginFile pluginfile : files) {
			if(installed.containsKey(pluginfile.getName())) {
				p.sendMessage(ChatColor.LIGHT_PURPLE + "Didn't install " + pluginfile.getName() + " v" + pluginfile.getVersion() + ":\n  Already installed v" + installed.get(pluginfile.getName()));
			}
			try {
				Plugin oldVersion = Bukkit.getPluginManager().getPlugin(pluginfile.getName());
				boolean update = oldVersion != null;
				
				File output = null;
				
				if(update) {
					Field f = JavaPlugin.class.getDeclaredField("file");
					f.setAccessible(true);
					output = (File) f.get(oldVersion);
				} 
				if (output == null){
					StringBuilder b = new StringBuilder();
					for(char c : pluginfile.getName().toCharArray())
						if(Character.isLetterOrDigit(c))
							b.append(c);
					String pre = b.toString();
					output = new File(pluginDir, pre + ".jar");
					int att = 0;
					while(output.exists()) {
						if(att >= 10) {
							throw new AuthorNagException(ChatColor.RED + "Could not find a file for " + pluginfile.getName() + " v" + pluginfile.getVersion());
						}
						output = new File(pluginDir, pre + att + ".jar");
					}
				}
				
				if(!output.exists()) {
					output.getParentFile().mkdirs();
					output.createNewFile();
				}
				
				OutputStream out = new FileOutputStream(output);
				
				out.write(pluginfile.getData());
				
				out.close();
				
				installed.put(pluginfile.getName(), (pluginfile.isUpdate() ? ChatColor.LIGHT_PURPLE : ChatColor.GREEN).toString());
				
				if(update) {
					reloadNeeded = true;
					continue;
				}
				
				try {
					toEnable.add(Bukkit.getServer().getPluginManager().loadPlugin(output));
				} catch (Exception e) {
					e.printStackTrace();
					throw new AuthorNagException(ChatColor.RED + "Error while loading " + pluginfile.getName() + " v" + pluginfile.getVersion());
				}
				
			} catch (AuthorNagException e) {
				p.sendMessage(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				p.sendMessage(ChatColor.RED + "Error while installing " + pluginfile.getName() + " v" + pluginfile.getVersion());
			}
		}
		
		if(installed.size() > 0) {
			StringBuilder b = new StringBuilder();
			for(Entry<String, String> pl : installed.entrySet()) {
				if(b.length() != 0)
					b.append(", ");
				b.append(pl.getValue()).append(pl.getKey()).append(ChatColor.GREEN);
			}
			p.sendMessage(ChatColor.GREEN + "Successfully installed " + b.toString());
		} else
			p.sendMessage(ChatColor.BLUE + "No plugins installed successfully");
		
		
		if(toEnable.size() > 0) {
			for(Plugin pl : toEnable)
				try {
					pl.onLoad();
				} catch (Throwable e) {
					p.sendMessage(ChatColor.RED + "Error in onLoad() of " + pl.getName());
					reloadNeeded = true;
					continue;
				}
			
			StringBuilder b = new StringBuilder();
			for(Plugin pl : toEnable)
				try {
					Bukkit.getServer().getPluginManager().enablePlugin(pl);
					if(b.length() != 0)
						b.append(", ");
					b.append(pl.getName());
				} catch (Throwable e) {
					p.sendMessage(ChatColor.RED + "Error while enabling " + pl.getName());
					reloadNeeded = true;
					continue;
				}
			if(b.length() != 0)
				p.sendMessage(ChatColor.BLUE + "Enabled " + b.toString());
		}
		
		if(reloadNeeded)
			p.sendMessage(ChatColor.BOLD + "" + ChatColor.LIGHT_PURPLE + "Please reload or restart to apply changes!");
	}

}
