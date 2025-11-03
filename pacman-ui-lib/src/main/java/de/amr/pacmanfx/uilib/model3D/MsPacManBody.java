package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import javafx.scene.Group;

public class MsPacManBody extends Group implements Disposable {

    private PacBody body;
    private MsPacManFemaleParts femaleParts;

    public MsPacManBody(PacBody body, MsPacManFemaleParts femaleParts) {
        this.body = body;
        this.femaleParts = femaleParts;
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
