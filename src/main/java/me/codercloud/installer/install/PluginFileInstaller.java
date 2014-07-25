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
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;
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
				
		HashMap<String, String> installed = new HashMap<String, String>();
		boolean reloadNeeded = false;
		boolean error = false;
		
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
				if (output == null)
					output = plugin.getPluginUtil().findFileForPlugin(pluginfile.getName());
				if(output == null)
					throw new AuthorNagException(ChatColor.RED + "Couldn't find a file for " + pluginfile.getName() + " v" + pluginfile.getVersion());
				
				if(!output.exists()) {
					output.getParentFile().mkdirs();
					output.createNewFile();
				}
				
				OutputStream out = new FileOutputStream(output);
				
				out.write(pluginfile.getData());
				
				out.close();
				
				installed.put(pluginfile.getName(), (pluginfile.isUpdate() ? ChatColor.LIGHT_PURPLE : ChatColor.GREEN).toString());
				
				if(update && !plugin.getPluginUtil().unloadPlugin(oldVersion)) {
					p.sendMessage(ChatColor.LIGHT_PURPLE + "Could not unload " + oldVersion.getName() + " v" + oldVersion.getDescription().getVersion());
					reloadNeeded = true;
					continue;
				}
				
				try {
					toEnable.add(Bukkit.getServer().getPluginManager().loadPlugin(output));
				} catch (UnknownDependencyException e) {
					p.sendMessage(ChatColor.LIGHT_PURPLE + "You need '" + e.getMessage() + "' installed to load " + pluginfile.getName());
					continue;
				} catch (InvalidPluginException e) {
					Throwable cause = e.getCause();
					if(cause != null && cause instanceof LinkageError) {
						p.sendMessage(ChatColor.LIGHT_PURPLE + "Problems while loading " + pluginfile.getName() + " v" + pluginfile.getVersion());
						reloadNeeded = true;
						continue;
					}
					e.printStackTrace();
					error = true;
					throw new AuthorNagException(ChatColor.RED + "Error while loading " + pluginfile.getName() + " v" + pluginfile.getVersion());
				} catch (Exception e) {
					e.printStackTrace();
					error = true;
					throw new AuthorNagException(ChatColor.RED + "Error while loading " + pluginfile.getName() + " v" + pluginfile.getVersion());
				}
				
			} catch (AuthorNagException e) {
				p.sendMessage(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				error = true;
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
		
		if(error) {
			p.sendMessage(ChatColor.RED + "An Error occured! A reload or restart might fix it, but its not guaranteed...\n"
					+ "Errorreports got printed to the console");
		} else if(reloadNeeded) {
			p.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "A reload or restart should apply all changes");
		}
	}

}
