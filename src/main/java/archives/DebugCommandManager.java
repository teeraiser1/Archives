package main.java.archives;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class DebugCommandManager {
	
	private static Map<String, Command> commands;

	public DebugCommandManager(List<Class<?>> controllers) {
		commands = new HashMap<>();
		
		for(Class<?> controller : controllers)
			registerDebugCommands(controller);
	}

	public void registerDebugCommands(Class<?> controllerClass) {
		for (Method method : controllerClass.getDeclaredMethods()) {
			DebugCommandHandler annotation = method.getAnnotation(DebugCommandHandler.class);
			
			if (annotation != null)
				addDebugCommand(controllerClass, method, annotation);
		}
	}
	
	private void addDebugCommand(Class<?> controllerClass, Method method, DebugCommandHandler annotation) {
		Parameter[] params = method.getParameters();
		List<Class<?>> parameters = new ArrayList<>();
		
		for (Parameter param : params) {
			parameters.add(param.getType());
		}
		Command command = new Command(method.getName(), parameters, controllerClass, method);
		
		commands.put(method.getName(), command);
	}
	
	public void dispatchDebugCommand(String prefix, Message message, MessageChannel channel, Guild guild, Member member) {
		String content = message.getContentDisplay().trim();
	    String[] args = content.split("\\s+");

	    if (!args[0].equals(prefix)) {
	      return;
	    }
	    
	    String commandName = args[1];
	    Command command = commands.get(commandName);

	    if (command == null)
	    	return ;
	    
	    Object[] parameters = new Object[command.parameters.size()];
	    
	    for (int i = 0; i < parameters.length; i++) {
	    	if (args[i + 2].equals("Message")) {
	    		parameters[i] = message;
	    	}
	    	else if (args[i + 2].equals("MessageChannel")) {
	    		parameters[i] = channel;
	    	}
	    	else if (args[i + 2].equals("Guild")) {
	    		parameters[i] = guild;
	    	}
	    	else if (args[i + 2].equals("GuildId")) {
	    		parameters[i] = guild.getId();
	    	}
	    	else if (args[i + 2].equals("UserId")) {
	    		parameters[i] = member.getIdLong();
	    	}
	    	else {
	    		parameters[i] = null;
	    	}
	    }
	  
	    
	    try {
			command.method.invoke(command.controllerClass, parameters);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	private class Command {
		String name;
		List<Class<?>> parameters;
		Class<?> controllerClass;
		Method method;
		
		public Command(String name, List<Class<?>> params, Class<?> controllerClass, Method method) {
			this.name = name;
			this.parameters = params;
			this.controllerClass = controllerClass;
			this.method = method;
		}
	}
}
