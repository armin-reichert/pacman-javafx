package de.amr.pacmanfx.arcade.ms_pacman.entities;

import de.amr.pacmanfx.arcade.ms_pacman.entities.copyright.comp.CopyrightImageComp;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;

public class Copyright extends GameEntity implements Renderable {

    public Copyright() {
        setComp(CopyrightImageComp.class, new CopyrightImageComp());
    }

    public CopyrightImageComp image() {
        return reqComp(CopyrightImageComp.class);
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.PROPS;
    }
}
