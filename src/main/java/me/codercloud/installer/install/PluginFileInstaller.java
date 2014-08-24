package me.codercloud.installer.install;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.utils.Variable;
import me.codercloud.installer.utils.task.Task;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.UnknownDependencyException;

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
	public void run(InstallerPlugin plugin, Variable<Boolean> cancelVar) {
				
		HashSet<String> unloading = new HashSet<String>(); 
		
		for(PluginFile f : files) {
			Plugin p = Bukkit.getPluginManager().getPlugin(f.getName());
			if(p != null)
				unloading.add(p.getName());
		}
		
		boolean b = true;
		while(b) {
			b = false;
			for(Plugin p : Bukkit.getPluginManager().getPlugins()) {
				if(!unloading.contains(p.getName())) {
					List<String> depend = p.getDescription().getDepend();
					List<String> softdepend = p.getDescription().getSoftDepend();
					for(String s : depend) {
						if(unloading.contains(s)) {
							unloading.add(p.getName());
							b = true;
						}
					}
					for(String s : softdepend)
						if(unloading.contains(s)) {
							unloading.add(p.getName());
							b = true;
						}
				}
			}
		}
		
		HashMap<String, File> toLoad = new HashMap<String, File>();
		
		for(String s : unloading) {
			Plugin p = Bukkit.getPluginManager().getPlugin(s);
			if(p != null) {
				boolean success = plugin.getPluginUtil().unloadPlugin(p);
				if(!success)
					this.p.sendMessage(ChatColor.RED + "Could not unload '" + p.getName() + "'");
				else
					toLoad.put(s, plugin.getPluginUtil().getPluginFile(p));
			}
		}
		
		for(PluginFile pluginfile : this.files) {
			
			try {
				Plugin oldVersion = Bukkit.getPluginManager().getPlugin(pluginfile.getName());
				boolean update = oldVersion != null;
				
				File output = toLoad.get(pluginfile.getName());
				if(output == null)
					output = plugin.getPluginUtil().findFileForPlugin(pluginfile.getName());
				if(output == null)
					throw new AuthorNagException(ChatColor.RED + "Could not find file for '" + pluginfile.getName() + "'");
				
				
				OutputStream out = new FileOutputStream(output);
				
				out.write(pluginfile.getData());
				
				out.close();
								
				if(!update) {
					toLoad.put(pluginfile.getName(), output);
				}
			} catch (AuthorNagException e) {
				p.sendMessage(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				p.sendMessage(ChatColor.RED + "Error while installing " + pluginfile.getName() + " v" + pluginfile.getVersion());
			}
		}
				
		HashMap<PluginDescriptionFile, File> load = new HashMap<PluginDescriptionFile, File>();
		
		for(File f : toLoad.values()) {
			PluginDescriptionFile d = plugin.getPluginUtil().getDescriptionFile(f);
			if(d != null)
				load.put(d, f);
			else
				p.sendMessage(ChatColor.RED + "Could not find plugin.yml in " + f);
		}
		
		PluginDescriptionFile[] descs = setLoadOrder(load.keySet().toArray(new PluginDescriptionFile[load.keySet().size()]));
		
		ArrayList<Plugin> loaded = new ArrayList<Plugin>();
		
		for(PluginDescriptionFile d : descs) {
			File f = load.get(d);
			if(f != null) {
				try {
					loaded.add(Bukkit.getServer().getPluginManager().loadPlugin(f));
				} catch (UnknownDependencyException e) {
					p.sendMessage(ChatColor.LIGHT_PURPLE + "You need '" + e.getMessage() + "' installed to load " + d.getName());
					continue;
				} catch (Exception e) {
					e.printStackTrace();
					throw new AuthorNagException(ChatColor.RED + "Error while loading " + d.getName() + " v" + d.getVersion());
				}
			}
		}
		
		StringBuilder str = new StringBuilder();
		for(Plugin pl : loaded)
			try {
				Bukkit.getServer().getPluginManager().enablePlugin(pl);
				if(str.length() != 0)
					str.append(", ");
				str.append(pl.getName());
			} catch (Throwable e) {
				p.sendMessage(ChatColor.RED + "Error while enabling " + pl.getName());
				continue;
			}
		if(str.length() != 0)
			p.sendMessage(ChatColor.BLUE + "Enabled " + str.toString());
	}
	
	private PluginDescriptionFile[] setLoadOrder(PluginDescriptionFile[] files) {
		
		ArrayList<PluginDescriptionFile> result = new ArrayList<PluginDescriptionFile>();
		
		Map<String, PluginDescriptionFile> plugins = new HashMap<String, PluginDescriptionFile>();
		
		for(PluginDescriptionFile f : files) {
			plugins.put(f.getName(), f);
		}
		
		int lenbuffer = 0;
		
		while(!plugins.isEmpty()) {
			boolean skip = lenbuffer == plugins.size();
			lenbuffer = plugins.size();
			Iterator<PluginDescriptionFile> i = plugins.values().iterator();
			while(i.hasNext()) {
				PluginDescriptionFile d = i.next();
				boolean b = true;

				if(!skip) {
					for (String s : d.getDepend())
						if (plugins.containsKey(s))
							b = false;

					for (String s : d.getSoftDepend())
						if (plugins.containsKey(s))
							b = false;
				}
				
				if(b) {
					i.remove();
					result.add(d);
					break;
				}
			}
		}
		
		
		return result.toArray(new PluginDescriptionFile[result.size()]);
	}
	
}
