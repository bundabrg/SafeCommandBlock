package me.KevinW1998.SafeCommandBlock;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;

public class SafeCommandBlock extends JavaPlugin {
	/*
	 * Permissions: SafeCommandBlock.access SafeCommandBlock.bypass
	 * 
	 * Features: - Whitelist or Blacklist Commands - World Whitelist (optional)
	 * - Commands can be set from players even if they not op and in creative
	 * mode - Set the name of a command block - Reset the command of a command
	 * block - With permission limitation
	 */

	public final Logger logger = Logger.getLogger("Minecraft");
	public static SafeCommandBlock plugin;
	public ProtocolManager pm;
	public ConfigManager ConfigSafeCommandBlock;
	
	public SafeCommandBlock() {
		ConfigSafeCommandBlock = new ConfigManager(this);
	}
	
	public void onLoad() {
		pm = ProtocolLibrary.getProtocolManager();
		pm.addPacketListener(new PacketAdapter(this, PacketType.Play.Client.CUSTOM_PAYLOAD) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				PacketContainer pack = event.getPacket();
				Player p = event.getPlayer();
				if (CommandPacketParser.isCommandBlockPacket(pack)) {
					event.setCancelled(true);
					if (p.hasPermission("SafeCommandBlock.access") || p.isOp()) {
						String Cmd = CommandPacketParser.getCmd(pack);
						if (!Cmd.startsWith("/") && !(Cmd == null || Cmd.equalsIgnoreCase(""))) {
							Cmd = "/" + Cmd; // set forslash if not preset, but
												// only if not empty and not the
												// usage of "say"
						}
						String RootCmd = SafeCommandUtils.getRootCmd(Cmd);
						if (p.hasPermission("SafeCommandBlock.color") || p.isOp()) {
							Cmd = SafeCommandUtils.replaceColorCode(Cmd);
						}
						if (p.hasPermission("SafeCommandBlock.bypass") || p.isOp()) {
							setCommandFromPlayer(p, Cmd, true);
							return;
						}
						if (ConfigSafeCommandBlock.hasWorldWhitelist && !ConfigSafeCommandBlock.FilterWorlds.contains(p.getWorld().getName())) {
							p.sendMessage(ChatColor.GOLD + p.getWorld().getName() + ChatColor.RED + " is not whitelisted! You cannot use command blocks in this world!");
							return;
						}
						if (Cmd == null || Cmd.equalsIgnoreCase("")) {
							ResetCommand(p);
							return;
						}
						if (RootCmd == null || RootCmd.equalsIgnoreCase("")) {
							p.sendMessage(ChatColor.RED + "You command was wrong formatted!");
							return;
						}
						if (ConfigSafeCommandBlock.isBlacklist) {
							if (ConfigSafeCommandBlock.Filter.contains(RootCmd)) {
								p.sendMessage(ChatColor.RED + "You cannot use command " + ChatColor.GOLD + RootCmd);
								return;
							}
						} else {
							if (!ConfigSafeCommandBlock.Filter.contains(RootCmd)) {
								p.sendMessage(ChatColor.RED + "You cannot use command " + ChatColor.GOLD + RootCmd);
								return;
							}
						}
						setCommandFromPlayer(p, Cmd, true);
					} else {
						p.sendMessage(ChatColor.RED + "You do not have permissions to access command blocks!");
					}
				}
			}
		});
	}

	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion() + " Has Been Enabled!");
		ConfigSafeCommandBlock.rl();
	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " Has Been Disabled!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (label.equalsIgnoreCase("resetcommand")) {
				if (p.hasPermission("SafeCommandBlock.resetCommand") || p.isOp()) {
					ResetCommand(p);
				} else {
					p.sendMessage(ChatColor.RED + "You do not have the permission to use this command!");
				}
			} else if (label.equalsIgnoreCase("setcommandname")) {
				if (args.length == 1) {
					if (p.hasPermission("SafeCommandBlock.setCommandName") || p.isOp()) {
						setNameFromPlayer(p, args[0], true);
					} else {
						p.sendMessage(ChatColor.RED + "You do not have the permission to use this command!");
					}
				} else if (args.length == 0) {
					if (p.hasPermission("SafeCommandBlock.setCommandName") || p.isOp()) {
						ResetName(p);
					} else {
						p.sendMessage(ChatColor.RED + "You do not have the permission to use this command!");
					}
				}
			} else if (label.equalsIgnoreCase("setcommand")) {
				if (args.length >= 1) {
					if (p.hasPermission("SafeCommandBlock.setCommand") || p.isOp()) {
						String cmd = "";
						if (args.length == 1) {
							cmd = args[0];
						} else {
							for (int i = 0; i < args.length; i++) {
								cmd = cmd + " " + args[i];
							}
							cmd = cmd.substring(1);
						}
						cmd = SafeCommandUtils.replaceColorCode(cmd);
						setCommandFromPlayer(p, cmd, true);
					} else {
						p.sendMessage(ChatColor.RED + "You do not have the permission to use this command!");
					}
				}
			} else if (label.equalsIgnoreCase("scbreload")) {
				if (p.hasPermission("SafeCommandBlock.reload") || p.isOp()) {
					this.reloadConfig();
					ConfigSafeCommandBlock.rl();
					p.sendMessage(ChatColor.GREEN + "Reloaded config & filter of Safe Command Block");
				}
			}
		} else {
			if (label.equalsIgnoreCase("scbreload")) {
				this.reloadConfig();
				ConfigSafeCommandBlock.rl();
				sender.sendMessage(ChatColor.GREEN + "Reloaded config & filter of Safe Command Block");
			} else {
				sender.sendMessage(ChatColor.RED + "You must be a Player to execute this command!");
			}
		}
		return false;
	}

	public void ResetCommand(Player p) {
		if (setCommandFromPlayer(p, "", false)) {
			p.sendMessage(ChatColor.GREEN + "Reset Command");
		}
	}

	public String BlockMustCmd = ChatColor.RED + "You need to look at the command block to set the command!";

	public boolean setCommandFromPlayer(Player p, String Command, boolean printNotify) {
		Block seeBlock = SafeCommandUtils.getTargetBlock(p, 200);
		if (seeBlock.getType() != Material.COMMAND) {
			p.sendMessage(BlockMustCmd);
			return false;
		}
		if (seeBlock.getState() instanceof CommandBlock) {
			CommandBlock ccBlockCmd = (CommandBlock) seeBlock.getState();
			ccBlockCmd.setCommand(Command);
			ccBlockCmd.update(true);
			if (printNotify) {
				p.sendMessage(ChatColor.GREEN + "Set Command: \"" + ChatColor.GOLD + Command + ChatColor.GREEN + "\"!");
			}
		} else {
			p.sendMessage(BlockMustCmd);
			return false;
		}
		return true;
	}

	public void ResetName(Player p) {
		if (setNameFromPlayer(p, null, false)) {
			p.sendMessage(ChatColor.GREEN + "Reset Name");
		}
	}

	public String BlockMustName = ChatColor.RED + "You need to look at the command block to set the command!";

	public boolean setNameFromPlayer(Player p, String Name, boolean printNotify) {
		Block seeBlock = SafeCommandUtils.getTargetBlock(p, 200);
		if (seeBlock.getType() != Material.COMMAND) {
			p.sendMessage(BlockMustName);
			return false;
		}
		if (seeBlock.getState() instanceof CommandBlock) {
			CommandBlock ccBlockCmd = (CommandBlock) seeBlock.getState();
			ccBlockCmd.setName(Name);
			ccBlockCmd.update(true);
			if (printNotify) {
				p.sendMessage(ChatColor.GREEN + "Set Name: \"" + ChatColor.GOLD + Name + ChatColor.GREEN + "\"!");
			}
		} else {
			p.sendMessage(BlockMustName);
			return false;
		}
		return true;
	}

	

	

}
