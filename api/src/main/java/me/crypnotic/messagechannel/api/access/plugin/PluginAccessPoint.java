package me.crypnotic.messagechannel.api.access.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.var;
import me.crypnotic.messagechannel.api.MessageChannelAPI;
import me.crypnotic.messagechannel.api.pipeline.IPipeline;
import me.crypnotic.messagechannel.api.pipeline.PipelineMessage;
import me.crypnotic.messagechannel.api.util.JsonObjectBuilder;
import me.crypnotic.messagechannel.api.util.Pair;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.*;

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
			var jmessage = object.get("message").getAsJsonObject();
			var request = jmessage.get("request") == null ? "not-provided" : jmessage.get("request").getAsString();
			
			if (request.equals(":request_data:answer")) {
				Optional.ofNullable(point.awaitingData.get(jmessage.get("token").getAsInt())).ifPresent(consumer -> {
					consumer.accept(object.get("target").getAsString(), jmessage.get("answer"));
				});
				return;
			}
			
			point.consumers.stream().filter(pair -> pair.getKey() == null || pair.getKey().equals(request)).forEach(pair -> {
				pair.getValue().accept(object.get("target").getAsString(), object.get("message").getAsJsonObject());
			});
		});
	}
	
	String plugin;
	List<Pair<String, BiConsumer<String, JsonObject>>> consumers = new ArrayList<>();
	
	private final HashMap<Integer, BiConsumer<String, JsonElement>> awaitingData = new HashMap<>();
	
	public PluginAccessPoint(String plugin) {
		this.plugin = plugin;
		pointHashMap.add(this);
		
	}
	
	public void dataAnswerHandler(Predicate<JsonObject> predicate, Function<String, JsonElement> answer) {
		handler(":request_data", (sender, msg) -> {
			var token = msg.get("token").getAsInt();
			var data = msg.get("data").getAsJsonObject();
			if (predicate.test(data)) {
				sendRequest(sender, JsonObjectBuilder.get().request(":request_data:answer").add("token", token).add("answer", answer.apply(sender)).build());
			}
		});
	}
	
	public CompletableFuture<JsonElement> requestData(String sender, JsonObject data) {
		final int token = ThreadLocalRandom.current().nextInt();
		
		sendRequest(sender, JsonObjectBuilder.get().add("request", ":request_data").add("token", token).add("data", data).build());
		
		Pair<JsonElement, ?> object = Pair.of(null, null);
		
		awaitingData.put(token, (dataSender, json) -> {
			awaitingData.remove(token);
			if (sender.equalsIgnoreCase(dataSender)) {
				synchronized (object) {
					object.setKey(json);
					object.notifyAll();
				}
			}
		});
		
		return CompletableFuture.supplyAsync(() -> {
			try {
				synchronized(object) {
					object.wait();
					return object.getKey();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}, Executors.newSingleThreadExecutor());
	}
	
	/**
	 *
	 * @param request request we are waiting. Can be null.
	 * @param consumer String - target, JsonObject - message with request (with any if request is null)
	 */
	public void handler(String request, BiConsumer<String, JsonObject> consumer) {
		this.consumers.add(Pair.of(request, consumer));
	}
	
	public void unregisterHandler(String request) {
		this.consumers.stream().filter(pair -> pair.getKey().equals(request)).forEach(this.consumers::remove);
	}
	
	public void sendRequest(String target, JsonObject message) {
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
