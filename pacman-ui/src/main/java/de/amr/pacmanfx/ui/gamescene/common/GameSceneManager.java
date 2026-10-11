/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Named;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.engine.gamescene.GameSceneConfig;
import de.amr.pacmanfx.ui.EngineUIContext;
import javafx.beans.property.ObjectProperty;

import java.util.Optional;

public interface GameSceneManager {

    void setGameSceneConfig(GameSceneConfig gameSceneConfig);

    Optional<GameScene> optCurrentGameScene();

    void removeCurrentGameScene();

    boolean currentGameSceneHasID(Named gameSceneID);

    void forceGameSceneUpdate(EngineUIContext engineContext);

    GameScene currentGameScene();

    void updateGameSceneAndForceReload(EngineUIContext engineContext, boolean forceGameSceneReload);

    ObjectProperty<GameScene> currentGameSceneProperty();
}
