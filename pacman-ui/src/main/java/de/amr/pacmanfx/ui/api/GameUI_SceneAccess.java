/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import java.util.Optional;

public interface GameUI_SceneAccess {

    Optional<GameScene> currentGameScene();

    boolean isCurrentGameSceneID(String id);

    void updateGameScene(boolean forceReloading);
}
