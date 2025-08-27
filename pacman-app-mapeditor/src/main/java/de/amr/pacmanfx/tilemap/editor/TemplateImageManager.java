package de.amr.pacmanfx.tilemap.editor;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.FILTER_ALL_FILES;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.FILTER_IMAGE_FILES;

public class TemplateImageManager {

    public static Image readImageFromFile(File file) {
        try (FileInputStream stream = new FileInputStream(file)) {
            return new Image(stream);
        } catch (IOException x) {
            Logger.error(x);
            return null;
        }
    }

    public static Optional<Image> selectTemplateImage(Window window, String title, File currentDirectory) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialDirectory(currentDirectory);
        fileChooser.getExtensionFilters().addAll(FILTER_IMAGE_FILES, FILTER_ALL_FILES);
        fileChooser.setSelectedExtensionFilter(FILTER_IMAGE_FILES);
        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            Image image = readImageFromFile(selectedFile);
            if (image != null) {
                return Optional.of(image);
            }
        }
        return Optional.empty();
    }

    public static boolean isTemplateImageSizeOk(Image image) {
        return image.getHeight() % TS == 0 && image.getWidth() % TS == 0;
    }
}
