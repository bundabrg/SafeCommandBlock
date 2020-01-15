package me.KevinW1998.SafeCommandBlock;

import java.io.UnsupportedEncodingException;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import io.netty.buffer.ByteBuf;
import org.bukkit.Bukkit;

public class CommandPacketParser {
	public static int readVarInt(ByteBuf buf) {
        int varInt = 0;
        int counter = 0;

        byte nextByte;

        do {
            nextByte = buf.readByte();
            varInt |= (nextByte & 127) << counter++ * 7;
            if (counter > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((nextByte & 128) == 128);

        return varInt;
    }
	
	public static boolean isCommandBlockPacket(PacketContainer packet){
		if(packet.getType() == PacketType.Play.Client.CUSTOM_PAYLOAD){
			Bukkit.getLogger().warning(packet.toString());
			Bukkit.getLogger().warning(packet.getMinecraftKeys().read(0).getFullKey());
			return packet.getMinecraftKeys().read(0).getFullKey().equals("MC|AdvCmd");
		}
		return false;
	}
	
	public static String getCmd(PacketContainer packet) {
		if(!isCommandBlockPacket(packet))
			throw new IllegalArgumentException("Wrong type of packet, expected custom payload!");
		
		// http://wiki.vg/Plugin_channels#MC.7CAutoCmd
		ByteBuf buf = ((ByteBuf) packet.getModifier().withType(ByteBuf.class).read(0)).duplicate();
		buf.readInt(); // x
		buf.readInt(); // y
		buf.readInt(); // z
		int cmdLen = readVarInt(buf); // length of Command (VarInt)
		byte[] cmdBuf = new byte[cmdLen]; // Actual command
		buf.readBytes(cmdBuf);
		try {
			return new String(cmdBuf, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Failed to convert Command text to UTF-8!", e);
		}
	}
}
