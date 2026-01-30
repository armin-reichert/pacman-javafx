/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.EditMode;
import de.amr.pacmanfx.mapeditor.MessageType;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.mapeditor.EditorGlobals.*;

public class Action_OpenTemplateCreateMap extends EditorUIAction<Void> {

    public Action_OpenTemplateCreateMap(TileMapEditorUI ui) {
        super(ui);
    }

    @Override
    public Void execute() {
        openTemplateImage().ifPresent(image -> {
            if (isTemplateImageSizeOk(image)) {
                editor.setTemplateImage(image);
                if (ui.editModeIs(EditMode.INSPECT) || ui.editModeIs(EditMode.ERASE)) {
                    ui.setEditMode(EditMode.EDIT);
                }
                new Action_SetEmptyMapFromTemplateImage(ui, image).execute();
            } else {
                ui.messageDisplay().showMessage("Template image size seems dubious", 3, MessageType.WARNING);
            }
        });
        return null;
    }

    private boolean isTemplateImageSizeOk(Image image) {
        return image.getHeight() % TS == 0 && image.getWidth() % TS == 0;
    }

    private Optional<Image> openTemplateImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(translated("open_template_image"));
        fileChooser.setInitialDirectory(editor.currentDirectory());
        fileChooser.getExtensionFilters().addAll(FILTER_IMAGE_FILES, FILTER_ALL_FILES);
        fileChooser.setSelectedExtensionFilter(FILTER_IMAGE_FILES);
        File file = fileChooser.showOpenDialog(ui.stage());
        return file == null ? Optional.empty() : readImage(file);
    }

    private Optional<Image> readImage(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return Optional.of(new Image(fis));
        } catch (IOException x) {
            Logger.error(x, "Could not read image from file '{}'", file);
            return Optional.empty();
        }
    }
}