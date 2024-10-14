package de.amr.games.pacman.ui3d.scene.common;

import de.amr.games.pacman.ui2d.GameContext;
import javafx.beans.value.ObservableDoubleValue;

public interface GameSceneConfiguration3D {

    void initPlayScene3D(GameContext context, ObservableDoubleValue widthProperty, ObservableDoubleValue heightProperty);
}
