package me.codercloud.installer.utils;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BaseUtil  {

	public static String connect(String a, String ... strings) {
		StringBuilder b = new StringBuilder();
		for(int i = 0, j = strings.length-1; i<j; i++)
			b.append(strings[i]).append(a);
		if(strings.length>0)
			b.append(strings[strings.length-1]);
		return b.toString();
	}
	
	public static ItemStack setNameAndLore(ItemStack i, String name, String ... lore) {
		return setLore(setName(i, name), lore);
	}
	
	public static ItemStack setName(ItemStack i, String name) {
		if(i == null)
			return i;
		ItemMeta m = i.getItemMeta();
		if(m == null)
			return i;
		
		m.setDisplayName(name);
		
		i.setItemMeta(m);
		return i;
	}
	
	public static ItemStack setLore(ItemStack i, String ... lore) {
		if(i == null)
			return i;
		ItemMeta m = i.getItemMeta();
		if(m == null)
			return i;
		
		ArrayList<String> l = new ArrayList<String>();
		for(String s : lore)
			l.add(s);
		
		m.setLore(l);
		
		i.setItemMeta(m);
		return i;
	}

}
