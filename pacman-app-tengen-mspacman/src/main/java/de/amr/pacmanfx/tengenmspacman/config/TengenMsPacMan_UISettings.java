/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.config;

import de.amr.pacmanfx.tengenmspacman.gamescene.SceneDisplay;
import de.amr.pacmanfx.game.PacManGamesCollection;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TengenMsPacMan_UISettings {

    public TengenMsPacMan_UISettings(GameActionContext actionContext) {}

    public final BooleanProperty joypadBindingsDisplayed = new SimpleBooleanProperty(false);

    public final ObjectProperty<SceneDisplay> playSceneDisplay = new SimpleObjectProperty<>(SceneDisplay.SCROLLING);
}
