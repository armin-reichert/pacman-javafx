/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views;

import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class GameViewManager {

    private final ObjectProperty<GameViewID> currentViewID = new SimpleObjectProperty<>();

    private final EnumMap<GameViewID, GameView> views = new EnumMap<>(GameViewID.class);

    public GameViewManager() {}

    public void setGameAppContext(GameAppContext appContext) {
        requireNonNull(appContext);

        currentViewIDProperty().addListener((_, oldID, newID) -> {
            appContext.ui().clearMessage();

            if (oldID != null) {
                assertView(oldID).onExit();
            }

            final GameView newView = assertView(newID);
            appContext.ui().window().mainScene().replaceGameView(newView);

            newView.onEnter();
        });

        views.values().forEach(gameView -> gameView.setAppContext(appContext));
    }

    public void registerView(GameViewID viewID, GameView gameView) {
        requireNonNull(viewID);
        requireNonNull(gameView);
        views.put(viewID, gameView);
        Logger.info("Game view registered. ID='{}': {}", viewID, gameView);
    }

    public GameView assertView(GameViewID viewID) {
        return assertView(viewID, GameView.class);
    }

    @SuppressWarnings("unchecked")
    public <T extends GameView> T assertView(GameViewID viewID, Class<T> viewClass) {
        final GameView view = views.get(viewID);
        if (view == null) {
            throw new IllegalStateException("No view found for ID: " + viewID);
        }
        if (!viewClass.isInstance(view)) {
            throw new IllegalStateException(
                "View for ID " + viewID + " is not of expected type " + viewClass.getName() +
                    " but was " + view.getClass().getName()
            );
        }
        return (T) view;
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
        return currentViewID() == viewID;
    }

    // Start pages view

    public void selectStartPagesView() {
        currentViewIDProperty().set(GameViewID.START_PAGES);
    }

    public StartPagesView startPagesView() {
        return (StartPagesView) assertView(GameViewID.START_PAGES);
    }

    // Game play view

    public void selectGamePlayView() {
        currentViewIDProperty().set(GameViewID.GAMEPLAY);
    }

    public GamePlayView gamePlayView() {
        return (GamePlayView) assertView(GameViewID.GAMEPLAY);
    }

    // Editor view

    public Optional<EditorView> optEditorView() {
        final EditorView editorView = (EditorView) views.get(GameViewID.EDITOR);
        return Optional.ofNullable(editorView);
    }

    public boolean trySelectEditorView(GameAppContext appContext) {
        if (views.get(GameViewID.EDITOR) == null) {
            Logger.info("Editor view has not been created yet");
            return false;
        }
        if (canOpenEditor(appContext)) {
            currentViewIDProperty().set(GameViewID.EDITOR);
            return true;
        }
        else {
            Logger.info("Editor cannot open in current state");
            return false;
        }
    }

    private boolean canOpenEditor(GameAppContext appContext) {
        if (isSelected(GameViewID.START_PAGES)) {
            return true;
        }
        if (isSelected(GameViewID.GAMEPLAY)) {
            return !appContext.currentGameContext().model().isPlaying();
        }
        return false;
    }
}
