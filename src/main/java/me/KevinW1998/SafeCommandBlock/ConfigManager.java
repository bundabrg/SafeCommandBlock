package me.KevinW1998.SafeCommandBlock;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import me.KevinW1998.SafeCommandBlock.utils.SafeCommandUtils;

public class ConfigManager {
	
	public File FilterConfig;
	public FileConfiguration FilterConfigYml;
	public boolean isBlacklist;
	public boolean hasWorldWhitelist;
	public List<String> Filter;
	public List<String> FilterWorlds;

	private JavaPlugin plugin;
	
	public ConfigManager(JavaPlugin pl){
		plugin = pl;
	}
	
	
	public void rl() {
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();
		isBlacklist = plugin.getConfig().getBoolean("blacklistmode");
		hasWorldWhitelist = plugin.getConfig().getBoolean("whitelistworld");
		try {
			manageFilter();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// internal
	public void manageFilter() throws Exception {
		File FilterConfig = new File(plugin.getDataFolder(), "filter.yml");
		if (!FilterConfig.exists()) {
			FilterConfig.getParentFile().mkdirs();
			SafeCommandUtils.copy(plugin.getResource("filter.yml"), FilterConfig);
		}
		FilterConfigYml = new YamlConfiguration();
		FilterConfigYml.load(FilterConfig);
		if (isBlacklist) {
			Filter = FilterConfigYml.getStringList("blacklist");
		} else {
			Filter = FilterConfigYml.getStringList("whitelist");
		}
		if (hasWorldWhitelist) {
			FilterWorlds = FilterConfigYml.getStringList("worlds");
		} else {
			FilterWorlds = new ArrayList<String>();
		}
	}
}
