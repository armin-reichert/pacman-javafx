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

    public static boolean isTemplateImageSizeOk(Image image) {
        return image.getHeight() % TS == 0 && image.getWidth() % TS == 0;
    }
}
