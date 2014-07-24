package me.codercloud.installer.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.codercloud.installer.utils.CommandArgs;
import me.codercloud.installer.utils.CommandHandler;

public class HelpCommand extends CommandArgs {
	
	public HelpCommand() {
		super("/inst <?/help> <var=page>");
	}

	@Override
	public void handleCommand(CommandHandler h, CommandSender sender,
			Command command, String label, String[] args) {
		String page = getVar("page", args);
		
		if(page == null)
			h.sendHelp(sender, 1, 5);
		else
			try {
				h.sendHelp(sender, Integer.valueOf(page), 5);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "'" + page + "' is not a number");
			}
	}
	
	@Override
	public boolean hasPermission(CommandSender p) {
		return p.hasPermission("installer.help");
	}
	
}
