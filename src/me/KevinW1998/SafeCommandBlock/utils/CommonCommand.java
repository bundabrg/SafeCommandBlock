package me.KevinW1998.SafeCommandBlock.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommonCommand {
	/**
	 * The command name.
	 */
	String commandName();
	/**
	 * The permission required to execute
	 */
	String permissionRequired();
	/**
	 * The required number of args as a minimum.
	 */
	int minArgsCount() default 0;
	
	boolean canExecutePlayer() default true;
	boolean canExecuteConsole() default false;
}
