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

    private StartPagesView startPagesView;

    private GamePlayView gamePlayView;

    private Supplier<EditorView> editorViewFactory;
    private EditorView editorView;
    private BooleanSupplier editorCanOpen = () -> false;

    public GameViewManager() {
    }

    public void connect(Game game) {
        requireNonNull(game);

        currentViewProperty().addListener((_, oldView, newView) -> {
            if (oldView != null) {
                oldView.onExit();
            }
            game.ui().window().mainScene().replaceGameView(newView);
            game.ui().flashMessages().clearMessage();
            newView.onEnter();
        });

        editorCanOpen = () -> {
            if (isSelected(startPagesView)) return true;

            if (isSelected(gamePlayView)) {
                return !game.currentGameContext().model().isPlaying();
            }

            if (editorView == null || isSelected(editorView)) return false;

            return false;
        };

    }

    public ObjectProperty<GameView> currentViewProperty() {
        return currentView;
    }

    public GameView currentView() {
        return currentView.get();
    }

    public boolean isSelected(GameView gameView) {
        requireNonNull(gameView);
        return currentView() == gameView;
    }

    // Start pages view

    public void setStartPagesView(StartPagesView startPagesView) {
        requireNonNull(startPagesView);
        this.startPagesView = startPagesView;
    }

    public StartPagesView startPagesView() {
        return startPagesView;
    }

    public void selectStartPagesView() {
        if (startPagesView == null) {
            throw new IllegalStateException("No start view has been set");
        }
        currentViewProperty().set(startPagesView);
    }

    // Game play view

    public void setGamePlayView(GamePlayView gamePlayView) {
        requireNonNull(gamePlayView);
        this.gamePlayView = gamePlayView;
    }

    public void selectGamePlayView() {
        if (gamePlayView == null) {
            throw new IllegalStateException("No Game play view has been set");
        }
        currentViewProperty().set(gamePlayView);
    }

    public GamePlayView gamePlayView() {
        return gamePlayView;
    }

    // Editor view

    public void setEditorViewFactory(Supplier<EditorView> factory) {
        if (editorViewFactory != null) {
            throw new IllegalStateException("EditorViewFactory is already set");
        }
        this.editorViewFactory = requireNonNull(factory);
    }

    public Optional<EditorView> optEditorView() {
        return Optional.ofNullable(editorView);
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
}
