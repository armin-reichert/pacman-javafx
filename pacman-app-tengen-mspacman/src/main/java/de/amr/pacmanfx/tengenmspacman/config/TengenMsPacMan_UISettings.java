/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.config;

import de.amr.pacmanfx.tengenmspacman.gamescene.SceneDisplay;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TengenMsPacMan_UISettings {

    private static class SingletonHolder {
        static final TengenMsPacMan_UISettings SINGLETON = new TengenMsPacMan_UISettings();
    }

    public static TengenMsPacMan_UISettings instance() {
        return SingletonHolder.SINGLETON;
    }

    private TengenMsPacMan_UISettings() {}

    public final BooleanProperty joypadBindingsDisplayed = new SimpleBooleanProperty(false);

    public final ObjectProperty<SceneDisplay> playSceneDisplay = new SimpleObjectProperty<>(SceneDisplay.SCROLLING);
}
