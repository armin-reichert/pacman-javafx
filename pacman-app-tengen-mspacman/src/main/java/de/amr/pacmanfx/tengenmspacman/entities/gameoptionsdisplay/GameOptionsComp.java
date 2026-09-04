package de.amr.pacmanfx.tengenmspacman.entities.gameoptionsdisplay;

import de.amr.pacmanfx.core.ecs.GameEntityComp;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.tengenmspacman.model.Difficulty;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;

public class GameOptionsComp implements GameEntityComp {

    private MapCategory mapCategory;

    private Difficulty difficulty;

    private BoosterMode boosterMode;

    public GameOptionsComp() {
    }

    public MapCategory mapCategory() {
        return mapCategory;
    }

    public void setMapCategory(MapCategory mapCategory) {
        this.mapCategory = mapCategory;
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public BoosterMode boosterMode() {
        return boosterMode;
    }

    public void setBoosterMode(BoosterMode boosterMode) {
        this.boosterMode = boosterMode;
    }
}
