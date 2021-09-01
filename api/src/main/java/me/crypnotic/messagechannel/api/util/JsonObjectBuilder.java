package me.crypnotic.messagechannel.api.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "get")
public class JsonObjectBuilder {
	JsonObject object = new JsonObject();
	
	public JsonObjectBuilder add(String property, JsonElement value) {
		object.add(property, value);
		return this;
	}
	
	public JsonObjectBuilder add(String property, String value) {
		object.addProperty(property, value);
		return this;
	}
	
	public JsonObjectBuilder add(String property, Number value) {
		object.addProperty(property, value);
		return this;
	}
	
	public JsonObjectBuilder add(String property, Boolean value) {
		object.addProperty(property, value);
		return this;
	}
	
	public JsonObjectBuilder add(String property, Character value) {
		object.addProperty(property, value);
		return this;
	}
	
	public JsonObjectBuilder request(String request) {
		add("request", ":request_data:answer");
		return this;
	}
	
	public JsonObject build() {
		return object;
	}
}
