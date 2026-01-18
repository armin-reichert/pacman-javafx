/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.assets;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static de.amr.pacmanfx.uilib.Ufx.formatColorHex;

public abstract class PreferencesManager {

    private final Map<String, Object> defaultValueMap = new HashMap<>();
    private final Preferences prefs;

    public PreferencesManager(Class<?> packageProvidingClass) {
        prefs = Preferences.userNodeForPackage(packageProvidingClass);
        storeDefaultValues();
        if (!isBackingStoreAccessible()) {
            Logger.error("User preferences could not be accessed, using default values!");
        } else {
            final List<String> allKeys = defaultValueMap.keySet().stream().sorted().toList();
            // add missing values
            try {
                final Set<String> prefKeys = new HashSet<>(Arrays.asList(prefs.keys()));
                for (String key : allKeys) {
                    if (!prefKeys.contains(key)) {
                        final Object defaultValue = defaultValueMap.get(key);
                        store(key, defaultValue);
                        Logger.info("Added missing preference '{}'='{}'", key, defaultValue);
                    }
                }
            } catch (BackingStoreException x) {
                Logger.error("Could not access preferences store to add missing keys");
                Logger.error(x);
            }
        }
    }

    protected abstract void storeDefaultValues();

    public boolean isBackingStoreAccessible() {
        try {
            prefs.keys();
            Logger.info("Preferences backing store accessible");
            return true;
        } catch (BackingStoreException x) {
            return false;
        }
    }

    public void store(String key, Object defaultValue) {
        switch (defaultValue) {
            case Float floatValue -> prefs.putFloat(key, floatValue);
            case Integer intValue -> prefs.putInt(key, intValue);
            default               -> prefs.put(key, String.valueOf(defaultValue));
        }
        defaultValueMap.put(key, defaultValue);
    }

    public void storeDefault(String key, Object defaultValue) {
        defaultValueMap.put(key, defaultValue);
    }

    public void storeColor(String key, Color color) {
        final String colorSpec = formatColorHex(color);
        prefs.put(key, colorSpec);
        defaultValueMap.put(key, colorSpec);
    }

    public void storeDefaultColor(String key, Color color) {
        final String colorSpec = formatColorHex(color);
        defaultValueMap.put(key, colorSpec);
    }

    public Color getColor(String colorKey) {
        final String colorValue = prefs.get(colorKey, getDefaultValue(colorKey, String.class));
        return Color.valueOf(colorValue);
    }

    public void storeFont(String key, Font font) {
        final String familyKey = key + ".family";
        prefs.put(familyKey, font.getFamily());
        defaultValueMap.put(familyKey, font.getFamily());

        final String styleKey = key + ".style";
        prefs.put(styleKey, font.getStyle());
        defaultValueMap.put(styleKey, font.getStyle());

        final String sizeKey = key + ".size";
        prefs.putFloat(sizeKey, (float) font.getSize());
        defaultValueMap.put(sizeKey, (float) font.getSize());
    }

    public void storeDefaultFont(String key, Font font) {
        final String familyKey = key + ".family";
        defaultValueMap.put(familyKey, font.getFamily());

        final String styleKey = key + ".style";
        defaultValueMap.put(styleKey, font.getStyle());

        final String sizeKey = key + ".size";
        defaultValueMap.put(sizeKey, (float) font.getSize());
    }

    public Font getFont(String key) {
        final String familyKey = key + ".family";
        final String family = prefs.get(familyKey, getDefaultValue(familyKey, String.class));

        final String styleKey = key + ".style";
        final String style = prefs.get(styleKey, getDefaultValue(styleKey, String.class));

        final String sizeKey = key + ".size";
        final float size = prefs.getFloat(sizeKey, getDefaultValue(sizeKey, Float.class));

        return Font.font(family, FontWeight.findByName(style), size);
    }

    public <T> T getDefaultValue(String key, Class<T> type) {
        final Object defaultValue = defaultValueMap.get(key);
        if (type.isInstance(defaultValue)) {
            return type.cast(defaultValue);
        }
        throw new IllegalArgumentException("There is no default preference value of type %s for key '%s'"
                .formatted(type.getSimpleName(), defaultValue));
    }

    private Object getValue(String key) {
        final Object defaultValue = defaultValueMap.get(key);
        return switch (defaultValue) {
            case Float floatValue -> prefs.getFloat(key, floatValue);
            case Integer intValue -> prefs.getInt(key, intValue);
            default               -> prefs.get(key, String.valueOf(defaultValue));
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