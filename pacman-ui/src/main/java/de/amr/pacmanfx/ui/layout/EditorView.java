/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.mapeditor.SaveConfirmationDialog;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.mapeditor.actions.Action_SaveMapFileInteractively;
import de.amr.pacmanfx.ui.action.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.GameAssets;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import de.amr.pacmanfx.ui.api.GameUI_View;
import de.amr.pacmanfx.uilib.model3D.PacManModel3DRepository;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static de.amr.pacmanfx.uilib.Ufx.colorBackground;

public class EditorView implements GameUI_View {

    private final TileMapEditor editor;
    private Consumer<TileMapEditor> quitEditorAction = editor -> {};

    public EditorView(Stage stage, GameAssets assets) {
        editor = new TileMapEditor(stage, PacManModel3DRepository.theRepository());
        MenuItem miQuitEditor = createQuitEditorMenuItem(assets);
        editor.ui().menuBar().menuFile().getItems().addAll(new SeparatorMenuItem(), miQuitEditor);
        editor.ui().layoutPane().setBackground(colorBackground(Color.web("#dddddd"))); // JavaFX default grey
    }

    private MenuItem createQuitEditorMenuItem(GameAssets assets) {
        var miQuitEditor = new MenuItem(assets.translated("back_to_game"));
        miQuitEditor.setOnAction(e -> {
            if (!editor.isEdited()) {
                editor.stop();
                quitEditorAction.accept(editor);
                return;
            }
            var saveDialog = new SaveConfirmationDialog();
            saveDialog.showAndWait().ifPresent(choice -> {
                if (choice == SaveConfirmationDialog.SAVE) {
                    File selectedFile = new Action_SaveMapFileInteractively(editor.ui()).execute();
                    if (selectedFile == null) { // File selection and saving was canceled
                        e.consume();
                    } else {
                        editor.stop();
                        quitEditorAction.accept(editor);
                    }
                }
                else if (choice == SaveConfirmationDialog.DONT_SAVE) {
                    editor.setEdited(false);
                    editor.stop();
                    quitEditorAction.accept(editor);
                }
                else if (choice == ButtonType.CANCEL) {
                    e.consume();
                }
            });
        });
        return miQuitEditor;
    }

    public void setQuitEditorAction(Consumer<TileMapEditor> quitEditorAction) {
        this.quitEditorAction = quitEditorAction;
    }

    public TileMapEditor editor() {
        return editor;
    }

    @Override
    public ActionBindingsManager actionBindingsManager() { return DefaultActionBindingsManager.EMPTY_BINDINGS_MANAGER; }

    @Override
    public Region root() {
        return editor.ui().layoutPane();
    }

    @Override
    public Optional<Supplier<String>> titleSupplier() {
        return Optional.of(editor.ui().titleProperty()::get);
    }
}