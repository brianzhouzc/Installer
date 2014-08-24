package me.codercloud.installer.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import me.codercloud.installer.InstallerPlugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginUtil {
	
	private InstallerPlugin plugin;
	
	public PluginUtil(InstallerPlugin plugin) {
		this.plugin = plugin;
	}
	
	@SuppressWarnings("unchecked")
	public boolean unloadPlugin(Plugin plugin) {
		PluginManager pluginManager = Bukkit.getPluginManager();
		if (pluginManager == null)
			return false;

		String name = plugin.getName();

		List<Plugin> plugins = null;
		Map<String, Plugin> lookupNames = null;
		SimpleCommandMap commandMap = null;
		Map<String, Command> knownCommands = null;

		try {
			Field pluginsField = Bukkit.getPluginManager().getClass()
					.getDeclaredField("plugins");
			pluginsField.setAccessible(true);
			plugins = (List<Plugin>) pluginsField.get(pluginManager);

			Field lookupNamesField = Bukkit.getPluginManager().getClass()
					.getDeclaredField("lookupNames");
			lookupNamesField.setAccessible(true);
			lookupNames = (Map<String, Plugin>) lookupNamesField
					.get(pluginManager);

			Field commandMapField = Bukkit.getPluginManager().getClass()
					.getDeclaredField("commandMap");
			commandMapField.setAccessible(true);
			commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

			Field knownCommandsField = SimpleCommandMap.class
					.getDeclaredField("knownCommands");
			knownCommandsField.setAccessible(true);
			knownCommands = (Map<String, Command>) knownCommandsField
					.get(commandMap);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		pluginManager.disablePlugin(plugin);

		if (plugins != null && plugins.contains(plugin))
			plugins.remove(plugin);

		if (lookupNames != null && lookupNames.containsKey(name))
			lookupNames.remove(name);
		
		if (commandMap != null) {
			HashSet<String> rem = new HashSet<String>();
			for (Entry<String, Command> e : knownCommands.entrySet()) {
				if (e.getValue() instanceof PluginIdentifiableCommand) {
					if(((PluginIdentifiableCommand) e.getValue()).getPlugin() == plugin)
						rem.add(e.getKey());
				}
			}

			for(String s : rem) {
				Command c = knownCommands.remove(s);
				
				if(c != null && c instanceof PluginIdentifiableCommand && ((PluginIdentifiableCommand) c).getPlugin() == plugin)
					c.unregister(commandMap);
				else if(c != null)
					knownCommands.put(s, c);
			}
		}
		
		ClassLoader c = plugin.getClass().getClassLoader();
		if (c instanceof URLClassLoader) {
			try {
				((URLClassLoader) c).close();
			} catch (IOException ex) {}
		}

		System.gc();

		return true;
	}
	
	public PluginDescriptionFile getDescriptionFile(File f) {
		ZipFile zip = null;
		try {
			zip = new ZipFile(f);
			ZipEntry e = zip.getEntry("plugin.yml");
			if(e != null)
				return new PluginDescriptionFile(zip.getInputStream(e));
		} catch (Exception e) {}
		finally {
			try {
				if(zip != null)
					zip.close();
			} catch (Exception e) {}
		}
		return null;
	}
	
	public File getPluginFile(Plugin plugin) {
		try {
			Field f = JavaPlugin.class.getDeclaredField("file");
			f.setAccessible(true);
			return (File) f.get(plugin);
		} catch (Exception e) {}
		return null;
	}
	
	public File findFileForPlugin(String name) {
		File file = null;
		
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(name);
		
		file = getPluginFile(plugin);
		
		if(file != null)
			return file;
		
		File pluginDir = this.plugin.getFile().getParentFile();
		
		ArrayList<File> files = new ArrayList<File>();
		
		for(File f : pluginDir.listFiles()) {
			if(!f.exists() || !f.isFile())
				continue;
			String fname = f.getName();
			int ind = fname.lastIndexOf(".");
			if(ind != -1 && fname.substring(ind+1, fname.length()).equalsIgnoreCase("jar"))
				files.add(f);
		}
		
		for(File f : files) {
			YamlConfiguration yml = getPluginDescription(f);
			if(yml != null && name.equals(yml.getString("name")))
				return f;
		}
		
		File f = new File(pluginDir, "/" + name + ".jar");
		
		if(!f.exists())
			return f;
		
		int t = 0;
		while(++t<10) {
			f = new File(pluginDir, "/" + name + "_" + t + ".jar");
			if(!f.exists())
				return f;
		}
		
		return null;
	}
	
	public YamlConfiguration getPluginDescription(File f) {
		ZipFile zip = null;
		try {
			zip = new ZipFile(f);
			ZipEntry e = zip.getEntry("plugin.yml");
			
			if (e == null)
				throw new InvalidPluginException();
			
			
			YamlConfiguration pluginyml = new YamlConfiguration();
			try {
				pluginyml.loadFromString(new String(BaseUtil.readFully(zip.getInputStream(e))));
			} catch (InvalidConfigurationException ex) {
				throw new InvalidPluginException();
			}

			return pluginyml;
		} catch (Exception e) {} finally {
			try {
				zip.close();
			} catch (Exception e) {}
		}
		return null;
	}
	

}
