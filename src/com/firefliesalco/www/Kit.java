package com.firefliesalco.www;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Kit implements Serializable{

	
	private static final long serialVersionUID = 5203046552939112422L;
	ArrayList<DecompiledItemStack> items = new ArrayList<DecompiledItemStack>();
	String name;
	String display;
	int delay;
	
	public Kit(String name, String display, int delay){
		this.name = name;
		this.display = display;
		this.delay = delay;
	}
	
	public ItemStack getItem(int num){
		ItemStack is = items.get(num).getItemStack();
		ItemMeta im = is.getItemMeta();
		List<String> lore = new ArrayList<String>();
		if(im.getLore() != null) lore = im.getLore();
		int i = 0;
		ArrayList<String> newLore = new ArrayList<String>();
		Random r = new Random();
		for(String s : lore){
			String[] words = s.split(" ");
			if(words.length >= 3){
				if(words[words.length-2].equalsIgnoreCase("chance")){
					int chance = Integer.parseInt(words[words.length-1]);
					if(r.nextInt(100) < chance){
						String output = "";
						for(int j = 0; j < words.length - 2; j++){
							if(j != 0){
								output += " ";
							}
							output += words[j];
						}
						newLore.add(output);
					}
				}
			}else{
				newLore.add(s);
			}
			i++;
		}
		im.setLore(newLore);
		is.setItemMeta(im);
		return is;
	}
	
}
