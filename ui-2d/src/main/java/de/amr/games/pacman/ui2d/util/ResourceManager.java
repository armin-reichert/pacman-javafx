/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.scene.image.Image;
import javafx.scene.layout.*;
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
 * Then the image can be loaded as follows:
 * <pre>
 *   ResourceManager rm = () -> my.package.SomeClass.class;
 *   Image image = rm.image("images/my_image.png"); *
 * </pre>
 *
 * @author Armin Reichert
 */
@FunctionalInterface
public interface ResourceManager {

    /**
     * @return the class relative to which resources are loaded if the path does not start with '/'
     */
    Class<?> getResourceRootClass();

    /**
     * Creates a URL from a resource path.
     *
     * @param path path of resource
     * @return URL of resource addressed by this path
     */
    default URL url(String path) {
        checkNotNull(path);
        URL url = getResourceRootClass().getResource(path);
        if (url == null) {
            throw new MissingResourceException(
                String.format("Resource '%s' not found relative to class '%s'", path, getClass()),
                getClass().getName(), path);
        }
        return url;
    }

    /**
     * @param path path to resource
     * @return audio clip from resource addressed by this path
     */
    default AudioClip loadAudioClip(String path) {
        var url = url(path);
        return new AudioClip(url.toExternalForm());
    }

    /**
     * @param path path to resource
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
            Logger.error("Font with URL '{}' could not be loaded", url);
            return Font.font(Font.getDefault().getFamily(), size);
        }
        return font;
    }

    /**
     * @param path path to image. If path has no leading '/' the resource managers root class
     * @return image loaded from resource addressed by this path.
     */
    default Image loadImage(String path) {
        var url = url(path);
        return new Image(url.toExternalForm());
    }

    /**
     * @param path path to resource
     * @return image background with default properties, see {@link BackgroundImage}
     */
    default Background imageBackground(String path) {
        var image = loadImage(path);
        return new Background(new BackgroundImage(image, null, null, null, null));
    }

    /**
     * @param path path to resource
     * @return image background with specified attributes
     */
    default Background imageBackground(String path, BackgroundRepeat repeatX, BackgroundRepeat repeatY,
                                       BackgroundPosition position, BackgroundSize size) {
        var image = loadImage(path);
        return new Background(new BackgroundImage(image, repeatX, repeatY, position, size));
    }
}