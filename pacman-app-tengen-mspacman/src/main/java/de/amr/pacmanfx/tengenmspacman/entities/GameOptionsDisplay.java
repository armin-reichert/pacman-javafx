package de.amr.pacmanfx.tengenmspacman.entities;

import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.tengenmspacman.entities.gameoptionsdisplay.GameOptionsDataComp;

public class GameOptionsDisplay extends GameEntity implements Renderable {

    public GameOptionsDisplay() {
        setComp(GameOptionsDataComp.class, new GameOptionsDataComp());
    }

    public GameOptionsDataComp options() {
        return reqComp(GameOptionsDataComp.class);
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.HUD;
    }
}
