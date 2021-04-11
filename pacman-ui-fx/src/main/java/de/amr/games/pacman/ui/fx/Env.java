package de.amr.games.pacman.ui.fx;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.DrawMode;

public class Env {

	public static SimpleBooleanProperty $timeMeasured = new SimpleBooleanProperty(false);
	public static SimpleBooleanProperty $paused = new SimpleBooleanProperty(false);
	public static SimpleIntegerProperty $slowDown = new SimpleIntegerProperty(1);
	public static SimpleBooleanProperty $hudVisible = new SimpleBooleanProperty(false);
	public static SimpleObjectProperty<DrawMode> $drawMode = new SimpleObjectProperty<DrawMode>(DrawMode.FILL);
	public static SimpleBooleanProperty $use3DScenes = new SimpleBooleanProperty(true);
	public static SimpleBooleanProperty $axesVisible = new SimpleBooleanProperty(false);
}