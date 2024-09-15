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
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class AssetMap {

    private final List<ResourceBundle> bundles = new ArrayList<>();
    private final Map<String, Object> map = new HashMap<>();

    public void addBundle(ResourceBundle bundle) {
        bundles.add(bundle);
    }

    public List<ResourceBundle> bundles() {
        return bundles;
    }

    public void set(String key, Object value) {
        map.put(key, value);
    }

    /**
     * Generic getter. Example usage:
     *
     * <pre>
     * AnyType value = assets.get("key.for.value");
     * </pre>
     *
     * @param <T>  expected return type
     * @param key resourceKey of value
     * @return stored value cast to return type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, int i, int j) {
        return (T) map.get(String.format("%s.%d.%d", key, i, j));
    }

    public Color color(String listKey, int i) {
        List<Object> list = get(listKey);
        return (Color) list.get(i);
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

    public long countEntriesOfType(Class<?> clazz) {
        long count = map.values().stream().filter(val -> val.getClass().isAssignableFrom(clazz)).count();
        for (var value : map.values()) {
            if (value instanceof List<?> list) {
                count += list.stream().filter(val -> val.getClass().isAssignableFrom(clazz)).count();
            }
        }
        return count;
    }

    public String summary(Map<Class<?>, String> assetTypesByClass) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Class<?> assetClass : assetTypesByClass.keySet()) {
            long count = countEntriesOfType(assetClass);
            sb.append(assetTypesByClass.get(assetClass)).append(" (").append(count).append(")");
            if (i < assetTypesByClass.size() - 1) {
                sb.append(", ");
            }
            i += 1;
        }
        return sb.toString();
    }
}