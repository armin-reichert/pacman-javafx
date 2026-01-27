/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.assets;

import de.amr.pacmanfx.uilib.Ufx;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

/**
 * <p>
 * Provides methods for loading assets using relative paths under the package of a specified root class. As there is
 * exactly one abstract method, an instance can be created using a lambda expression.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   src/main/resources/de/amr/images/photo-of-my-cat.png
 *  </pre>
 * Then the image can be loaded as follows:
 * <pre><code>
 *   ResourceManager rm = () -> de.amr.AnyClassInThisPackage.class;
 *   Image photo = rm.loadImage("images/photo-of-my-cat.png");
 * </code></pre>
 */
@FunctionalInterface
public interface ResourceManager {
    /**
     * @return class relative to which resources are loaded if the path does not start with '/'
     */
    Class<?> resourceRootClass();

    /**
     * Creates a URL from a resource path.
     *
     * @param path path of resource
     * @return URL of resource addressed by this path
     */
    default URL url(String path) {
        requireNonNull(path);
        URL url = resourceRootClass().getResource(path);
        if (url == null) {
            throw new MissingResourceException(
                "Resource '%s' not found relative to class '%s'".formatted(path, resourceRootClass().getName()),
                resourceRootClass().getName(), path);
        }
        return url;
    }

    default ResourceBundle getModuleBundle(String baseName) {
        return ResourceBundle.getBundle(baseName, resourceRootClass().getModule());
    }

    /**
     * @param path path to audio file
     * @return audio clip from resource addressed by this path
     */
    default AudioClip loadAudioClip(String path) {
        return new AudioClip(url(path).toExternalForm());
    }

    default Media loadMedia(String path) {
        return new Media(url(path).toExternalForm());
    }

    /**
     * @param path path to font file
     * @param size    font size (must be a positive number)
     * @return font loaded from resource addressed by this path. If no such font can be loaded, a default font is returned
     */
    default Font loadFont(String path, double size) {
        if (size <= 0) {
            throw new IllegalArgumentException(String.format("Font size must be positive but is %.2f", size));
        }
        var url = url(path);
        var font = Font.loadFont(url.toExternalForm(), size);
        if (font == null) {
            Logger.error("Font could not be loaded from URL '{}' ", url);
            return Ufx.deriveFont(Font.getDefault(), size);
        }
        return font;
    }

    /**
     * @param path path to source file.
     * @return source loaded from resource addressed by this path.
     */
    default Image loadImage(String path) {
        return new Image(url(path).toExternalForm());
    }

    default Cursor cursor(String path) {
        return Cursor.cursor(url(path).toExternalForm());
    }
}