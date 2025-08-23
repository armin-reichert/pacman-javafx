package de.amr.pacmanfx.uilib.rendering;

import java.util.HashMap;
import java.util.Map;

public class RenderInfo {

    private final Map<String, Object> map = new HashMap<>();

    public static RenderInfo build(Map<String, Object> map) {
        var info = new RenderInfo();
        info.map.putAll(map);
        return info;
    }

    public boolean getBoolean(String key) {
        return get(key, Boolean.class);
    }

    public <T> T get(String key, Class<T> valueClass) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (valueClass.isInstance(value)) {
            return valueClass.cast(value);
        }
        throw new IllegalArgumentException("Key '%s' is not assigned to value of class '%s'"
                .formatted(key, valueClass.getSimpleName()));
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }
}
