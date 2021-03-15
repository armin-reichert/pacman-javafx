package de.amr.games.pacman.ui.fx.common;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.DrawMode;

public class Env {

	public static SimpleBooleanProperty $measureTime = new SimpleBooleanProperty(false);
	public static SimpleBooleanProperty $paused = new SimpleBooleanProperty(false);
	public static SimpleBooleanProperty $infoViewVisible = new SimpleBooleanProperty(true);
	public static SimpleObjectProperty<DrawMode> $drawMode = new SimpleObjectProperty<DrawMode>(DrawMode.FILL);
	public static SimpleBooleanProperty $use3DScenes = new SimpleBooleanProperty(true);
	public static SimpleBooleanProperty $showAxes = new SimpleBooleanProperty(false);
}