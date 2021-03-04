package de.amr.games.pacman.ui.fx.common;

import javafx.scene.SubScene;

public interface GameScene3D extends GameScene {

	SubScene getSubScene();

	void resize(double width, double height);
}