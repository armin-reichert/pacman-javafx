/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.*;
import java.util.stream.Stream;

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

    /**
     * Generic getter. Example usage:
     *
     * @param <T>  expected asset type
     * @param key asset key
     * @return stored asset cast to expected type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) map.get(key);
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