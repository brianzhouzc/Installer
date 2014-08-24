package me.codercloud.installer.install;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.utils.Loader;
import me.codercloud.installer.utils.Variable;
import me.codercloud.installer.utils.task.Task;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.InvalidPluginException;

public class PluginFileLoader extends Task<InstallerPlugin> {
	
	private Project project;
	private Version version;
	private Player player;
	
	public PluginFileLoader(Player player, Project project, Version version) {
		this.player = player;
		this.project = project;
		this.version = version;
	}

	public void run(InstallerPlugin plugin, Variable<Boolean> cancelVar) {
		try {
			
			Loader l = new Loader(version.getDownload(), null);
			
			setNextTask(new PluginFileSelector(player, project, version, parseData(l.readURLBytes(cancelVar))));
			
		} catch (InterruptedException e) {
			player.sendMessage(ChatColor.RED + "Your download got canceled");
		} catch (AuthorNagException e) {
			player.sendMessage(e.getMessage());
			return;
		} catch (Exception e) {
			e.printStackTrace();
			player.sendMessage(ChatColor.RED
					+ "Internal error! Check console for more information");
			return;
		}
	}
	
	private PluginFile[] parseData(byte[] data) {
		try {
			return new PluginFile[] {new PluginFile(data)};
		} catch (Exception e) {
		}
		
		try {
			ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(data));
			
			ArrayList<byte[]> files = new ArrayList<byte[]>();
			
			ZipEntry e;
			while((e = in.getNextEntry()) != null) {
				String name = e.getName();
				int index = name.lastIndexOf(".");
				if(index != -1 && name.length()>index+1 && name.substring(index+1, name.length()).equalsIgnoreCase("jar")) {
					ByteArrayOutputStream s = new ByteArrayOutputStream();
					byte[] buff = new byte[1024];
					for(int i = in.read(buff); i != -1; i = in.read(buff))
						s.write(buff, 0, i);
					files.add(s.toByteArray());
				}
			}
			
			ArrayList<PluginFile> plugins = new ArrayList<PluginFile>();
			
			for(byte[] f : files) 
				try {
					plugins.add(new PluginFile(f));
				} catch (InvalidPluginException ex) {}
			
			if(plugins.size() == 0)
				return null;
			
			return plugins.toArray(new PluginFile[plugins.size()]);
		} catch (Exception e) {

		}
		
		return new PluginFile[0];
	}

	@Override
	public void tryCancel() {
		// TODO Auto-generated method stub
		
	}
	
}
