package me.codercloud.installer.command;

import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.install.ProjectInstaller;
import me.codercloud.installer.utils.CommandHandler;
import me.codercloud.installer.utils.CommandHandler.CommandListener;
import me.codercloud.installer.utils.task.RunTask;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class LoadCommand extends CommandListener{
	
	InstallerPlugin p;

	public LoadCommand(InstallerPlugin p) {
		super("/inst load <...>");
		this.p = p;
	}

	@Override
	public void handleCommand(CommandHandler h, CommandSender sender,
			Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You have to be a player");
			return;
		}
		try {
			new RunTask<InstallerPlugin>(new ProjectInstaller((Player) sender, getVar("<...>", args)), p);
		} catch (NullPointerException e) {}
	}
	
	@Override
	public boolean hasPermission(Permissible p) {
		return p.hasPermission("installer.load");
	}
	
}
