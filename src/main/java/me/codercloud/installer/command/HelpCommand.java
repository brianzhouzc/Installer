package me.codercloud.installer.command;

import me.codercloud.installer.utils.CommandHandler;
import me.codercloud.installer.utils.CommandHandler.CommandListener;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

public class HelpCommand extends CommandListener {
	
	public HelpCommand() {
		super("/inst <?/help> <var=page>");
	}

	@Override
	public void handleCommand(CommandHandler h, CommandSender sender,
			Command command, String label, String[] args) {
		if(isVarSet("page", args))
			sender.sendMessage(h.getHelp(sender, 1, 5));
		else {
			Integer page = getVarAsInt("page", args);
			if(page != null)
				sender.sendMessage(h.getHelp(sender, Integer.valueOf(page), 5));
			else
				sender.sendMessage(ChatColor.RED + "'" + getVar("page", args) + "' is not a number");
		}
	}
	
	@Override
	public boolean hasPermission(Permissible p) {
		return p.hasPermission("installer.help");
	}
	
}
