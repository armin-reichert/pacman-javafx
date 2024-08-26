/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.util.CanvasLayoutPane;
import de.amr.games.pacman.ui2d.util.DecoratedCanvas;
import de.amr.games.pacman.ui2d.util.FadingPane;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import static de.amr.games.pacman.ui2d.GameParameters.PY_DEBUG_INFO;
import static de.amr.games.pacman.ui2d.util.Ufx.opaqueColor;

/**
 * @author Armin Reichert
 */
public class PopupLayer extends Pane {

    private final GameContext context;
    private final FadingPane helpPopUp = new FadingPane();
    private final Signature signature = new Signature();

    public PopupLayer(GameContext context, DecoratedCanvas canvas) {
        this.context = context;

        getChildren().addAll(helpPopUp, signature);

        minHeightProperty().bind(canvas.minHeightProperty());
        maxHeightProperty().bind(canvas.maxHeightProperty());
        prefHeightProperty().bind(canvas.prefHeightProperty());
        minWidthProperty().bind(canvas.minWidthProperty());
        maxWidthProperty().bind(canvas.maxWidthProperty());
        prefWidthProperty().bind(canvas.prefWidthProperty());

        borderProperty().bind(Bindings.createObjectBinding(
            () -> PY_DEBUG_INFO.get() && !context.currentGameSceneIs(GameSceneID.PLAY_SCENE_3D)
                ? Ufx.border(Color.GREENYELLOW, 3) : null,
            PY_DEBUG_INFO, context.gameSceneProperty()
        ));
        mouseTransparentProperty().bind(PY_DEBUG_INFO);
    }

    public Signature signature() {
        return signature;
    }

    public void prepareSignature(CanvasLayoutPane canvasPane, Font font, String... words) {
        signature.setWords(words);
        signature.fontPy.bind(Bindings.createObjectBinding(
            () -> Font.font(font.getFamily(), canvasPane.scaling() * font.getSize()),
            canvasPane.scalingPy
        ));
        // keep centered over canvas container
        signature.translateXProperty().bind(Bindings.createDoubleBinding(
            () -> 0.5 * (canvasPane.decoratedCanvas().getWidth() - signature.getWidth()),
            canvasPane.scalingPy, canvasPane.decoratedCanvas().widthProperty()
        ));
        // keep at vertical position over intro scene
        signature.translateYProperty().bind(Bindings.createDoubleBinding(
            () -> canvasPane.scaling() * 30,
            canvasPane.scalingPy, canvasPane.decoratedCanvas().heightProperty()
        ));
    }

    public void showHelp(double scaling) {
        var bgColor = context.game().variant() == GameVariant.MS_PACMAN
            ? context.assets().color("palette.red")
            : context.assets().color("palette.blue");
        var font = context.assets().font("font.monospaced", Math.max(6, 14 * scaling));
        var helpPane = HelpInfo.build(context).createPane(opaqueColor(bgColor, 0.8), font);
        helpPopUp.setTranslateX(10 * scaling);
        helpPopUp.setTranslateY(30 * scaling);
        helpPopUp.setContent(helpPane);
        helpPopUp.show(Duration.seconds(1.5));
    }
}