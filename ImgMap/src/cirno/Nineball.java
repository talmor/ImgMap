package cirno;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;


public class Nineball extends JavaPlugin {
	protected static MapRenderer normalrender;
	protected final String[] Formats = {"jpg","jpeg","png","gif","bmp"};
	protected DataSaver ds;
	
	public void onEnable(){
		try {
			ds = new DataSaver(this);
			ds.initializeFile();
			ds.setGlobalMaps();
			System.out.print("Set global maps.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Boolean checkForImgType(String url, CommandSender s){
		if(url.contains("goo.gl") || url.contains("bit.ly") || url.contains("tinyurl.com")){
			return true;
		}
		for(String urls : Formats){
			if(url.endsWith(urls))
				return true;
		}
		getServer().getPlayer(s.getName()).sendMessage(ChatColor.RED + "[ImgMap] Image format not supported!");
		return false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "[ImgMap] You must be a player to use this plugin!");
		}
		if(command.getName().startsWith("map") && sender.hasPermission("imgmap.render") || sender.isOp()){
			if(!(args.length == 1)){
				sender.sendMessage(ChatColor.RED + "[ImgMap] That commands requires an argument! Eg. /map example.com/img.jpg");
			}
			if(checkForImgType(args[0], sender)){
				ItemStack item = getServer().getPlayer(sender.getName()).getItemInHand();
				if(item.getType() == Material.MAP){
					MapView map = Bukkit.getServer().getMap(item.getDurability());
					if(normalrender != new ImgRenderer()){
						normalrender = map.getRenderers().get(0);
					}
					try{ map.removeRenderer(map.getRenderers().get(0));  }catch(Exception e){}
					map.addRenderer(new ImgRenderer(args[0]));
					ds.setMapData(item.getDurability(), args[0]);
					getServer().getPlayer(sender.getName()).sendMessage(ChatColor.GREEN + "[ImgMap] Now rendering " + args[0]);
				} else {
					getServer().getPlayer(sender.getName()).sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "[ImgMap] That isn't a map item!");
				}
				return true;
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Cirno sad. Cirno want you to access command, but Cirno cannot let you. Cirno will leak tears :'(");
			return true;
		}

		if(command.getName().equalsIgnoreCase("restoremap") && (sender.hasPermission("imgmap.clear") || sender.isOp())){
			ItemStack item = getServer().getPlayer(sender.getName()).getItemInHand();	
			if(item.getType() == Material.MAP && normalrender != null){
				MapView map = Bukkit.getServer().getMap(item.getDurability());
				try{ map.removeRenderer(map.getRenderers().get(0));  }catch(Exception e){}
				map.addRenderer(normalrender);
				return true;
			} else {
				getServer().getPlayer(sender.getName()).sendMessage(ChatColor.RED + "[ImgMap] Could not restore the normal rendering!");
				return true;
			}
		}
		return true;
	}
}
