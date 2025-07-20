package de.amr.pacmanfx.ui;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import static de.amr.pacmanfx.uilib.Ufx.formatColorHex;

public class PreferenceManager {

    private final Map<String, Object> defaultValueMap = new HashMap<>();
    private final Preferences prefs;

    public PreferenceManager(Class<?> _class) {
        prefs = Preferences.userNodeForPackage(_class);
    }

    private Object getValue(String key) {
        Object defaultValue = defaultValueMap.get(key);
        return switch (defaultValue) {
            case Float floatValue -> prefs.getFloat(key, floatValue);
            case Integer intValue -> prefs.getInt(key, intValue);
            default -> prefs.get(key, String.valueOf(defaultValue));
        };
    }

    public float getFloat(String key) {
        return (float) getValue(key);
    }

    public int getInt(String key) {
        return (int) getValue(key);
    }

    public String getString(String key) {
        return (String) getValue(key);
    }

    public void storeColor(String key, Color color) {
        prefs.put(key, formatColorHex(color));
    }

    public Color getColor(String colorKey) {
        String colorValue = prefs.get(colorKey, getDefaultValue(colorKey));
        return Color.web(colorValue);
    }

    @SuppressWarnings("unchecked")
    public <T> T getDefaultValue(String key) {
        return (T) defaultValueMap.get(key);
    }

    public void store(String key, Object defaultValue) {
        switch (defaultValue) {
            case Float floatValue -> prefs.putFloat(key, floatValue);
            case Integer intValue -> prefs.putInt(key, intValue);
            default -> prefs.put(key, String.valueOf(defaultValue));
        }
        defaultValueMap.put(key, defaultValue);
    }

    public void storeFont(String key, Font font) {
        defaultValueMap.put(key + ".family", font.getFamily());
        defaultValueMap.put(key + ".style", font.getStyle());
        defaultValueMap.put(key + ".size", (float) font.getSize());
    }

    public Font getFont(String key) {
        String family = (String) defaultValueMap.get(key + ".family");
        String style = (String) defaultValueMap.get(key + ".style");
        float size = (float) defaultValueMap.get(key + ".size");
        return Font.font(family, FontWeight.findByName(style), size);
    }
}
