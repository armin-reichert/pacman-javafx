/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.scene.common;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.scene.common.GameSceneConfiguration;
import javafx.beans.value.ObservableDoubleValue;

public interface GameSceneConfiguration3D extends GameSceneConfiguration {

    void initPlayScene3D(GameContext context, ObservableDoubleValue widthProperty, ObservableDoubleValue heightProperty);
}
