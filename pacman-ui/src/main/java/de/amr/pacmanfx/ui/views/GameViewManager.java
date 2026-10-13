/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views;

import de.amr.basics.Named;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.ui.EngineUIContext;
import javafx.beans.property.ObjectProperty;

import java.util.Optional;

public interface GameViewManager {

    void init(EngineUIContext engineContext);

    <V extends GameView> V assertView(Named id, Class<V> viewClass);

    default GameView assertView(Named viewID) {
        return assertView(viewID, GameView.class);
    }

    <V extends GameView> Optional<V> optView(Named id, Class<V> viewClass);

    ObjectProperty<Named> currentViewIDProperty();

    Named currentViewID();

    Optional<GameView> optCurrentView();

    void onGameSuspended(GameScene gameScene);

    boolean isSelected(Named viewID);
}
