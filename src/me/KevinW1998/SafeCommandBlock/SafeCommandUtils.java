package me.KevinW1998.SafeCommandBlock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SafeCommandUtils {
	
	
	
	
	public static Block getTargetBlock(Player player, int range) {
		Location loc = player.getEyeLocation();
		Vector dir = loc.getDirection().normalize();

		Block b = null;

		for (int i = 0; i <= range; i++) {
			b = loc.add(dir).getBlock();
			if(b.getType() != Material.AIR){
				break;
			}
		}

		return b;
	}
	
	public static void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String replaceColorCode(String str) {
		String res = str.replace("&0", ChatColor.BLACK + "").replace("&1", ChatColor.DARK_BLUE + "");
		res = res.replace("&2", ChatColor.DARK_GREEN + "").replace("&3", ChatColor.DARK_AQUA + "");
		res = res.replace("&4", ChatColor.DARK_RED + "").replace("&5", ChatColor.DARK_PURPLE + "");
		res = res.replace("&6", ChatColor.GOLD + "").replace("&7", ChatColor.GRAY + "");
		res = res.replace("&8", ChatColor.DARK_GRAY + "").replace("&9", ChatColor.BLUE + "");
		res = res.replace("&a", ChatColor.GREEN + "").replace("&b", ChatColor.AQUA + "");
		res = res.replace("&c", ChatColor.RED + "").replace("&d", ChatColor.LIGHT_PURPLE + "");
		res = res.replace("&e", ChatColor.YELLOW + "").replace("&f", ChatColor.WHITE + "");
		res = res.replace("&l", ChatColor.BOLD + "").replace("&m", ChatColor.STRIKETHROUGH + "");
		res = res.replace("&n", ChatColor.UNDERLINE + "").replace("&o", ChatColor.ITALIC + "");
		res = res.replace("&n", ChatColor.RESET + "");
		return res;
	}
	
	public static String getRootCmd(String cmd) {
		return cmd.substring(1).split(" ")[0];
	}
}
