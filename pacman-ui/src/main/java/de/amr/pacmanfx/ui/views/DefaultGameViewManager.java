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

import java.util.EnumMap;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class DefaultGameViewManager implements GameViewManager {

    private final ObjectProperty<Named> currentViewID = new SimpleObjectProperty<>();

    private final EnumMap<GameViewID, GameView> views = new EnumMap<>(GameViewID.class);

    public DefaultGameViewManager(EngineUIContext engineContext) {
        requireNonNull(engineContext);

        views.put(GameViewID.START_PAGES, new StartPagesView());
        views.put(GameViewID.GAMEPLAY, new GamePlayView());
        views.put(GameViewID.EDITOR, new EditorView());

        views.values().forEach(gameView -> gameView.setEngineContext(engineContext));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends GameView> T assertView(Named viewID, Class<T> viewClass) {
        if (!(viewID instanceof GameViewID)) {
            throw new IllegalArgumentException(viewID + " is not a GameViewID");
        }
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
        if (!(viewID instanceof GameViewID)) {
            throw new IllegalArgumentException(viewID + " is not a GameViewID");
        }
        final GameView view = views.get(viewID);
        return viewClass.isInstance(view) ? Optional.of((V) view) : Optional.empty();
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
        final var viewID = currentViewID();
        if (viewID == null) {
            return Optional.empty();
        }
        if (!(viewID instanceof GameViewID)) {
            throw new IllegalArgumentException(viewID + " is not a GameViewID");
        }
        return Optional.ofNullable(views.get(viewID));
    }

    @Override
    public boolean isSelected(Named viewID) {
        requireNonNull(viewID);
        return currentViewID() == viewID;
    }
}
