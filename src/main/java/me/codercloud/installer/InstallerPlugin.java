package me.codercloud.installer;

import java.io.File;

import me.codercloud.installer.command.HelpCommand;
import me.codercloud.installer.command.LoadCommand;
import me.codercloud.installer.command.SearchCommand;
import me.codercloud.installer.utils.CommandHandler;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class InstallerPlugin extends JavaPlugin {
	
	private CommandHandler h;
	
	@Override
	public void onEnable() {
		h = new CommandHandler(ChatColor.BLUE + "Use '/inst help' to see all commands", 
				new SearchCommand(this),
				new HelpCommand(),
				new LoadCommand(this));
	}
	
	@Override
	public void onDisable() {
		System.out.println("BYE!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		return h.handleCommand(sender, command, label, args);
	}
	
	@Override
	public File getFile() {
		return super.getFile();
	}
	
}