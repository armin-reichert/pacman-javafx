package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import javafx.scene.Group;
import javafx.scene.paint.Color;

public class MsPacManBody extends Group implements Disposable {

    private PacBody body;
    private MsPacManFemaleParts femaleParts;

    public MsPacManBody(PacManModel3DRepository model3DRepository,
                        double size,
                        Color headColor, Color eyesColor, Color palateColor,
                        Color hairBowColor, Color hairBowPearlsColor, Color boobsColor)
    {
        body = model3DRepository.createPacBody(size, headColor, eyesColor, palateColor);
        femaleParts = model3DRepository.createFemaleBodyParts(size, hairBowColor, hairBowPearlsColor, boobsColor);
        getChildren().addAll(body, femaleParts);
    }

    @Override
    public void dispose() {
        getChildren().clear();
        body.dispose();
        body = null;
        femaleParts.dispose();
        femaleParts = null;
    }
}
