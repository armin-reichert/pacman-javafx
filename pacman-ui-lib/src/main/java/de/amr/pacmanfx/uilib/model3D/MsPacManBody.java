package de.amr.pacmanfx.uilib.model3D;

import javafx.scene.Group;
import javafx.scene.paint.Color;

public class MsPacManBody extends Group implements Destroyable {

    private PacBody body;
    private MsPacManFemaleParts femaleParts;

    public MsPacManBody(Model3DRepository model3DRepository,
        double size,
        Color headColor, Color eyesColor, Color palateColor,
        Color hairBowColor, Color hairBowPearlsColor, Color boobsColor)
    {
        body = model3DRepository.createPacBody(size, headColor, eyesColor, palateColor);
        femaleParts = model3DRepository.createFemaleBodyParts(size, hairBowColor, hairBowPearlsColor, boobsColor);
        getChildren().addAll(body, femaleParts);
    }

    @Override
    public void destroy() {
        getChildren().clear();
        body.destroy();
        body = null;
        femaleParts.destroy();
        femaleParts = null;
    }
}
