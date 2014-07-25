package me.codercloud.installer.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public final class CommandHandler {

	private final CommandArgs[] cmds;
	private final String unknownCommand;
	
	public CommandHandler(String unknownMessage, CommandArgs... cmds) {
		this.cmds = cmds;
		this.unknownCommand = unknownMessage;
	}

	public boolean handleCommand(CommandSender sender, Command command,
			String label, String[] args) {
		boolean found = false;;
		for (CommandArgs cmd : cmds) {
			if (cmd.isCommand(command, args)) {
				if (cmd.hasPermission(sender))
					cmd.handleCommand(this, sender, command, label, args);
				else
					cmd.sendPermissionMessage(sender);
				found = true;
			}
		}
		
		StringBuilder cmd = new StringBuilder().append("/").append(label);
		for(String s : args)
			cmd.append(" ").append(s);
		
		if(!found && unknownCommand != null)
			sender.sendMessage(unknownCommand.replaceAll("<cmd>", "'" + cmd + "'").replaceAll("<user>", sender.getName()));
		return true;
	}
	
	public List<String> handleTabComplete(CommandSender sender, Command command,
			String label, String[] args) {
		ArrayList<String> strings = new ArrayList<String>();
		
		for (CommandArgs cmd : cmds) {
			cmd.tabComplete(strings, sender, command, label, args);
		}
		
		return strings;
	}
	
	private String helpHeader = ChatColor.YELLOW + "Help (Page <p>/<mp>):",
			noCmdFound = ChatColor.RED + "No commands found",
			descSeperator = " -> ";
	private ChatColor c1 = ChatColor.GREEN, c2 = ChatColor.DARK_GREEN;

	public void setHelpFormat(String helpHeader, String noCmdFound,
			String descSeperator, ChatColor flip1, ChatColor flip2) {
		this.helpHeader = helpHeader;
		this.noCmdFound = noCmdFound;
		this.descSeperator = descSeperator;
		this.c1 = null;
		this.c2 = null;
	}

	public void sendHelp(CommandSender sender, int page, int perPage) {
		ArrayList<CommandArgs> cmds = new ArrayList<CommandArgs>();
		for (CommandArgs c : this.cmds)
			if (c.hasPermission(sender))
				cmds.add(c);
		if (cmds.size() == 0) {
			if (helpHeader == null) {
				if (noCmdFound == null)
					return;
				sender.sendMessage(noCmdFound);
			} else {
				if (noCmdFound == null) {
					sender.sendMessage(helpHeader.replaceAll("<p>", "0")
							.replaceAll("<mp>", "0"));
				} else {
					sender.sendMessage(new String[] {
							helpHeader.replaceAll("<p>", "0").replaceAll(
									"<mp>", "0"), noCmdFound });
				}
			}
			return;
		}

		int maxPage = (cmds.size() - 1) / perPage + 1;
		if (page < 1)
			page = 1;
		if (page > maxPage)
			page = maxPage;
		int offs = (page - 1) * perPage;
		int len = cmds.size() - offs;
		if(len > perPage)
			len = perPage;
		String[] msg;
		int a;
		if (helpHeader == null) {
			msg = new String[len];
			a = 0;
		} else {
			msg = new String[len + 1];
			msg[0] = helpHeader.replaceAll("<p>", "" + page).replaceAll("<mp>",
					"" + maxPage);
			a = 1;
		}
		for (int i = 0; i < len; i++) {
			CommandArgs args = cmds.get(i + offs);
			String name = args.getCommandFormat();
			String desc = args.getDescription();
			msg[i+a] = ((i%2==1)? (c1 == null ? ChatColor.RESET:c1) : (c1 == null ? ChatColor.RESET:c2)) + name + "";
			if(desc == null)
				continue;
			if(descSeperator == null)
				msg[i+a] += ": ";
			else
				msg[i+a] += descSeperator;
			msg[i+a] += desc;
		}
		sender.sendMessage(msg);
	}

}
