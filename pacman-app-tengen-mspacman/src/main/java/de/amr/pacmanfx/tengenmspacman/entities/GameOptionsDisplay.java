package de.amr.pacmanfx.tengenmspacman.entities;

import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.ecs.GameEntity;
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
