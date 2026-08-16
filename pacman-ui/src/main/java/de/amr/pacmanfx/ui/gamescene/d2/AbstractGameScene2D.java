/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ScrollEvent;

import java.util.Optional;

/**
 * Base class for all 2D game scenes.
 */
public abstract class AbstractGameScene2D extends AbstractGameScene {

    private Rendering2DSupport rendering2D;

    public AbstractGameScene2D(GameAppContext appContext) {
        super(appContext);
        rendering2D = new Rendering2DSupport();
    }

    public Rendering2DSupport rendering2D() {
        return rendering2D;
    }

    @Override
    public void onBeforeEmbedded() {
        //TODO remove this hook method
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {
        // Used only by very few subclasses
    }

    @Override
    public Optional<ContextMenu> optContextMenu() {
        return Optional.empty();
    }

    @Override
    public void dispose() {
        if (rendering2D != null) {
            rendering2D.dispose();
        }
    }

    /**
     * Hook called when entering this 2D scene from a 3D scene.
     * Subclasses may override to adjust state or transitions.
     */
    public void onEnteredFrom3DScene() {}

    /**
     * If a 3D-variant of this game scene is active when the game level gets created, this method has not yet been called,
     * but it gets called when the 3D->2D scene switch happens.
     */
    public void acceptGameLevel(GameSession session, GameLevel level) {
        if (rendering2D != null) {
            final Vector2i size = level.worldMap().terrainLayer().sizeInPixel();
            rendering2D.unscaledWidthProperty().set(size.x());
            rendering2D.unscaledHeightProperty().set(size.y());
        }
    }
}
