/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib;

import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.Globals.assertNotNull;

/**
 * @author Armin Reichert
 */
public class AssetStorage {

    private final List<ResourceBundle> bundles = new ArrayList<>();
    private final Map<String, Object> map = new HashMap<>();

    public void addBundle(ResourceBundle bundle) {
        bundles.add(bundle);
    }

    public List<ResourceBundle> bundles() {
        return bundles;
    }

    public void store(String key, Object value) {
        map.put(key, value);
    }

    public void addAll(AssetStorage other) {
        map.putAll(other.map);
        bundles.addAll(other.bundles);
    }

    public String text(String keyOrPattern, Object... args) {
        assertNotNull(keyOrPattern);
        for (ResourceBundle bundle : bundles) {
            if (bundle.containsKey(keyOrPattern)) {
                return MessageFormat.format(bundle.getString(keyOrPattern), args);
            }
        }
        Logger.error("Missing localized text for key {}", keyOrPattern);
        return "[" + keyOrPattern + "]";
    }

    /**
     * Generic getter. Example usage:
     *
     * @param <T>  expected asset type
     * @param key asset key
     * @return stored asset cast to expected type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        T value = (T) map.get(key);
        if (value == null) {
            Logger.error("Asset not found, key={}", key);
        }
        return value;
    }

    public Color color(String key) {
        return get(key);
    }

    public Font font(String key) {
        return get(key);
    }

    public Font font(String key, double size) {
        return Font.font(font(key).getFamily(), size);
    }

    public Image image(String key) {
        return get(key);
    }

    public Background background(String key) {
        return get(key);
    }

    public Stream<AudioClip> audioClips() {
        return map.values().stream().filter(AudioClip.class::isInstance).map(AudioClip.class::cast);
    }

    public long countAssetsOfClass(Class<?> assetClass) {
        long count = map.values().stream()
            .filter(asset -> asset.getClass().isAssignableFrom(assetClass)).count();
        for (var mapValue : map.values()) {
            if (mapValue instanceof List<?> assetList) {
                count += assetList.stream()
                    .filter(asset -> asset.getClass().isAssignableFrom(assetClass)).count();
            }
        }
        return count;
    }

    public String summary(Map<Class<?>, String> assetTypesByClass) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Class<?> assetClass : assetTypesByClass.keySet()) {
            long count = countAssetsOfClass(assetClass);
            sb.append(assetTypesByClass.get(assetClass)).append(" (").append(count).append(")");
            if (i < assetTypesByClass.size() - 1) {
                sb.append(", ");
            }
            i += 1;
        }
        return sb.toString();
    }
}