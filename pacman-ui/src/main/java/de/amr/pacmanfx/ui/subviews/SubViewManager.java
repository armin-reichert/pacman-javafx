/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.subviews.editor.Editor_SubView;
import de.amr.pacmanfx.ui.subviews.playview.GamePlay_SubView;
import de.amr.pacmanfx.ui.FlashMessageManager;
import de.amr.pacmanfx.ui.subviews.startpages.StartPages_SubView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Pane;
import org.tinylog.Logger;
import org.tinylog.Supplier;

import java.io.File;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static java.util.Objects.requireNonNull;

public class SubViewManager {

    /** Index in the root pane's child list where the active view is embedded. */
    private static final int RESERVED_VIEW_INDEX_IN_LAYOUT = 0;

    private final ObjectProperty<GameUI_SubView> currentView = new SimpleObjectProperty<>();

    private Supplier<Editor_SubView> editorViewFactory;
    private BooleanSupplier editorCanOpen = () -> false;

    private StartPages_SubView startView;
    private GamePlay_SubView playView;
    private Editor_SubView editorView;

    public SubViewManager() {}

    public void init(Pane rootPane, FlashMessageManager flashMessageManager) {
        requireNonNull(flashMessageManager);

        currentViewProperty().addListener((_, oldView, newView) -> {
            if (oldView != null) {
                oldView.onExit();
                oldView.actionBindings().dispose();
            }

            if (rootPane.getChildren().isEmpty()) {
                throw new IllegalStateException("Root pane has no placeholder for embedding view");
            }

            rootPane.getChildren().set(RESERVED_VIEW_INDEX_IN_LAYOUT, newView.rootPane());

            newView.onEnter();

            flashMessageManager.clearMessage();
        });
    }

    public void setPlayView(GamePlay_SubView playView) {
        this.playView = requireNonNull(playView);
    }

    public void setStartView(StartPages_SubView startView) {
        this.startView = requireNonNull(startView);
    }

    public void setEditorViewFactory(Supplier<Editor_SubView> factory) {
        this.editorViewFactory = requireNonNull(factory);
    }

    public void setEditorCanOpen(BooleanSupplier editorCanOpen) {
        this.editorCanOpen = requireNonNull(editorCanOpen);
    }

    public StartPages_SubView startView() {
        return startView;
    }

    public GamePlay_SubView playView() {
        return playView;
    }

    public Optional<Editor_SubView> optEditorView() {
        return Optional.ofNullable(editorView);
    }

    public void selectStartView() {
        currentViewProperty().set(startView);
    }

    public void selectPlayView() {
        currentViewProperty().set(playView);
    }

    public void createEditorIfNotExisting(File workDir) {
        if (editorView == null) {
            editorView = editorViewFactory.get();
            editorView.editor().init(workDir);
        }
    }

    public void selectEditorView(GameUI ui) {
        if (editorView == null) {
            Logger.warn("Editor view has not been created yet");
            return;
        }
        if (editorCanOpen.getAsBoolean()) {
            ui.life().stopGame();
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

    public ObjectProperty<GameUI_SubView> currentViewProperty() {
        return currentView;
    }

    public GameUI_SubView currentView() {
        return currentView.get();
    }
}
