/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views;

import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;
import org.tinylog.Supplier;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import static java.util.Objects.requireNonNull;

public final class GameViewManager {

    private final ObjectProperty<GameView> currentView = new SimpleObjectProperty<>();

    private Supplier<EditorView> editorViewFactory;
    private BooleanSupplier editorCanOpen = () -> false;

    private StartPagesView startPagesView;
    private GamePlayView gamePlayView;
    private EditorView editorView;

    public GameViewManager() {}

    public void connect(Game game) {
        requireNonNull(game);

        currentViewProperty().addListener((_, oldView, newView) -> {
            if (oldView != null) {
                oldView.onExit();
                oldView.actionBindings().dispose();
            }
            game.ui().window().mainScene().replaceSubView(newView);
            game.ui().flashMessages().clearMessage();
            newView.onEnter();
        });

        setEditorCanOpen(() -> {
            // No editor view exists or editor already selected: cannot open
            if (editorView == null || isSelected(editorView)) return false;

            if (isSelected(startPagesView)) return true;

            if (isSelected(gamePlayView)) {
                return !game.currentGameContext().model().isPlaying();
            }

            return false;
        });

        gamePlayView.connect(game);
    }

    public ObjectProperty<GameView> currentViewProperty() {
        return currentView;
    }

    public GameView currentView() {
        return currentView.get();
    }

    public void setGamePlayView(GamePlayView gamePlayView) {
        requireNonNull(gamePlayView);
        this.gamePlayView = gamePlayView;
    }

    public void setStartPagesView(StartPagesView startPagesView) {
        requireNonNull(startPagesView);
        this.startPagesView = startPagesView;
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
        return startPagesView;
    }

    public GamePlayView gamePlayView() {
        return gamePlayView;
    }

    public Optional<EditorView> optEditorView() {
        return Optional.ofNullable(editorView);
    }

    public void selectStartView() {
        if (startPagesView == null) {
            throw new IllegalStateException("No start view has been set");
        }
        currentViewProperty().set(startPagesView);
    }

    public void selectGamePlayView() {
        if (gamePlayView == null) {
            throw new IllegalStateException("No Game play view has been set");
        }
        currentViewProperty().set(gamePlayView);
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
            currentViewProperty().set(editorView);
            return true;
        }
        else {
            Logger.info("Editor cannot open in current state");
            return false;
        }
    }

    public boolean isSelected(GameView gameView) {
        requireNonNull(gameView);
        return currentView() == gameView;
    }
 }
