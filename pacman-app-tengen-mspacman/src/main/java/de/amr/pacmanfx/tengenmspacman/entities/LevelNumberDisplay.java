package de.amr.pacmanfx.tengenmspacman.entities;

import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.tengenmspacman.entities.levelnumberdisplay.LevelNumberComp;

public class LevelNumberDisplay extends GameEntity implements Renderable {

    public LevelNumberDisplay() {
        setComp(LevelNumberComp.class, new LevelNumberComp());
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.HUD;
    }

    public LevelNumberComp levelNumber() {
        return reqComp(LevelNumberComp.class);
    }
}
