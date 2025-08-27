package de.amr.pacmanfx.tilemap.editor;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

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
        FileChooser selector = new FileChooser();
        selector.setTitle(title);
        selector.setInitialDirectory(currentDirectory);
        selector.getExtensionFilters().addAll(TileMapEditor.FILTER_IMAGE_FILES, TileMapEditor.FILTER_ALL_FILES);
        selector.setSelectedExtensionFilter(TileMapEditor.FILTER_IMAGE_FILES);
        File selectedFile = selector.showOpenDialog(window);
        if (selectedFile != null) {
            Image image = readImageFromFile(selectedFile);
            if (image != null) {
                return Optional.of(image);
            }
        }
        return Optional.empty();
    }

}
