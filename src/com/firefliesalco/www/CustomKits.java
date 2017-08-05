package com.firefliesalco.www;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class CustomKits extends JavaPlugin implements Listener {

	ArrayList<Kit> kits = new ArrayList<Kit>();
	Map<UUID, Map<Integer, Integer>> usable = new HashMap<UUID, Map<Integer, Integer>>();
	@SuppressWarnings("unchecked")
	@Override
	public void onEnable(){
		
		getServer().getPluginManager().registerEvents(this, this);

		
		
		File kitSave = new File("plugins/kitSave.txt");
		
		
		if(!kitSave.exists()){
			try {
				kitSave.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(kitSave));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Object temp = ois.readObject();
			if(temp != null) kits = (ArrayList<Kit>) temp;
			
			temp = ois.readObject();
			if(temp != null) usable =  (Map<UUID, Map<Integer, Integer>>) temp;
			ois.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(kits == null) kits = new ArrayList<Kit>();
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				for(UUID uuid : usable.keySet()){
					for(int val : usable.get(uuid).keySet()){
						if(usable.get(uuid).get(val) > 0){
							usable.get(uuid).put(val, usable.get(uuid).get(val)-1);
						}
					}
				}
			}
		}, 0, 1200);
		
	}
	
	
	
	@Override
	public void onDisable(){
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("plugins/kitSave.txt")));
			oos.writeObject(kits);
			oos.writeObject(usable);
			oos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {	
		if(sender instanceof Player){
			Player p = (Player) sender;
			if(label.equalsIgnoreCase("addkit") && p.hasPermission("kits.add")){
				if(args.length == 3){
					Kit kit = new Kit(args[0], args[1], Integer.parseInt(args[2]));
					for(ItemStack is : p.getInventory().getContents()){
						if(is != null) kit.items.add(new DecompiledItemStack(is));
					}
					for(ItemStack is : p.getInventory().getArmorContents()){
						if(is.getType() != Material.AIR && is != null) kit.items.add(new DecompiledItemStack(is));
					}
					kits.add(kit);
					p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Kit Added");
				}
				
			}
			if(label.equalsIgnoreCase("delkit") && p.hasPermission("kits.delete")){
				if(args.length == 1){
					int index = -1;
					int i = 0;
					for(Kit kit : kits){
						if(kit.name.equalsIgnoreCase(args[0])){
							index = i;
						}
						i++;
					}
					if(index != -1){
						kits.remove(index);
						p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Kit Removed");
					}else{
						p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Kit not found");
					}
				}
			}
			if(label.equalsIgnoreCase("listkit") && p.hasPermission("kits.list")){
				p.sendMessage(ChatColor.GREEN + "--Kits--");
				for(Kit kit : kits){
					p.sendMessage(kit.name);
				}
			}
			if(label.equalsIgnoreCase("gkit")){
				openKits(p);
			}
		}
		return true;
	}
	
	@EventHandler
	public void inventoryClick(InventoryClickEvent event){
		if(event.getInventory().getName().equals("Kit Menu")){
			event.setCancelled(true);
			if(slotToIndex(event.getRawSlot())!= -1){
				int index = slotToIndex(event.getRawSlot());
				if(event.getWhoClicked().hasPermission("kits." + kits.get(index))){
					if(!usable.containsKey(event.getWhoClicked().getUniqueId()) || !usable.get(event.getWhoClicked().getUniqueId()).containsKey(index) || usable.get(event.getWhoClicked().getUniqueId()).get(index) == 0){
						for(int i = 0; i < kits.get(index).items.size(); i++){
							if(event.getWhoClicked().getInventory().firstEmpty() != -1)
								event.getWhoClicked().getInventory().addItem(kits.get(index).getItem(i));
							else
								event.getWhoClicked().getWorld().dropItemNaturally(event.getWhoClicked().getLocation(), kits.get(index).getItem(i));
						}
						event.getWhoClicked().sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Kit recieved");
						if(!usable.containsKey(event.getWhoClicked().getUniqueId()))
							usable.put(event.getWhoClicked().getUniqueId(), new HashMap<Integer, Integer>());
						
						usable.get(event.getWhoClicked().getUniqueId()).put(index, kits.get(index).delay);
						openKits((Player)event.getWhoClicked());
						
					}else{
						event.getWhoClicked().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + usable.get(event.getWhoClicked().getUniqueId()).get(index) + " minute(s) until this kit is usable.");
					}
				}else{
					event.getWhoClicked().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Insufficient Permission");
				}
			}
		}
	}
	
	public void openKits(Player p){
		Inventory inv = Bukkit.createInventory(null, 54, "Kit Menu");
		for(int i = 0; i < 54; i++){
			ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)(i%2==0?11:14));
			inv.setItem(i, is);
		}
		for(int i = 0; i < kits.size(); i++){
			
			int slot = indexToSlot(i);
			ItemStack display = kits.get(i).getItem(0);
			ItemMeta im = display.getItemMeta();
			String displayname = kits.get(i).display;
			while(displayname.indexOf('&')!=-1){
				int index = displayname.indexOf('&');
				displayname = displayname.substring(0, index) + ChatColor.getByChar(displayname.charAt(index+1)) + displayname.substring(index+2, displayname.length());
			}
			displayname = ChatColor.RESET + displayname;
			while(displayname.indexOf('_')!=-1){
				int index = displayname.indexOf('_');
				displayname = displayname.substring(0, index) + " " + displayname.substring(index + 1, displayname.length());
			}
			im.setDisplayName(displayname);
			ArrayList<String> lore = new ArrayList<String>();
			lore.add((p.hasPermission("kits." + kits.get(i).name) ? (!usable.containsKey(p.getUniqueId()) ||!usable.get(p.getUniqueId()).containsKey(i) || usable.get(p.getUniqueId()).get(i) == 0? ChatColor.GREEN + "" + ChatColor.BOLD + "USABLE" : ChatColor.RED + "" +ChatColor.BOLD+"UNUSABLE") : ChatColor.RED + "" + ChatColor.BOLD + "UNUSABLE"));
			im.setLore(lore);
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			display.setItemMeta(im);
			display.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 3);

			inv.setItem(slot, display);
			
			
			
		}
		p.openInventory(inv);
	}
	
	public int indexToSlot(int i){
		int position = -1;
		if(i == 0){
			position = 11;
		}
		if(i == 1){
			position = 13;
		}
		if(i == 2){
			position = 15;
		}
		if(i == 3){
			position = 21;
		}
		if(i == 4){
			position = 23;
		}
		if(i == 5){
			position = 29;
		}
		if(i == 6){
			position = 31;
		}
		if(i == 7){
			position = 33;
		}
		if(i == 8){
			position = 39;
		}
		if(i == 9){
			position = 41;
		}
		return position;
	}
	
	public int slotToIndex(int i){
		int position = -1;
		if(i == 11){
			position = 0;
		}
		if(i == 13){
			position = 1;
		}
		if(i == 15){
			position = 2;
		}
		if(i == 21){
			position = 3;
		}
		if(i == 23){
			position = 4;
		}
		if(i == 29){
			position = 5;
		}
		if(i == 31){
			position = 6;
		}
		if(i == 33){
			position = 7;
		}
		if(i == 39){
			position = 8;
		}
		if(i == 41){
			position = 9;
		}
		return position;
	}
	
	public int getKit(Player p, int spot){
		return slotToIndex(spot);
		
	}
	
	 
	
}
