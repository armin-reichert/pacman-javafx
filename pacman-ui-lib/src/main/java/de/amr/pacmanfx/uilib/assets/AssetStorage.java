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
import java.util.Map;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

/**
 * Data structure for storing all kinds of game assets.
 */
public class AssetStorage {

    private final Map<String, Object> assetMap = new HashMap<>();
    private ResourceBundle localizedTexts;

    public void setLocalizedTexts(ResourceBundle bundle) {
        this.localizedTexts = bundle;
    }

    public ResourceBundle localizedTexts() {
        return localizedTexts;
    }

    public void set(String key, Object value) {
        requireNonNull(key);
        requireNonNull(value);
        assetMap.put(key, value);
    }

    public void removeAll() {
        assetMap.clear();
    }

    public void remove(String key) {
        requireNonNull(key);
        assetMap.remove(key);
    }

    public String translated(String keyOrPattern, Object... args) {
        requireNonNull(keyOrPattern);
        requireNonNull(args);
        if (localizedTexts == null) {
            Logger.error("No localized text resources available");
            return "???";
        }
        if (localizedTexts.containsKey(keyOrPattern)) {
            return MessageFormat.format(localizedTexts.getString(keyOrPattern), args);
        }
        Logger.error("Missing localized text for key {}", keyOrPattern);
        return "[" + keyOrPattern + "]";
    }

    /**
     * Generic getter for asset value. The value is cast to the expected type (font, image etc.).
     * <p>Example:
     * <p><code>Image image = assets.valueOfType("key_for_image", Image.class);</code>
     *
     * @param <T>  expected asset type
     * @param key asset key
     * @return stored value cast to expected type
     * @throws ClassCastException if specified type does not match asset value type
     */
    public <T> T asset(String key, Class<T> assetClass) {
        Object value = assetMap.get(key);
        if (value == null) {
            Logger.error("No asset value for key '{}' exists", key);
            return null;
        }
        if (assetClass.isInstance(value)) {
            return assetClass.cast(value);
        }
        throw new ClassCastException("Asset for key '%s' is not of type %s but %s".formatted(
                key, assetClass.getSimpleName(), value.getClass().getSimpleName()));
    }

    public Background background(String key) { return asset(key, Background.class); }

    public Color color(String key) { return asset(key, Color.class); }

    public Font font(String key) { return asset(key, Font.class); }

    public Font font(String key, double size) { return Font.font(font(key).getFamily(), size); }

    public Image image(String key) { return asset(key, Image.class); }
}