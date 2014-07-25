package me.codercloud.installer.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public abstract class CommandArgs {
	
	private final String command;
	private final String[] args;
	private final boolean[] isVar;
	private final HashMap<String, Integer> varNames = new HashMap<String, Integer>();
	
	public CommandArgs(String command) {
		if(command.startsWith("/"))
			command = command.replaceFirst("/", "");
		String[] args;
		if(command.indexOf(" ") == -1) {
			args = new String[0];
		}else{
			args = removeEmptyStrings(command.substring(command.indexOf(" ")+1, command.length()).split(" "));
			command = command.substring(0, command.indexOf(" "));
		}
		boolean[] isVar = new boolean[args.length];
		
		for(int i = 0; i<args.length; i++) {
			String arg = args[i];
			if(arg != null && arg.startsWith("<var=") && arg.endsWith(">")) {
				isVar[i] = true;
				args[i] = arg.substring(5, arg.length()-1).toLowerCase();
				if(args[i].equals("<...>"))
					throw new IllegalArgumentException("A var cant be named '<...>'");
				if(varNames.containsKey(args[i]))
					throw new IllegalArgumentException("You cant have two vars named '" + args[i] + "'");
				varNames.put(args[i], i);
			} else if(args[i].equals("<...>")) {
				isVar[i] = true;
			} else {
				isVar[i] = false;
			}
		}
		
		this.command = command;
		this.args = args;
		this.isVar = isVar;
	}
	
	public abstract void handleCommand(CommandHandler h, CommandSender sender, Command command, String label, String[] args);
	
	public void sendPermissionMessage(CommandSender p) {
		p.sendMessage(ChatColor.RED + "You are not permitted :(");
	}
	
	public boolean hasPermission(CommandSender p) {
		return true;
	}
	
	public void tabCompleteArgument(ArrayList<String> add, CommandSender s, Command c, String label, String[] args, String argName, String current) {
		
	}
	
	public void tabCompleteEnd(ArrayList<String> add, CommandSender s, Command c, String label, String[] args, String current, int endoffs) {
		
	}
	
	public String getCommandFormat() {
		String cmd = "/" + command;
		for(int i = 0; i<args.length; i++) {
			if(isVar[i]) {
				if(args[i].equals("<...>"))
					cmd += " " + "...";
				else
					cmd += " [" + args[i].toUpperCase() + "]";
			} else
				cmd += " " + args[i];
		}
		
		return cmd;
	}
	
	public String getDescription() {
		return null;
	}
	
	public final boolean isCommand(Command cmd, String[] args) {
		if(!cmd.getName().equalsIgnoreCase(command))
		    return false;
		int i;
		for(i = 0; i<this.args.length; i++) {
			if(isVar[i])
				continue;
			if(!(i<args.length))
				return false;
			String arg = this.args[i];
			if(arg.startsWith("<") && arg.endsWith(">")){
				String[] sp = arg.substring(1, arg.length()-1).split("/");
				boolean b = false;
				for(String s : sp) {
					if(s.equalsIgnoreCase(args[i]))
						b = true;
				}
				if(b)
					continue;
				else
					return false;
			}
			if(!args[i].equalsIgnoreCase(arg))
				return false;
		}
		if(args.length > i)
			if(this.args.length == 0 || !this.args[i-1].equals("<...>"))
				return false;
		return true;
	}
	
	public final boolean isCommandStart(Command cmd, String[] args) {
		if(!cmd.getName().equalsIgnoreCase(command))
		    return false;
		int i;
		for(i = 0; i<args.length; i++) {
			boolean last = i+1 == args.length;

			
			if(!(i<this.args.length))
				if(!this.args[this.args.length-1].equals("<...>"))
					return false;
				else
					return true;
			
			if(isVar[i])
				continue;
			
			String arg = this.args[i];
			if(arg.startsWith("<") && arg.endsWith(">")){
				String[] sp = arg.substring(1, arg.length()-1).split("/");
				boolean b = false;
				for(String s : sp) {
					if(s.equalsIgnoreCase(args[i]) || (last && s.toLowerCase().startsWith(args[i].toLowerCase())))
						b = true;
				}
				if(b)
					continue;
				else
					return false;
			}
			if(last) {
				if (!arg.toLowerCase().startsWith(args[i].toLowerCase()))
					return false;
			} else if(!arg.equalsIgnoreCase(args[i]))
				return false;
		}
		return true;
	}
	
	public final void tabComplete(ArrayList<String> add, CommandSender s, Command c, String label, String[] args) {
		if(isCommandStart(c, args)) {
			if(hasPermission(s)) {
				int i = args.length;
				if(i>this.args.length)
					i = this.args.length;
				
				i--;
				
				String arg = this.args[i];
				
				
				if(isVar[i]) {
					String current = args[args.length-1];
					if(arg.equals("<...>"))
						tabCompleteEnd(add, s, c, label, args, current, args.length-this.args.length);
					else
						tabCompleteArgument(add, s, c, label, args, arg, current);
				} else {
					if(arg.startsWith("<") && arg.endsWith(">")){
						String[] sp = arg.substring(1, arg.length()-1).split("/");
						for(String st : sp) {
							if((st.toLowerCase().startsWith(args[i].toLowerCase())))
								add.add(st);
						}
					} else {
						if((arg.toLowerCase().startsWith(args[i].toLowerCase())))
							add.add(arg);
					}
				}
			}
		}
	}
	
	public final String getVar(String varName, String[] args) {
		if(varName.equals("<...>")) {
			int offs = this.args.length-1;
			if(args.length-offs <= 0)
				return null;
			String s = args[offs++];
			for(;offs<args.length; offs++)
				s += " " + args[offs];
			return s;
		}
		int loc = varNames.get(varName.toLowerCase());
		if(args.length <= loc || loc < 0)
			return null;
		return args[loc];
	}
	
	private static String[] removeEmptyStrings(String[] s) {
		ArrayList<String> l = new ArrayList<String>();
		for(String str : s)
			if(str != null && str != "")
				l.add(str);
		return l.toArray(new String[l.size()]);
	}
	
}
