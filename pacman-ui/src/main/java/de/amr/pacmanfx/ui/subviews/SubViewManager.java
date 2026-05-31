/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.subviews.editor.Editor_SubView;
import de.amr.pacmanfx.ui.subviews.playview.GamePlay_SubView;
import de.amr.pacmanfx.ui.subviews.startpages.StartPages_SubView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;
import org.tinylog.Supplier;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import static java.util.Objects.requireNonNull;

public class SubViewManager {

    private final ObjectProperty<GameUI_SubView> currentSubView = new SimpleObjectProperty<>();

    private Supplier<Editor_SubView> editorViewFactory;
    private BooleanSupplier editorCanOpen = () -> false;

    private StartPages_SubView startView;
    private GamePlay_SubView gamePlayView;
    private Editor_SubView editorView;

    public SubViewManager() {}

    public void setUI(GameUI ui) {
        requireNonNull(ui);

        currentSubViewProperty().addListener((_, oldView, newView) -> {
            if (oldView != null) {
                oldView.onExit();
                oldView.actionBindings().dispose();
            }
            newView.onEnter();
            ui.view().replaceSubView(newView);
            ui.access().flashMessages().clearMessage();
        });
    }

    public void setGamePlayView(GamePlay_SubView newGamePlayView) {
        requireNonNull(newGamePlayView);
        gamePlayView = newGamePlayView;
    }

    public void setStartView(StartPages_SubView newStartView) {
        requireNonNull(newStartView);
        startView = newStartView;
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

    public GamePlay_SubView gamePlayView() {
        return gamePlayView;
    }

    public Optional<Editor_SubView> optEditorView() {
        return Optional.ofNullable(editorView);
    }

    public void selectStartView() {
        if (startView == null) {
            throw new IllegalStateException("No start view has been set");
        }
        currentSubViewProperty().set(startView);
    }

    public void selectGamePlayView() {
        if (gamePlayView == null) {
            throw new IllegalStateException("No Game play view has been set");
        }
        currentSubViewProperty().set(gamePlayView);
    }

    public void ensureEditorViewCreated() {
        if (editorView == null) {
            editorView = editorViewFactory.get();
        }
    }

    public boolean trySelectEditorView() {
        if (editorView == null) {
            Logger.info("Editor view has not been created yet");
            return false;
        }
        if (editorCanOpen.getAsBoolean()) {
            editorView.editor().start();
            currentSubViewProperty().set(editorView);
            return true;
        }
        else {
            Logger.info("Editor cannot open (maybe already opened?)");
            return false;
        }
    }

    public boolean isSelected(GameUI_SubView view) {
        return currentView() == view;
    }

    public ObjectProperty<GameUI_SubView> currentSubViewProperty() {
        return currentSubView;
    }

    public GameUI_SubView currentView() {
        return currentSubView.get();
    }
}
