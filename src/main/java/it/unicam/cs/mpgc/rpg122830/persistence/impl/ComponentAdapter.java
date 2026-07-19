package it.unicam.cs.mpgc.rpg122830.persistence.impl;

import com.google.gson.*;
import it.unicam.cs.mpgc.rpg122830.core.model.*;
import java.lang.reflect.Type;

public class ComponentAdapter implements JsonSerializer<Component>, JsonDeserializer<Component> {
    
    @Override
    public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", src.getName());
        jsonObject.addProperty("type", src.getType().name());
        jsonObject.addProperty("condition", src.getCondition());
        jsonObject.addProperty("performanceBonus", src.getPerformanceBonus());
        return jsonObject;
    }

    @Override
    public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        ComponentType type = ComponentType.valueOf(jsonObject.get("type").getAsString());
        double condition = jsonObject.get("condition").getAsDouble();
        int performanceBonus = jsonObject.get("performanceBonus").getAsInt();

        switch (type) {
            case ENGINE:
                return new Engine(name, condition, performanceBonus);
            case TURBOCHARGER:
                return new Turbocharger(name, condition, performanceBonus);
            case BRAKES:
                return new Brakes(name, condition, performanceBonus);
            case SUSPENSION:
                return new Suspension(name, condition, performanceBonus);
            default:
                throw new JsonParseException("Unknown component type: " + type);
        }
    }
}
