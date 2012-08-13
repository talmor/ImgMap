package cirno;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;


public class root extends JavaPlugin {
	protected static MapRenderer normalrender;
	protected final String[] Formats = {"jpg","jpeg","png","gif","bmp"};

	public class ImgRenderer extends MapRenderer{
		String URL;

		public ImgRenderer(String url){
			URL = url;
		}

		public ImgRenderer(){}

		@Override
		@SuppressWarnings("deprecation")
		public void render(MapView map, final MapCanvas canvas, final Player player) {
			try{
				final Thread x = new Thread(){
					public void run(){
						try {
							System.out.print("cat.");
							canvas.drawImage(0, 0, resizeImage(ImageIO.read(new URL(URL))));
							System.out.print("cats.");
						} catch (Exception e){
							e.printStackTrace();
						}
					}
				};
				x.start();
				getServer().getScheduler().scheduleSyncDelayedTask(root.this, new Runnable() {
					public void run() {
						x.stop();
						x.destroy();
					}
				}
				,200L);
			} catch(Exception e){
				e.printStackTrace();
			}
		}

		public Image resizeImage(BufferedImage originalImage){
			BufferedImage resizedImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(originalImage, 0, 0, 128, 128, null);
			g.finalize();
			g.dispose();
			resizedImage.flush();
			return resizedImage;
		}
	}

	//This will probably never work.
	/*
	public class NormalRender extends MapRenderer{
		private final WorldMap worldMap;

		public NormalRender(WorldMap worldMap) {
			super(false);
			this.worldMap = worldMap;
		}

		@Override
		public void render(MapView map, MapCanvas canvas, Player player) {
			// Map
			for (int x = 0; x < 128; ++x) {
				for (int y = 0; y < 128; ++y) {
					canvas.setPixel(x, y, worldMap.colors[y * 128 + x]);
				}
			}

			// Cursors
			MapCursorCollection cursors = canvas.getCursors();
			while (cursors.size() > 0) {
				cursors.removeCursor(cursors.getCursor(0));
			}
			for (int i = 0; i < worldMap.decorations.size(); ++i) {
				WorldMapDecoration decoration = (WorldMapDecoration) worldMap.decorations.get(i);
				cursors.addCursor(decoration.locX, decoration.locY, (byte) (decoration.rotation & 15), (byte) (decoration.type));
			}
		}
	}
	 */

	public Boolean checkForImgType(String url, CommandSender s){
		if(url.contains("goo.gl") || url.contains("bit.ly") || url.contains("tinyurl.com")){
			return true;
		}
		for(int i=0; i < Formats.length; i++){
			if(url.endsWith(Formats[i])){
				return true;
			}
		}
		getServer().getPlayer(s.getName()).sendMessage(ChatColor.RED + "[ImgMap] Image format not supported!");
		return false;
	}

	public void onDisable(){}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "[ImgMap] You must be a player to use this plugin!");
		}
		if(command.getName().startsWith("map")){
			if(sender.hasPermission("imgmap.render") /*|| sender.isOp()*/){
				if(!(args.length == 1)){
					sender.sendMessage(ChatColor.RED + "[ImgMap] That commands requires an argument! Eg. /map example.com/img.jpg");
				} else {
					if(checkForImgType(args[0], sender)){
						ItemStack item = getServer().getPlayer(sender.getName()).getItemInHand();
						if(item.getType() == Material.MAP){
							MapView map = Bukkit.getServer().getMap(item.getDurability());
							if(normalrender != new ImgRenderer()){
								normalrender = map.getRenderers().get(0);
							}
							try{ map.removeRenderer(map.getRenderers().get(0));  }catch(Exception e){}
							map.addRenderer(new ImgRenderer(args[0]));
							getServer().getPlayer(sender.getName()).sendMessage(ChatColor.GREEN + "[ImgMap] Now rendering " + args[0]);
						} else {
							getServer().getPlayer(sender.getName()).sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "[ImgMap] That isn't a map item!");
						}
						return true;
					}
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Cirno sad. Cirno want you to access command, but Cirno cannot let you. Cirno will leak tears :'(");
				return true;
			}
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
