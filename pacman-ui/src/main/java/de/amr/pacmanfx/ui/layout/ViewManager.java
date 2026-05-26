/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.layout.playview.PlayView;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.tinylog.Logger;
import org.tinylog.Supplier;

import java.io.File;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static java.util.Objects.requireNonNull;

public class ViewManager {

    /** Index in the layout pane's child list where the active view is embedded. */
    private static final int RESERVED_VIEW_INDEX_IN_LAYOUT = 0;

    private final ObjectProperty<View> currentView = new SimpleObjectProperty<>();

    private Supplier<EditorView> editorViewFactory;
    private BooleanSupplier editorCanOpen = () -> false;

    private final File editorWorkDir;

    private StartPagesCarousel startView;
    private PlayView playView;
    private EditorView editorView;

    private final GameUI ui;

    public ViewManager(GameUI ui, Scene scene, File editorWorkDir, FlashMessageView flashMessageView) {
        this.ui = requireNonNull(ui);
        requireNonNull(scene);
        requireNonNull(flashMessageView);

        this.editorWorkDir = requireNonNull(editorWorkDir);

        currentViewProperty().addListener((_, oldView, newView) -> {
            if (oldView != null) {
                oldView.onExit();
                oldView.actionBindings().removeFromKeyboard();
            }

            if (!(scene.getRoot() instanceof Pane rootPane)) {
                throw new IllegalStateException(
                    "Cannot replace view: Scene root must be a Pane but is " + scene.getRoot());
            }
            if (rootPane.getChildren().isEmpty()) {
                throw new IllegalStateException(
                    "Layout pane has no placeholder for embedding view");
            }

            rootPane.getChildren().set(RESERVED_VIEW_INDEX_IN_LAYOUT, newView.root());
            newView.actionBindings().assignToKeyboard();
            newView.onEnter();
            flashMessageView.clear();
        });
    }

    public void setPlayView(PlayView playView) {
        this.playView = requireNonNull(playView);
    }

    public void setStartView(StartPagesCarousel startView) {
        this.startView = requireNonNull(startView);
    }

    public void setEditorViewFactory(Supplier<EditorView> factory) {
        this.editorViewFactory = requireNonNull(factory);
    }

    public void setEditorCanOpen(BooleanSupplier editorCanOpen) {
        this.editorCanOpen = requireNonNull(editorCanOpen);
    }

    public StartPagesCarousel startView() {
        return startView;
    }

    public PlayView playView() {
        return playView;
    }

    public Optional<EditorView> optEditorView() {
        return Optional.ofNullable(editorView);
    }

    public void selectStartView() {
        ui.stopGame();
        currentViewProperty().set(startView);
    }

    public void selectPlayView() {
        currentViewProperty().set(playView);
    }

    public void selectEditorView() {
        if (editorCanOpen.getAsBoolean()) {
            ui.stopGame();
            if (editorView == null) {
                editorView = editorViewFactory.get();
            }
            editorView.editor().init(editorWorkDir);
            editorView.editor().start();
            currentViewProperty().set(editorView);
        } else {
            Logger.warn("Editor cannot open!");
        }
    }

    public boolean isStartViewSelected() {
        return currentView() == startView;
    }

    public boolean isPlayViewSelected() {
        return currentView() == playView;
    }

    public boolean isEditorViewSelected() {
        return currentView() == editorView;
    }

    public ObjectProperty<View> currentViewProperty() {
        return currentView;
    }

    public View currentView() {
        return currentView.get();
    }
}
