package com.firefliesalco.www;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DecompiledItemStack implements Serializable{

	
	private static final long serialVersionUID = 4259988936838907335L;
	String name;
	List<String> lore;
	Map<String, Integer> enchants = new HashMap<String, Integer>();
	Material material;
	int amount;
	Set<ItemFlag> flags;
	
	public DecompiledItemStack(ItemStack is){
		if(is.getItemMeta() == null) is.setItemMeta(Bukkit.getItemFactory().getItemMeta(is.getType()));
		lore = is.getItemMeta().getLore();
		name = is.getItemMeta().getDisplayName();
		Map<Enchantment, Integer> tempEnchant = is.getEnchantments();
		for(Enchantment key : tempEnchant.keySet()){
			enchants.put(key.getName(), tempEnchant.get(key));
		}
		material = is.getType();
		amount = is.getAmount();
		flags = is.getItemMeta().getItemFlags();
	}
	
	public ItemMeta getItemMeta(){
		ItemMeta im = Bukkit.getItemFactory().getItemMeta(material);
		for(ItemFlag flag : flags)
			im.addItemFlags(flag);
		im.setDisplayName(name);
		im.setLore(lore);
		return im;
			
	}
	
	public ItemStack getItemStack(){
		ItemStack is = new ItemStack(material, amount);
		is.setItemMeta(getItemMeta());
		for(String enchant : enchants.keySet())
			is.addUnsafeEnchantment(Enchantment.getByName(enchant), enchants.get(enchant));
		
		return is;
	}
	
}
