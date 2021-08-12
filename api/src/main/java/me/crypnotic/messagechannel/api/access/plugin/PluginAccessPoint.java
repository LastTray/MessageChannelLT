package me.crypnotic.messagechannel.api.access.plugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.crypnotic.messagechannel.api.MessageChannelAPI;
import me.crypnotic.messagechannel.api.pipeline.IPipeline;
import me.crypnotic.messagechannel.api.pipeline.PipelineMessage;
import me.crypnotic.messagechannel.api.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class PluginAccessPoint {
	private final static List<PluginAccessPoint> pointHashMap = new ArrayList<>();
	private final static IPipeline pipeline = MessageChannelAPI.getPipelineRegistry().registerAsync("built_in_service");
	
	/*
	Я блять хуй знает. Я уже и так пробовал, и сяк, и со стула вставал, и по комнате ходил, и с умным видом сидел, даже в гляделки с кодом играть пытался.
	Вот не работает по-другому и делай что хочешь. А на бубны ты цены видел? Посмотри. Было бы у меня столько, я бы ещё и станцевал, поверь.
	Иными словами, этот код лучше ВООБЩЕ НЕ ТРОГАТЬ. Тут если дышать очень громко всё полетит в пизду-ху-ху.
	 */
	static {
		pipeline.onReceive(message -> {
			var read = message.read(String.class);
			var object = new JsonParser().parse(read).getAsJsonObject();
			var point = ofPlugin(object.get("target_plugin").getAsString());
			System.out.println(point);
			var request = object.get("message").getAsJsonObject().get("request") == null ? "not-provided" : object.get("message").getAsJsonObject().get("request").getAsString();
			point.consumers.stream().filter(pair -> pair.getKey() == null || pair.getKey().equals(request)).forEach(pair -> pair.getValue().accept(object.get("target").getAsString(), object.get("message").getAsJsonObject()));
		});
	}
	
	String plugin;
	List<Pair<String, BiConsumer<String, JsonObject>>> consumers = new ArrayList<>();
	
	public PluginAccessPoint(String plugin) {
		this.plugin = plugin;
		pointHashMap.add(this);
	}
	
	/**
	 *
	 * @param request request we are waiting. Can be null.
	 * @param consumer String - target, JsonObject - message with request (with any if request is null)
	 */
	public void handler(String request, BiConsumer<String, JsonObject> consumer) {
		this.consumers.add(Pair.of(request, consumer));
	}
	
	public void sendRequest(String target, JsonObject message) {
		var pipeline = MessageChannelAPI.getPipelineRegistry().getRegisteredPipeline("built_in_service");
		
		var json = new JsonObject();
		json.addProperty("target", target);
		json.addProperty("target_plugin", plugin);
		json.add("message", message);
		
		{
			PipelineMessage pipelineMessage = new PipelineMessage(target);
			pipelineMessage.write(json.toString());
			pipeline.send(pipelineMessage);
		}
	}
	
	public static PluginAccessPoint ofPlugin(String plugin) {
		return pointHashMap.stream().filter(point -> point.plugin.equalsIgnoreCase(plugin)).findAny().orElse(new PluginAccessPoint(plugin));
	}
	
}
