package de.amr.games.pacman.ui.fx;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.DrawMode;

/**
 * Globally available properties.
 * 
 * @author Armin Reichert
 */
public class Env {

	public static final ResourceBundle MESSAGES = ResourceBundle.getBundle("/common/messages");

	public static String message(String pattern, Object... args) {
		return MessageFormat.format(MESSAGES.getString(pattern), args);
	}

	public static final String APP_ICON_PATH = "/pacman/graphics/pacman.png";

	public static final BooleanProperty $axesVisible = new SimpleBooleanProperty(false);
	public static final ObjectProperty<DrawMode> $drawMode3D = new SimpleObjectProperty<DrawMode>(DrawMode.FILL);
	public static final IntegerProperty $fps = new SimpleIntegerProperty();
	public static final BooleanProperty $isHUDVisible = new SimpleBooleanProperty(false);
	public static final BooleanProperty $isTimeMeasured = new SimpleBooleanProperty(false);
	public static final IntegerProperty $mazeResolution = new SimpleIntegerProperty(8);
	public static final DoubleProperty $mazeWallHeight = new SimpleDoubleProperty(3.5);
	public static final IntegerProperty $totalTicks = new SimpleIntegerProperty();
	public static final IntegerProperty $slowDown = new SimpleIntegerProperty(1);
	public static final BooleanProperty $paused = new SimpleBooleanProperty(false);
	public static final BooleanProperty $use3DScenes = new SimpleBooleanProperty(true);
}