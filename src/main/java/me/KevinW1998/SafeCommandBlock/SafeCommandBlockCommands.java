package me.KevinW1998.SafeCommandBlock;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.KevinW1998.SafeCommandBlock.utils.CommonCommand;
import me.KevinW1998.SafeCommandBlock.utils.SafeCommandUtils;

public class SafeCommandBlockCommands {
	public static final int SETTING_SET_NAME = 0;
	public static final int SETTING_SET_COMMAND = 1;
	
	private ConfigManager configManager;
	public SafeCommandBlockCommands(ConfigManager configManager) {
		this.configManager = configManager;
	}
	
	
	@CommonCommand(commandName = "resetcommand", permissionRequired = "SafeCommandBlock.resetCommand")
	public void resetCommand(CommandSender sender, String[] args)
	{
		setCommandBlockSetting((Player)sender, SETTING_SET_COMMAND, "");
	}
	
	@CommonCommand(commandName = "setcommandname", permissionRequired = "SafeCommandBlock.setCommandName")
	public void setCommandName(CommandSender sender, String[] args)
	{
		if(args.length >= 1)
			setCommandBlockSetting((Player)sender, SETTING_SET_NAME, args[0]);
		else
			setCommandBlockSetting((Player)sender, SETTING_SET_NAME, "");
	}
	
	@CommonCommand(commandName = "setcommand", permissionRequired = "SafeCommandBlock.setCommand", minArgsCount = 1)
	public void setCommand(CommandSender sender, String[] args)
	{
		setCommandSafe((Player)sender, args);
	}
	
	@CommonCommand(commandName = "scbreload", permissionRequired = "SafeCommandBlock.reload", canExecuteConsole = true, canExecutePlayer = true)
	public void reloadSafeCommandBlock(CommandSender sender, String[] args)
	{
		configManager.rl();
	}
	
	public void setCommandSafe(Player p, String[] args){
		String cmd = SafeCommandUtils.join(args, " ");
		String rootCmd = args[0];
		
		if (SafeCommandUtils.isOpOrHasPermission(p, "SafeCommandBlock.color")) {
			cmd = SafeCommandUtils.replaceColorCode(cmd);
		}
		if (SafeCommandUtils.isOpOrHasPermission(p, "SafeCommandBlock.bypass")) {
			setCommandBlockSetting(p, SETTING_SET_COMMAND, cmd);
			return;
		}
		if (configManager.hasWorldWhitelist && !configManager.FilterWorlds.contains(p.getWorld().getName())) {
			p.sendMessage(ChatColor.GOLD + p.getWorld().getName() + ChatColor.RED + " is not whitelisted! You cannot use command blocks in this world!");
			return;
		}
		if (configManager.isBlacklist) {
			if (configManager.Filter.contains(rootCmd)) {
				p.sendMessage(ChatColor.RED + "You cannot use command " + ChatColor.GOLD + rootCmd);
				return;
			}
		} else {
			if (!configManager.Filter.contains(rootCmd)) {
				p.sendMessage(ChatColor.RED + "You cannot use command " + ChatColor.GOLD + rootCmd);
				return;
			}
		}
		setCommandBlockSetting(p, SETTING_SET_COMMAND, cmd);
	}
	
	
	public void setCommandBlockSetting(Player p, int settingType, String settingValue) {
		if(settingType < SETTING_SET_NAME || settingType > SETTING_SET_COMMAND)
			throw new IllegalArgumentException("settingType for setCommandBlockSetting must be between 0 and 1!");
		
		String attributeName = "";
		if(settingType == SETTING_SET_NAME)
			attributeName = "Name";
		if(settingType == SETTING_SET_COMMAND)
			attributeName = "Command";
		
		
		Block seeBlock = SafeCommandUtils.getTargetBlock(p, 200);
		if (seeBlock.getType() != Material.COMMAND_BLOCK && !(seeBlock.getState() instanceof CommandBlock)) {
			p.sendMessage(ChatColor.RED + "You need to look at the command block to set the command!");
			return;
		}
		CommandBlock ccBlockCmd = (CommandBlock) seeBlock.getState();
		if(settingType == SETTING_SET_NAME)
			ccBlockCmd.setName(settingValue);
		else if(settingType == SETTING_SET_COMMAND)
			ccBlockCmd.setCommand(settingValue);
		ccBlockCmd.update(true);
		if (!settingValue.equals("")) {
			p.sendMessage(ChatColor.GREEN + "Set " + attributeName + ": \"" + ChatColor.GOLD + settingValue + ChatColor.GREEN + "\"!");
		} else {
			p.sendMessage(ChatColor.GREEN + "Reset " + attributeName);
		}
		return;
	}
	
	
}
