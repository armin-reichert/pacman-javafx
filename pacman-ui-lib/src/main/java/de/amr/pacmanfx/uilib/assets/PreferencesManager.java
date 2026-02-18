/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.assets;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static de.amr.pacmanfx.uilib.UfxColors.formatColorHex;

public abstract class PreferencesManager {

    private final Map<String, Object> defaultsMap = new HashMap<>();
    private final Preferences userPrefs;

    public PreferencesManager(Class<?> packageMember) {
        userPrefs = Preferences.userNodeForPackage(packageMember);
        if (!isBackingStoreAccessible()) {
            Logger.error("User preferences could not be accessed, using default values!");
        } else {
            Logger.info("Preferences backing store is accessible");
            storeDefaultValues();
            // Add missing values
            final List<String> keys = defaultsMap.keySet().stream().sorted().toList();
            try {
                final Set<String> definedKeys = new HashSet<>(Arrays.asList(userPrefs.keys()));
                for (String key : keys) {
                    if (!definedKeys.contains(key)) {
                        final Object defaultValue = defaultsMap.get(key);
                        storeEntry(key, defaultValue);
                        Logger.info("Added missing preference '{}'='{}'", key, defaultValue);
                    }
                }
            } catch (BackingStoreException x) {
                Logger.error(x, "Adding missing preference keys failed: Could not access preferences store");
            }
        }
    }

    protected abstract void storeDefaultValues();

    public boolean isBackingStoreAccessible() {
        try {
            userPrefs.keys();
            return true;
        } catch (BackingStoreException x) {
            return false;
        }
    }

    public void logPreferences() {
        try {
            Arrays.stream(userPrefs.keys()).sorted().forEach(key -> {
                final Object value = getValue(key);
                Logger.info("{} => {}", key, value);
            });
        } catch (BackingStoreException e) {
				    Logger.error("Could not log preferences: backing store not accessible");
        }
		}

    public void storeEntry(String key, Object defaultValue) {
        switch (defaultValue) {
            case Float floatValue -> userPrefs.putFloat(key, floatValue);
            case Integer intValue -> userPrefs.putInt(key, intValue);
            default               -> userPrefs.put(key, String.valueOf(defaultValue));
        }
        defaultsMap.put(key, defaultValue);
    }

    public void storeDefaultEntry(String key, Object defaultValue) {
        defaultsMap.put(key, defaultValue);
    }

    public void storeDefaultColor(String key, Color color) {
        final String colorSpec = formatColorHex(color);
        defaultsMap.put(key, colorSpec);
    }

    public void storeDefaultFont(String key, Font font) {
        final String familyKey = key + ".family";
        defaultsMap.put(familyKey, font.getFamily());

        final String styleKey = key + ".style";
        defaultsMap.put(styleKey, font.getStyle());

        final String sizeKey = key + ".size";
        defaultsMap.put(sizeKey, (float) font.getSize());
    }

    public void storeColor(String key, Color color) {
        final String colorSpec = formatColorHex(color);
        userPrefs.put(key, colorSpec);
        defaultsMap.put(key, colorSpec);
    }

    public Color getColor(String colorKey) {
        final String colorValue = userPrefs.get(colorKey, getDefaultValue(colorKey, String.class));
        return Color.valueOf(colorValue);
    }

    public void storeFont(String key, Font font) {
        final String familyKey = key + ".family";
        userPrefs.put(familyKey, font.getFamily());
        defaultsMap.put(familyKey, font.getFamily());

        final String styleKey = key + ".style";
        userPrefs.put(styleKey, font.getStyle());
        defaultsMap.put(styleKey, font.getStyle());

        final String sizeKey = key + ".size";
        userPrefs.putFloat(sizeKey, (float) font.getSize());
        defaultsMap.put(sizeKey, (float) font.getSize());
    }

    public Font getFont(String key) {
        final String familyKey = key + ".family";
        final String family = userPrefs.get(familyKey, getDefaultValue(familyKey, String.class));

        final String styleKey = key + ".style";
        final String style = userPrefs.get(styleKey, getDefaultValue(styleKey, String.class));

        final String sizeKey = key + ".size";
        final float size = userPrefs.getFloat(sizeKey, getDefaultValue(sizeKey, Float.class));

        return Font.font(family, FontWeight.findByName(style), size);
    }

    public <T> T getDefaultValue(String key, Class<T> type) {
        final Object defaultValue = defaultsMap.get(key);
        if (type.isInstance(defaultValue)) {
            return type.cast(defaultValue);
        }
        throw new IllegalArgumentException("There is no default preference value of type %s for key '%s'"
                .formatted(type.getSimpleName(), defaultValue));
    }

    private Object getValue(String key) {
        final Object defaultValue = defaultsMap.get(key);
        return switch (defaultValue) {
            case null -> "<undefined>";
            case Float floatValue -> userPrefs.getFloat(key, floatValue);
            case Integer intValue -> userPrefs.getInt(key, intValue);
            default               -> userPrefs.get(key, String.valueOf(defaultValue));
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