package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.tengenmspacman.scenes.SceneDisplayMode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TengenMsPacMan_UISettings {

    public final BooleanProperty propertyJoypadBindingsDisplayed = new SimpleBooleanProperty(false);

    public final ObjectProperty<SceneDisplayMode> propertyPlaySceneDisplayMode = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);
}
