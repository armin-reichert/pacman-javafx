/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews;

import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.subviews.editor.EditorView;
import de.amr.pacmanfx.ui.subviews.playview.GamePlayView;
import de.amr.pacmanfx.ui.subviews.startpages.StartPagesView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;
import org.tinylog.Supplier;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import static java.util.Objects.requireNonNull;

public final class SubViewManager {

    private final ObjectProperty<SubView> currentSubView = new SimpleObjectProperty<>();

    private Supplier<EditorView> editorViewFactory;
    private BooleanSupplier editorCanOpen = () -> false;

    private StartPagesView startView;
    private GamePlayView gamePlayView;
    private EditorView editorView;

    public SubViewManager() {}

    public void connect(Game game) {
        requireNonNull(game);

        currentSubViewProperty().addListener((_, oldView, newView) -> {
            if (oldView != null) {
                oldView.onExit();
                oldView.actionBindings().dispose();
            }
            game.ui().view().mainScene().replaceSubView(newView);
            game.ui().flashMessages().clearMessage();
            newView.onEnter();
        });

        setEditorCanOpen(() -> {
            // No editor view exists or editor already selected: cannot open
            if (editorView == null || isSelected(editorView)) return false;

            if (isSelected(startView)) return true;

            if (isSelected(gamePlayView)) {
                return !game.currentGameContext().model().isPlaying();
            }

            return false;
        });

        gamePlayView.connect(game);
    }

    public ObjectProperty<SubView> currentSubViewProperty() {
        return currentSubView;
    }

    public SubView currentSubView() {
        return currentSubView.get();
    }

    public void setGamePlayView(GamePlayView newGamePlayView) {
        requireNonNull(newGamePlayView);
        gamePlayView = newGamePlayView;
    }

    public void setStartView(StartPagesView newStartView) {
        requireNonNull(newStartView);
        startView = newStartView;
    }

    public void setEditorViewFactory(Supplier<EditorView> factory) {
        if (editorViewFactory != null) {
            throw new IllegalStateException("EditorViewFactory is already set");
        }
        this.editorViewFactory = requireNonNull(factory);
    }

    public void setEditorCanOpen(BooleanSupplier editorCanOpen) {
        this.editorCanOpen = requireNonNull(editorCanOpen);
    }

    public StartPagesView startView() {
        return startView;
    }

    public GamePlayView gamePlayView() {
        return gamePlayView;
    }

    public Optional<EditorView> optEditorView() {
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
            if (editorViewFactory == null) {
                throw new IllegalStateException("No editor view factory has been set");
            }
            editorView = editorViewFactory.get();
        }
    }

    public boolean trySelectEditorView() {
        if (editorView == null) {
            Logger.info("Editor view has not been created yet");
            return false;
        }
        if (editorCanOpen.getAsBoolean()) {
            currentSubViewProperty().set(editorView);
            return true;
        }
        else {
            Logger.info("Editor cannot open in current state");
            return false;
        }
    }

    public boolean isSelected(SubView view) {
        return view != null && currentSubView() == view;
    }

 }
