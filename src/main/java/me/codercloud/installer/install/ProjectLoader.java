package me.codercloud.installer.install;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;

import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.utils.Loader;
import me.codercloud.installer.utils.Task;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ProjectLoader extends Task<InstallerPlugin> {
	
	public static void main(String[] args) {
		new ProjectLoader(null, "essentials").run(null);
	}
	
	private String search;
	private Player player;
	
	public ProjectLoader(Player player, String search) {
		this.search = search;
		this.player = player;
		this.search = search;
	}

	public void run(InstallerPlugin plugin) {
		try {
			Loader l = new Loader(getURL(), getPostData());
			
			ArrayList<Project> projects = new ArrayList<Project>();
						
			try {
				JSONArray a = (JSONArray) l.readURLJSON();
				
				for(int i = 0; i<a.size(); i++) {
					JSONObject o = (JSONObject) a.get(i);
					String name = (String) o.get("plugin_name");
					if(o.get("curse_id") == null)
						continue;
					long curseId = (Long) o.get("curse_id");
					String stage = (String) o.get("stage");
					String slug = (String) o.get("slug");
					String[] authors;
					{
						JSONArray au = (JSONArray) o.get("authors");
						authors = new String[au.size()];
						for(int j = 0; j<authors.length; j++)
							authors[j] = (String) au.get(j); 
					}
					ProjectVersion[] versions;
					{
						JSONArray v = (JSONArray) o.get("versions");
						versions = new ProjectVersion[v.size()];
						int newl = versions.length;
						for(int j = 0; j<versions.length; j++) {
							JSONObject ve = (JSONObject) v.get(j);
							
							try {
								String id = ve.get("download").toString();
								int loc = id.length();
								loc = id.lastIndexOf("/", loc-1);
								loc = id.lastIndexOf("/", loc-1);
								loc = id.lastIndexOf("/", loc-1);
								id = id.substring(loc, id.length());
								
								long date = 0;
								try {
									 date = (Long) ve.get("date");
								}catch(Exception e) {e.printStackTrace();}
								
								versions[j] = new ProjectVersion(date, id);
							} catch (Exception e) {newl--;}
							
						}
						if(newl != versions.length) {
							ProjectVersion[] li = versions;
							versions = new ProjectVersion[newl>0?newl:0];
							for(int j=0, k=0; j<versions.length; j++) {
								while(li[k++] == null);
								
							}
						}
					}
					
					
					
					projects.add(new Project(curseId, name, slug, stage, authors, versions));
				}
				
				
				try {
					setNextTask(new ProjectSelector(player, this.search, projects.toArray(new Project[projects.size()])));
				} catch (NullPointerException e) {}
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
	
	@SuppressWarnings("unchecked")
	private String getFilter() {
		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
		JSONArray search = new JSONArray();
		JSONObject search_1 = new JSONObject();
		JSONObject search_2 = new JSONObject();
		
		array.add(obj);
		
		obj.put("field", "");
		obj.put("action", "likeor");
		obj.put("value", search);
		
		search.add(search_1);
		search.add(search_2);
		
		search_1.put("plugin_name", this.search);
		search_2.put("slug", this.search.replaceAll(" ", "-").toLowerCase());
		
		return array.toString();
	}
	
	private String getURL() {
		return "http://api.bukget.org/3/search/";
	}
	
	private byte[] getPostData() throws IOException {
		StringBuilder s = new StringBuilder();
		
		s.append("filters=").append(URLEncoder.encode(getFilter(), "UTF-8"));
		s.append("&fields=slug,plugin_name,stage,authors,curse_id,versions.date,versions.download");
		
		return s.toString().getBytes();
	}
}
