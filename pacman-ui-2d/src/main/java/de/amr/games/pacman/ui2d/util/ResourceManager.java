/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.net.URL;
import java.util.MissingResourceException;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * <p>
 * Provides methods for loading assets using relative paths under the package of a specified root class. As there is
 * exactly one abstract method, an instance can be created using a lambda expression.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   src/main/resources/my/package/images/my_image.png
 *  </pre>
 * Then the source can be loaded as follows:
 * <pre>
 *   ResourceManager rm = () -> my.package.SomeClass.class;
 *   Image source = rm.source("images/my_image.png"); *
 * </pre>
 *
 * @author Armin Reichert
 */
@FunctionalInterface
public interface ResourceManager {

    /**
     * @return class relative to which resources are loaded if the path does not start with '/'
     */
    Class<?> rootClass();

    /**
     * Creates a URL from a resource path.
     *
     * @param path path of resource
     * @return URL of resource addressed by this path
     */
    default URL url(String path) {
        checkNotNull(path);
        URL url = rootClass().getResource(path);
        if (url == null) {
            throw new MissingResourceException(
                "Resource '%s' not found relative to class '%s'".formatted(path, rootClass().getName()),
                rootClass().getName(), path);
        }
        return url;
    }

    /**
     * @param path path to audio file
     * @return audio clip from resource addressed by this path
     */
    default AudioClip loadAudioClip(String path) {
        return new AudioClip(url(path).toExternalForm());
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
            return Font.font(Font.getDefault().getFamily(), size);
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
}