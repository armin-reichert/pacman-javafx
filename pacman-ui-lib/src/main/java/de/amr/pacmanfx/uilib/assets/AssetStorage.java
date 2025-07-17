/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.assets;

import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

/**
 * Data structure for storing all kinds of game assets.
 */
public class AssetStorage {

    private final Map<String, Object> assetMap = new HashMap<>();
    protected ResourceBundle localizedTextBundle;

    public void store(String key, Object value) {
        requireNonNull(key);
        requireNonNull(value);
        assetMap.put(key, value);
    }

    public void removeAll(String keyPrefix) {
        requireNonNull(keyPrefix);
        assetMap.keySet().removeIf(key -> key.startsWith(keyPrefix));
    }

    public void remove(String key) {
        requireNonNull(key);
        assetMap.remove(key);
    }

    public String text(String keyOrPattern, Object... args) {
        requireNonNull(keyOrPattern);
        requireNonNull(args);
        if (localizedTextBundle == null) {
            Logger.error("No localized text resources available");
            return "???";
        }
        if (localizedTextBundle.containsKey(keyOrPattern)) {
            return MessageFormat.format(localizedTextBundle.getString(keyOrPattern), args);
        }
        Logger.error("Missing localized text for key {}", keyOrPattern);
        return "[" + keyOrPattern + "]";
    }

    /**
     * Generic getter for asset value. The value is cast (without check) to the expected type (font, image etc.).
     * <p>Usage:
     * <p><code>Image image = assets.asset("key_for_image");</code>
     *
     * @param <T>  expected asset type
     * @param key asset key
     * @return stored value cast to expected type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        T value = (T) assetMap.get(key);
        if (value == null) {
            Logger.error("Asset not found, key={}", key);
        }
        return value;
    }

    public Color color(String key) { return get(key); }

    public Font font(String key) { return get(key); }

    public Font font(String key, double size) { return Font.font(font(key).getFamily(), size); }

    public Image image(String key) { return get(key); }

    public Background background(String key) { return get(key); }
}