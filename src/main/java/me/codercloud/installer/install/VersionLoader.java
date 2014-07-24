package me.codercloud.installer.install;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.utils.Loader;
import me.codercloud.installer.utils.Task;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class VersionLoader extends Task<InstallerPlugin> {
	
	
	private Player player;
	private Project project;
	
	public VersionLoader(Player player, Project project) {
		this.player = player;
		this.project = project;
	}

	public void run(InstallerPlugin plugin) {
		
		try {
			Loader l = new Loader(getURL(), getPostData());
			
			try {
				JSONArray a = (JSONArray) l.readURLJSON();
				
				ArrayList<Version> versions = new ArrayList<Version>();
				
				for(int i = 0; i<a.size(); i++) {
					JSONObject o = (JSONObject) a.get(i);
					
					String download = o.get("downloadUrl").toString();
					String name = o.get("name").toString();
					String type = o.get("releaseType").toString();
					
					ProjectVersion v = project.getVersion(download);
					
					if(v == null)
						continue;
					
					versions.add(new Version(v, name, type, download));
				}
				
				try {
					setNextTask(new VersionSelector(player, project, versions.toArray(new Version[versions.size()])));
				} catch(NullPointerException e) {}
			} catch (Exception e) {
				e.printStackTrace();
				player.sendMessage("Could not parse recieved data");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			player.sendMessage(ChatColor.RED + "Error while connecting to bukkit");
		} catch (IOException e) {
			e.printStackTrace();
			player.sendMessage(ChatColor.RED + "Error while connecting to bukkit");
		}
	}
	
	
	private String getURL() {
		return "https://api.curseforge.com/servermods/files?projectIds="
				+ Long.toString(project.getCurseId());
	}
	
	private byte[] getPostData() throws IOException {
		return null;
	}
}
