/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import java.util.Optional;

public interface GameSceneAccess {

    Optional<GameScene> currentGameScene();

    boolean isCurrentGameSceneID(String id);

    void updateGameScene(boolean forceReloading);
}
