/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.assets;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Data structure and API for game assets.
 */
public class AssetMap implements Disposable {

    private boolean frozen;
    private final Map<String, Object> assetsByID = new HashMap<>();

    @Override
    public void dispose() {
        assetsByID.clear();
        frozen = false;
    }

    public void addAsset(String key, Object asset) {
        requireNonNull(key);
        requireNonNull(asset);
        if (frozen) {
            throw new IllegalStateException("Cannot add asset with key '%s': asset map is frozen!".formatted(key));
        }
        assetsByID.put(key, asset);
    }

    /**
     * Prevents further modification of this assets map until the complete map is disposed.
     */
    public void freeze() {
        frozen = true;
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
        Object value = assetsByID.get(key);
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

    public Font font(String key, double size) { return Ufx.deriveFont(font(key), size); }

    public Image image(String key) { return asset(key, Image.class); }
}