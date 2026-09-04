package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

public class CreditDataComp implements GameEntityComp {

    private int credit;

    public int credit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }
}
