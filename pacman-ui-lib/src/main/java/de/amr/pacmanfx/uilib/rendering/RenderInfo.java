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
        return (Boolean) map.get(key);
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }
}
