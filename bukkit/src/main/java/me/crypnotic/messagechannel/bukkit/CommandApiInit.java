package me.crypnotic.messagechannel.bukkit;

import com.google.gson.JsonParser;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import lombok.var;
import me.crypnotic.messagechannel.api.MessageChannelAPI;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CommandApiInit {
	
	public CommandApiInit() {
		new CommandAPICommand("messagechannel")
			.withArguments(
				new EntitySelectorArgument("target", EntitySelectorArgument.EntitySelector.ONE_PLAYER),
				new StringArgument("target plugin"),
				new GreedyStringArgument("json message")
			)
			.executesPlayer((sender, args) -> {
				try {
					var target = (Player) args[0];
					var targetPlugin = (String) args[1];
					var jsonMessage = new JsonParser().parse((String) args[2]).getAsJsonObject();
					
					MessageChannelAPI.getPipelineRegistry().getPluginAccessPoint(targetPlugin).sendRequest(target.getName(), jsonMessage);
				} catch (Exception e) {
					Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).forEach(sender::sendMessage);
				}
			})
			.register();
	}
}
