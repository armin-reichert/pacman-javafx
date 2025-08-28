package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.MessageType;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.FILTER_IMAGE_FILES;
import static de.amr.pacmanfx.tilemap.editor.TemplateImageManager.*;

public class Action_OpenTemplateCreateMap extends AbstractEditorAction<Void> {

    public Action_OpenTemplateCreateMap(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public Void execute() {
        openTemplateImage(editor.stage(), translated("open_template_image"), editor.currentDirectory()).ifPresent(image -> {
            if (isTemplateImageSizeOk(image)) {
                editor.setTemplateImage(image);
                new Action_CreateMapFromTemplate(editor, image).execute();
                editor.selectTemplateImageTab();
                editor.messageManager().showMessage("Select map colors from template!", 20, MessageType.INFO);
            } else {
                editor.messageManager().showMessage("Template image size seems dubious", 3, MessageType.WARNING);
            }
        });
        return null;
    }

    private Optional<Image> openTemplateImage(Window window, String title, File currentDirectory) {
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

    private Image readImageFromFile(File file) {
        try (FileInputStream stream = new FileInputStream(file)) {
            return new Image(stream);
        } catch (IOException x) {
            Logger.error(x);
            return null;
        }
    }
}
