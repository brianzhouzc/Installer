package me.codercloud.installer;

import java.io.File;
import java.util.List;

import me.codercloud.installer.command.HelpCommand;
import me.codercloud.installer.command.LoadCommand;
import me.codercloud.installer.command.SearchCommand;
import me.codercloud.installer.utils.CommandHandler;
import me.codercloud.installer.utils.PluginUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class InstallerPlugin extends JavaPlugin {
	
	private CommandHandler h;
	private PluginUtil pluginUtil;
	
	public InstallerPlugin() {
		h = new CommandHandler(ChatColor.BLUE + "Use '/inst help/?' to see all commands", 
				new SearchCommand(this),
				new HelpCommand(),
				new LoadCommand(this));
		h.setDefaultHelpFormat(ChatColor.BLUE + "<Installer> Help Page (<page>/<maxpage>)", " -> ", ChatColor.RED + "No commands found!", ChatColor.GREEN, ChatColor.DARK_GREEN);
		pluginUtil = new PluginUtil(this);
	}
	
	@Override
	public void onEnable() {
		
	}
	
	@Override
	public void onDisable() {
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		return h.handleCommand(sender, command, label, args);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String label, String[] args) {
		return h.handleTabComplete(sender, command, label, args);
	}
	
	public PluginUtil getPluginUtil() {
		return pluginUtil;
	}
	
	@Override
	public File getFile() {
		return super.getFile();
	}
	
}