package me.KevinW1998.SafeCommandBlock;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import me.KevinW1998.SafeCommandBlock.utils.CommonCommandBridge;

public class SafeCommandBlock extends JavaPlugin implements Listener {
	/*
	 * Permissions: SafeCommandBlock.access SafeCommandBlock.bypass
	 * 
	 * Features: - Whitelist or Blacklist Commands - World Whitelist (optional)
	 * - Commands can be set from players even if they not op and in creative
	 * mode - Set the name of a command block - Reset the command of a command
	 * block - With permission limitation
	 */

	public static final Logger MC_LOGGER = Logger.getLogger("Minecraft");
	public static SafeCommandBlock plugin;
	public ProtocolManager pm;
	public final ConfigManager ConfigSafeCommandBlock = new ConfigManager(this);
	public final SafeCommandBlockCommands commandExecuter = new SafeCommandBlockCommands(ConfigSafeCommandBlock);
	public final CommonCommandBridge commandBridge = new CommonCommandBridge(commandExecuter);
	
	public void onLoad() {
		pm = ProtocolLibrary.getProtocolManager();

		pm.addPacketListener(new PacketAdapter(this,
				PacketType.Play.Client.SET_COMMAND_BLOCK,
				PacketType.Play.Client.BLOCK_PLACE,
				PacketType.Play.Client.USE_ITEM,
				PacketType.Play.Server.ENTITY_STATUS
				) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				PacketContainer pack = event.getPacket();
				Player p = event.getPlayer();
				p.sendMessage("Packet:" + event.getPacketType());
				Bukkit.getLogger().warning("Packet:" + event.getPacketType() + " - " + pack);
//				event.setCancelled(true);
//				if (CommandPacketParser.isCommandBlockPacket(pack)) {
//					event.setCancelled(true);
//					if (p.hasPermission("SafeCommandBlock.access") || p.isOp()) {
//						String cmd = CommandPacketParser.getCmd(pack);
//						commandExecuter.setCommandSafe(p, cmd.split(" "));
//					} else {
//						p.sendMessage(ChatColor.RED + "You do not have permissions to access command blocks!");
//					}
//				}
			}

			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer pack = event.getPacket();
				Player p = event.getPlayer();

				p.sendMessage("Sent:" + event.getPacketType());
			}
		});
		pm.addPacketListener(new PacketAdapter(this, PacketType.Play.Server.OPEN_WINDOW) {
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer container = event.getPacket();
				if(container.getType() == PacketType.Play.Server.OPEN_WINDOW){
					System.out.println("Packet sending: " + container.toString());
				}
			}
			
			@Override
			public void onPacketReceiving(PacketEvent event) {
				PacketContainer container = event.getPacket();
				if(container.getType() == PacketType.Play.Server.OPEN_WINDOW){
					System.out.println("Packet receiving: " + container.toString());
				}
			}
		});
	}

	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		MC_LOGGER.info(pdfFile.getName() + " Version " + pdfFile.getVersion() + " Has Been Enabled!");
		ConfigSafeCommandBlock.rl();

		getServer().getPluginManager().registerEvents(this, this);


	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		MC_LOGGER.info(pdfFile.getName() + " Has Been Disabled!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		commandBridge.findAndExecuteCommand(sender, label, args);
		return false;
	}

	@EventHandler
	public void onCmd(EntityChangeBlockEvent e) {
		Block clickedBlock = e.getBlock();

		BlockState state = clickedBlock.getState();
		Bukkit.getLogger().warning("EntityChangeBlock: " + state.toString() + " - ");

	}

	@EventHandler
	public void onServerCmd(ServerCommandEvent e) {
		Bukkit.getLogger().warning("ServerCommand: " + e.toString());
		if (e.getSender() instanceof BlockCommandSender) {
			Bukkit.getLogger().warning("  - BlockCommandSender: " + e.getCommand());
			e.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onUseIem(PlayerInteractEvent e) {
		Bukkit.getLogger().warning("PlayerInteractEvent: " + e.toString() + " clicked:" +  e.getClickedBlock() + "item: " + e.getItem());

		if (e.getPlayer().isOp()) {
			return;
		}

		// Right clicked command block whilst not sneaking, open up editor
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.COMMAND_BLOCK && !e.getPlayer().isSneaking()) {
//			Location location = e.getClickedBlock().getLocation().add(e.getBlockFace().getDirection());
//			e.getPlayer().sendBlockChange(location, Material.COMMAND_BLOCK.createBlockData());
			Location location = e.getClickedBlock().getLocation();

			PacketContainer packet = new PacketContainer(PacketType.Play.Server.TILE_ENTITY_DATA);
			List<NbtBase<?>> tags = new ArrayList<>();
			tags.add(NbtFactory.of("id", "Control"));
			tags.add(NbtFactory.of("Command", "bum"));
			tags.add(NbtFactory.of("x", location.getBlockX()));
			tags.add(NbtFactory.of("y", location.getBlockY()));
			tags.add(NbtFactory.of("z", location.getBlockZ()));

			packet.getBlockPositionModifier().write(0, new BlockPosition(e.getClickedBlock().getLocation().toVector()));
			packet.getIntegers().write(0, 2);
			packet.getNbtModifier().write(0, NbtFactory.ofCompound("", tags));

			try {
				Bukkit.getLogger().warning("Sending packet:" + packet.toString());
				pm.sendServerPacket(e.getPlayer(), packet);
			} catch (InvocationTargetException ex) {
				ex.printStackTrace();
			}
			e.setCancelled(true);
			return;
		}


//		if (e.getItem() == null || e.getClickedBlock() == null || e.getItem().getType() != Material.COMMAND_BLOCK) {
//			return;
//		}
//
//		if (!e.getPlayer().isSneaking() && e.getClickedBlock().getType() == Material.COMMAND_BLOCK) {
//
//		}
//
//
//		Bukkit.getLogger().warning("Creating block at: " + e.getClickedBlock().getLocation().add(e.getBlockFace().getDirection()));
//		e.getClickedBlock().getLocation().add(e.getBlockFace().getDirection()).getBlock().setType(Material.COMMAND_BLOCK);

	}

	@EventHandler(priority= EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent e) {
		Bukkit.getLogger().warning("BlockPlaceEvent: " + e.toString());
	}






}
