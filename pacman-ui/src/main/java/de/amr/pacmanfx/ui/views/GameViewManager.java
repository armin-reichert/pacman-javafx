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

import java.util.EnumMap;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static java.util.Objects.requireNonNull;

public final class GameViewManager {

    private final ObjectProperty<GameViewID> currentViewID = new SimpleObjectProperty<>();

    private final EnumMap<GameViewID, GameView> views = new EnumMap<>(GameViewID.class);

    private Supplier<EditorView> editorViewFactory;
    private BooleanSupplier editorCanOpen = () -> false;

    public GameViewManager() {}

    public void connect(Game game) {
        requireNonNull(game);

        currentViewIDProperty().addListener((_, oldID, newID) -> {
            if (oldID != null) {
                assertView(oldID).onExit();
            }

            final GameView newView = assertView(newID);
            newView.onEnter();

            game.ui().window().mainScene().replaceGameView(newView);
            game.ui().flashMessages().clearMessage();
        });

        editorCanOpen = () -> {
            if (isSelected(GameViewID.START_PAGES)) {
                return true;
            }
            if (isSelected(GameViewID.GAMEPLAY)) {
                return !game.currentGameContext().model().isPlaying();
            }
            return false;
        };
    }

    public void setView(GameViewID viewID, GameView gameView) {
        requireNonNull(viewID);
        requireNonNull(gameView);
        views.put(viewID, gameView);
        Logger.info("Game view id='{}' set to {}", viewID, gameView);
    }


    public GameView assertView(GameViewID viewID) {
        final GameView view = views.get(viewID);
        if (view == null) {
            throw new IllegalStateException("No view found for ID: " + viewID);
        }
        return view;
    }

    public ObjectProperty<GameViewID> currentViewIDProperty() {
        return currentViewID;
    }

    public GameViewID currentViewID() {
        return currentViewID.get();
    }

    public GameView assertCurrentView() {
        return assertView(currentViewID());
    }

    public Optional<GameView> optCurrentView() {
        if (currentViewID() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(views.get(currentViewID()));
    }

    public boolean isSelected(GameViewID viewID) {
        requireNonNull(viewID);
        return assertCurrentView() == assertView(viewID);
    }

    // Start pages view

    public StartPagesView startPagesView() {
        return (StartPagesView) assertView(GameViewID.START_PAGES);
    }

    public void selectStartPagesView() {
        currentViewIDProperty().set(GameViewID.START_PAGES);
    }

    // Game play view

    public void selectGamePlayView() {
        currentViewIDProperty().set(GameViewID.GAMEPLAY);
    }

    public GamePlayView gamePlayView() {
        return (GamePlayView) assertView(GameViewID.GAMEPLAY);
    }

    // Editor view

    public void setEditorViewFactory(Supplier<EditorView> factory) {
        if (editorViewFactory != null) {
            throw new IllegalStateException("EditorViewFactory is already set");
        }
        this.editorViewFactory = requireNonNull(factory);
    }

    public Optional<EditorView> optEditorView() {
        final EditorView editorView = (EditorView) views.get(GameViewID.EDITOR);
        return Optional.ofNullable(editorView);
    }

    public void ensureEditorViewCreated() {
        if (views.get(GameViewID.EDITOR) == null) {
            if (editorViewFactory == null) {
                throw new IllegalStateException("No editor view factory has been set");
            }
            views.put(GameViewID.EDITOR, editorViewFactory.get());
        }
    }

    public boolean trySelectEditorView() {
        if (views.get(GameViewID.EDITOR) == null) {
            Logger.info("Editor view has not been created yet");
            return false;
        }
        if (editorCanOpen.getAsBoolean()) {
            currentViewIDProperty().set(GameViewID.EDITOR);
            return true;
        }
        else {
            Logger.info("Editor cannot open in current state");
            return false;
        }
    }
}
