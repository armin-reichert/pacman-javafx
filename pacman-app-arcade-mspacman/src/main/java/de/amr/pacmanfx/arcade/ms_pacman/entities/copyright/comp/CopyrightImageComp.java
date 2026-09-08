package de.amr.pacmanfx.arcade.ms_pacman.entities.copyright.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;
import javafx.scene.image.Image;

public class CopyrightImageComp implements GameEntityComp {

    private Image image;

    public Image image() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
