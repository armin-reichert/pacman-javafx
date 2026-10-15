/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views;

import de.amr.basics.Named;
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

    public DefaultGameViewManager() {}

    @Override
    public void init(EngineUIContext engineContext) {
        requireNonNull(engineContext);

        views.put(GameViewID.START_PAGES, new StartPagesView(engineContext.translationManager()));
        views.put(GameViewID.GAMEPLAY, new GamePlayView(engineContext));
        views.put(GameViewID.EDITOR, new EditorView());

        currentViewID.set(GameViewID.START_PAGES);
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
    @SuppressWarnings("unchecked")
    public <GV extends GameView> GV assertView(Named viewID, Class<GV> gameViewClass) {
        requireNonNull(viewID);
        requireNonNull(gameViewClass);

        if (!(viewID instanceof GameViewID)) {
            throw new IllegalArgumentException(viewID + " is not a GameViewID");
        }
        final GameView gameView = views.get(viewID);
        if (gameView == null) {
            throw new IllegalStateException("No view found for ID: " + viewID);
        }
        if (!gameViewClass.isInstance(gameView)) {
            throw new IllegalStateException(
                "View for ID " + viewID + " is not of expected type " + gameViewClass.getName() +
                    " but was " + gameView.getClass().getName()
            );
        }
        return (GV) gameView;
    }

    @Override
    public Optional<GameView> optCurrentView() {
        return optView(currentViewID(), GameView.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <GV extends GameView> Optional<GV> optView(Named viewID, Class<GV> gameViewClass) {
        requireNonNull(viewID);
        requireNonNull(gameViewClass);

        if (!(viewID instanceof GameViewID)) {
            throw new IllegalArgumentException(viewID + " is not a GameViewID");
        }
        final GameView view = views.get(viewID);
        return gameViewClass.isInstance(view) ? Optional.of((GV) view) : Optional.empty();
    }

    @Override
    public boolean isSelected(Named viewID) {
        requireNonNull(viewID);
        return currentViewID().name().equals(viewID.name());
    }
}
