package me.KevinW1998.SafeCommandBlock;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import net.minecraft.server.v1_8_R1.PacketDataSerializer;
import net.minecraft.server.v1_8_R1.PacketPlayInCustomPayload;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class CommandPacketParser {
	
	public static String getPayloadCommand(PacketPlayInCustomPayload customPayload){
		Class<? extends PacketPlayInCustomPayload> clazzCustomPayload = customPayload.getClass();
		Field[] fields = clazzCustomPayload.getDeclaredFields();
		for(Field f : fields){
			f.setAccessible(true);
			Object fieldObj;
			try {
				fieldObj = f.get(customPayload);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return "";
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return "";
			}
			if(fieldObj instanceof String){
				return (String)fieldObj;
			}
		}
		return "";
	}
	
	public static PacketDataSerializer getSerializer(PacketPlayInCustomPayload customPayload){
		Class<? extends PacketPlayInCustomPayload> clazzCustomPayload = customPayload.getClass();
		Field[] fields = clazzCustomPayload.getDeclaredFields();
		for(Field f : fields){
			f.setAccessible(true);
			Object fieldObj;
			try {
				fieldObj = f.get(customPayload);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return null;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
			if(fieldObj instanceof PacketDataSerializer){
				return (PacketDataSerializer)fieldObj;
			}
		}
		return null;
	}
	
	public static String extractCommandBlockCommand(PacketPlayInCustomPayload customPayload){
		PacketDataSerializer pds = getSerializer(customPayload);
		return pds.toString(14, pds.readableBytes()-15, StandardCharsets.UTF_8);
	}
	
	
	public static boolean isCommandBlockPacket(PacketContainer packet){
		if(packet.getType()==PacketType.Play.Client.CUSTOM_PAYLOAD){
			PacketPlayInCustomPayload plIn = (PacketPlayInCustomPayload)packet.getHandle();
			return getPayloadCommand(plIn).equals("MC|AdvCdm");
		}
		return false;
	}
	
	public static String getCmd(PacketContainer packet){
		return extractCommandBlockCommand((PacketPlayInCustomPayload)packet.getHandle());
	}
}
