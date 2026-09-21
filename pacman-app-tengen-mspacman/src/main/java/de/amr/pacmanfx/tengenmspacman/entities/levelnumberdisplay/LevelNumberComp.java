package de.amr.pacmanfx.tengenmspacman.entities.levelnumberdisplay;

import de.amr.basics.ui.ecs.GameEntityComp;

public class LevelNumberComp implements GameEntityComp {

    private int number;

    public int number() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
