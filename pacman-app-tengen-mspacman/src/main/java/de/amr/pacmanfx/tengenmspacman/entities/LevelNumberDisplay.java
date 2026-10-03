package de.amr.pacmanfx.tengenmspacman.entities;

import de.amr.basics.ecs.GameEntity;
import de.amr.pacmanfx.tengenmspacman.entities.levelnumberdisplay.LevelNumberComp;

public class LevelNumberDisplay extends GameEntity {

    public LevelNumberDisplay() {
        setComponent(LevelNumberComp.class, new LevelNumberComp());
    }

    public LevelNumberComp levelNumber() {
        return assertComponent(LevelNumberComp.class);
    }
}
