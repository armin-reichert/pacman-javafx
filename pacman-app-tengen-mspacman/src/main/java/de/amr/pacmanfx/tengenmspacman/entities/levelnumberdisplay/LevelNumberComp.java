package de.amr.pacmanfx.tengenmspacman.entities.levelnumberdisplay;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

public class LevelNumberComp implements GameEntityComp {

    private int number;

    public int number() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
