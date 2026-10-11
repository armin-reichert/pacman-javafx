/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.gamescene;

import de.amr.basics.Named;
import javafx.beans.property.ObjectProperty;

import java.util.Optional;

public interface GameSceneManager {

    void setGameSceneConfig(GameSceneConfig gameSceneConfig);

    ObjectProperty<GameScene> currentGameSceneProperty();

    Optional<GameScene> optCurrentGameScene();

    void removeCurrentGameScene();

    boolean currentGameSceneHasID(Named gameSceneID);

    GameScene currentGameScene();
}
