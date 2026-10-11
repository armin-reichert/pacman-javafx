/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views;

import de.amr.basics.Named;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class DefaultGameViewManager implements GameViewManager {

    private final ObjectProperty<Named> currentViewID = new SimpleObjectProperty<>();

    private final Map<Named, GameView> views = new HashMap<>();

    public DefaultGameViewManager(EngineUIContext engineContext) {
        requireNonNull(engineContext);

        registerView(GameViewID.START_PAGES, new StartPagesView());
        registerView(GameViewID.GAMEPLAY, new GamePlayView());
        registerView(GameViewID.EDITOR, new EditorView());

        currentViewIDProperty().addListener((_, oldID, newID) -> {
            engineContext.ui().clearMessage();

            if (oldID != null) {
                assertView(oldID).onExit();
            }

            final GameView newView = assertView(newID);
            engineContext.ui().window().mainScene().replaceGameView(newView);

            newView.onEnter();
        });

        views.values().forEach(gameView -> gameView.setEngineContext(engineContext));
    }

    public void registerView(Named viewID, GameView gameView) {
        requireNonNull(viewID);
        requireNonNull(gameView);
        views.put(viewID, gameView);
        Logger.info("Game view registered. ID='{}': {}", viewID, gameView);
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T extends GameView> T assertView(Named viewID, Class<T> viewClass) {
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

    @Override
    @SuppressWarnings("unchecked")
    public <V extends GameView> Optional<V> optView(Named viewID, Class<V> viewClass) {
        final GameView view = views.get(viewID);
        return viewClass.isInstance(view) ? Optional.of((V) view) : Optional.empty();
    }

    @Override
    public void onGameVariantChanged() {
        currentViewIDProperty().set(GameViewID.START_PAGES);
        assertView(GameViewID.START_PAGES, StartPagesView.class).rootPane().setSelectedIndex(0);
    }

    @Override
    public void onGameSuspended(GameScene currentGameScene) {
        currentGameScene.deactivate();
    }

    @Override
    public ObjectProperty<Named> currentViewIDProperty() {
        return currentViewID;
    }

    @Override
    public Named currentViewID() {
        return currentViewID.get();
    }

    @Override
    public Optional<GameView> optCurrentView() {
        if (currentViewID() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(views.get(currentViewID()));
    }

    public boolean isSelected(Named viewID) {
        requireNonNull(viewID);
        return currentViewID() == viewID;
    }
}
