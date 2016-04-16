package me.KevinW1998.SafeCommandBlock.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class CommonCommandBridge {
	private Class<?> executer;
	private Object executerInstance;
	private Collection<Method> commonCommandsMethods = new ArrayList<Method>();
	
	public CommonCommandBridge(Object executerInstance){
		this.executer = executerInstance.getClass();
		this.executerInstance = executerInstance;
		
		for(Method possibleCommonCommand : executer.getMethods())
		{
			 if(possibleCommonCommand.isAnnotationPresent(CommonCommand.class)){
				 CommonCommand commonCommand = possibleCommonCommand.getAnnotation(CommonCommand.class);
				 if(!commonCommand.canExecuteConsole() && !commonCommand.canExecutePlayer())
					 throw new IllegalArgumentException(possibleCommonCommand.getName() + " has an invalid annotations. Both canExecuteConsole and canExecutePlayer are false!");
				 commonCommandsMethods.add(possibleCommonCommand);		 
			 }
		}
		
	}
	
	public void findAndExecuteCommand(CommandSender sender, String rootCommand, String[] args)
	{
		for(Method nextMethod : commonCommandsMethods)
		{
			CommonCommand nextCommonCommand = nextMethod.getAnnotation(CommonCommand.class);
			if(nextCommonCommand == null)
				throw new NullPointerException("Command has no CommonCommand annotation!");
			if(nextCommonCommand.commandName().equalsIgnoreCase(rootCommand))
			{
				if(sender instanceof Player){
					if(!nextCommonCommand.canExecutePlayer())
					{
						sender.sendMessage(ChatColor.RED + "This command can only be executed from the console!");
						return;
					}
					if(!SafeCommandUtils.isOpOrHasPermission((Player)sender, nextCommonCommand.permissionRequired())) {
						sender.sendMessage(ChatColor.RED + "You do not have the permission to use this command!");
						return;
					}
				} else {
					if(!nextCommonCommand.canExecuteConsole())
					{
						sender.sendMessage(ChatColor.RED + "You must be a Player to execute this command!");
						return;
					}
				}
				if(args.length < nextCommonCommand.minArgsCount()){
					sender.sendMessage(ChatColor.RED + "Expected " + nextCommonCommand.minArgsCount() + " Arguments, got " + args.length);
					return;
				}
				
				try {
					nextMethod.invoke(executerInstance, sender, args);
				} catch (Exception e) {
					throw new IllegalStateException("Failed to execute common command: " + nextMethod.getName(), e);
				}
			}
		}
	}
	
}
