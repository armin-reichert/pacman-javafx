/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static de.amr.pacmanfx.uilib.Ufx.formatColorHex;

public class PreferenceManager {

    private final Map<String, Object> defaultValueMap = new HashMap<>();
    private final Preferences prefs;

    public PreferenceManager(Class<?> _class) {
        prefs = Preferences.userNodeForPackage(_class);
    }

    public boolean isAccessible() {
        try {
            for (String key : prefs.keys()) {
                prefs.get(key, null);
                Logger.debug("Prefs key '{}' could be accessed");
            }
            return true;
        } catch (Exception x) {
            return false;
        }
    }

    public void addMissingValues() {
        try {
            Set<String> prefKeys = new HashSet<>(Arrays.asList(prefs.keys()));
            List<String> allKeys = defaultValueMap.keySet().stream().sorted().toList();
            for (String key : allKeys) {
                if (!prefKeys.contains(key)) {
                    Object defaultValue = defaultValueMap.get(key);
                    storeValue(key, defaultValue);
                    Logger.info("Added missing entry '{}'='{}'", key, defaultValue);
                }
            }
        } catch (BackingStoreException x) {
            Logger.error("Could not access preferences to add missing keys");
            Logger.error(x);
        }
    }

    public void storeValue(String key, Object defaultValue) {
        switch (defaultValue) {
            case Float floatValue -> prefs.putFloat(key, floatValue);
            case Integer intValue -> prefs.putInt(key, intValue);
            default -> prefs.put(key, String.valueOf(defaultValue));
        }
        defaultValueMap.put(key, defaultValue);
    }

    public void storeDefaultValue(String key, Object defaultValue) {
        defaultValueMap.put(key, defaultValue);
    }

    public void storeColor(String key, Color color) {
        String colorSpec = formatColorHex(color);
        prefs.put(key, colorSpec);
        defaultValueMap.put(key, colorSpec);
    }

    public void storeDefaultColor(String key, Color color) {
        String colorSpec = formatColorHex(color);
        defaultValueMap.put(key, colorSpec);
    }

    public Color getColor(String colorKey) {
        String colorValue = prefs.get(colorKey, getDefaultValue(colorKey));
        return Color.web(colorValue);
    }

    public void storeFont(String key, Font font) {
        String familyKey = key + ".family";
        prefs.put(familyKey, font.getFamily());
        defaultValueMap.put(familyKey, font.getFamily());

        String styleKey = key + ".style";
        prefs.put(styleKey, font.getStyle());
        defaultValueMap.put(styleKey, font.getStyle());

        String sizeKey = key + ".size";
        prefs.putFloat(sizeKey, (float) font.getSize());
        defaultValueMap.put(sizeKey, (float) font.getSize());
    }

    public void storeDefaultFont(String key, Font font) {
        String familyKey = key + ".family";
        defaultValueMap.put(familyKey, font.getFamily());

        String styleKey = key + ".style";
        defaultValueMap.put(styleKey, font.getStyle());

        String sizeKey = key + ".size";
        defaultValueMap.put(sizeKey, (float) font.getSize());
    }

    public Font getFont(String key) {
        String familyKey = key + ".family";
        String family = prefs.get(familyKey, getDefaultValue(familyKey));

        String styleKey = key + ".style";
        String style = prefs.get(styleKey, getDefaultValue(styleKey));

        String sizeKey = key + ".size";
        Object defaultSize = getDefaultValue(sizeKey);
        float size = prefs.getFloat(sizeKey, defaultSize instanceof Float ? ((Float) defaultSize) : 8);

        return Font.font(family, FontWeight.findByName(style), size);
    }

    @SuppressWarnings("unchecked")
    public <T> T getDefaultValue(String key) {
        return (T) defaultValueMap.get(key);
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
}