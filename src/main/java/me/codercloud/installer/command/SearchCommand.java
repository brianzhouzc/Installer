package me.codercloud.installer.command;

import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.install.ProjectLoader;
import me.codercloud.installer.utils.CommandArgs;
import me.codercloud.installer.utils.CommandHandler;
import me.codercloud.installer.utils.RunTask;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SearchCommand extends CommandArgs {
	
	private InstallerPlugin plugin;
	
	public SearchCommand(InstallerPlugin p) {
		super("/inst search <...>");
		this.plugin = p;
	}

	@Override
	public void handleCommand(CommandHandler h, CommandSender sender,
			Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You have to be a player");
			return;
		}
		String q = getVar("<...>", args);
		new RunTask<InstallerPlugin>(new ProjectLoader((Player) sender, q), plugin);
	}
	
	@Override
	public boolean hasPermission(CommandSender p) {
		return p.hasPermission("installer.install");
	}
}
