package de.amr.pacmanfx.tengenmspacman.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.tengenmspacman.entities.levelnumberdisplay.LevelNumberComp;

public class LevelNumberDisplay extends GameEntity {

    public LevelNumberDisplay() {
        setComp(LevelNumberComp.class, new LevelNumberComp());
    }

    public LevelNumberComp levelNumber() {
        return reqComp(LevelNumberComp.class);
    }
}
