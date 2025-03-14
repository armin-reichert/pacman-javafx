/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.uilib.FadingPane;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.uilib.Ufx.opaqueColor;

/**
 * @author Armin Reichert
 */
public class PopupLayer extends Pane {

    private final GameContext context;
    private final FadingPane helpPopUp = new FadingPane();
    private final TextFlow signature = new TextFlow();
    private Animation signatureAnimation;

    public PopupLayer(GameContext context, TooFancyCanvasContainer canvas) {
        this.context = context;

        getChildren().addAll(helpPopUp, signature);

        minHeightProperty().bind(canvas.minHeightProperty());
        maxHeightProperty().bind(canvas.maxHeightProperty());
        prefHeightProperty().bind(canvas.prefHeightProperty());
        minWidthProperty().bind(canvas.minWidthProperty());
        maxWidthProperty().bind(canvas.maxWidthProperty());
        prefWidthProperty().bind(canvas.prefWidthProperty());
    }

    public void sign(TooFancyCanvasContainer gameCanvasContainer, Font font, Color color, String... words) {
        signature.setOpacity(0); // invisible initially
        signature.getChildren().clear(); // just in case
        for (String word : words) {
            var text = new Text(word);
            text.setFill(color);
            text.fontProperty().bind(gameCanvasContainer.scalingPy.map(
                scaling -> Font.font(font.getFamily(), scaling.doubleValue() * font.getSize()))
            );
            signature.getChildren().add(text);
        }

        // keep signature centered over canvas
        signature.translateXProperty().bind(Bindings.createDoubleBinding(
            () -> 0.5 * (gameCanvasContainer.getWidth() - signature.getWidth()),
            gameCanvasContainer.scalingPy, gameCanvasContainer.widthProperty()
        ));

        // keep vertical position over intro scene, also when scene gets scaled
        signature.translateYProperty().bind(gameCanvasContainer.scalingPy.map(
            scaling -> scaling.doubleValue() * (gameCanvasContainer.isDecorationEnabled() ? 4*TS : 3*TS)
        ));
    }

    public void showSignature(double delaySec, double fadeInSec, double fadeOutSec) {
        var fadeIn = new FadeTransition(Duration.seconds(fadeInSec), signature);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        var fadeOut = new FadeTransition(Duration.seconds(fadeOutSec), signature);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        signatureAnimation = new SequentialTransition(fadeIn, fadeOut);
        signatureAnimation.setDelay(Duration.seconds(delaySec));
        signatureAnimation.play();
    }

    public void hideSignature() {
        if (signatureAnimation != null) {
            signatureAnimation.stop();
            signature.setOpacity(0);
        }
    }

    public void showHelp(double scaling) {
        Color bgColor = Color.web(context.gameVariant() == GameVariant.MS_PACMAN
            ? Arcade.Palette.RED : Arcade.Palette.BLUE);
        var font = context.assets().font("font.monospaced", Math.max(6, 14 * scaling));
        var helpPane = HelpInfo.build(context).createPane(opaqueColor(bgColor, 0.8), font);
        helpPopUp.setTranslateX(10 * scaling);
        helpPopUp.setTranslateY(30 * scaling);
        helpPopUp.setContent(helpPane);
        helpPopUp.show(Duration.seconds(1.5));
    }
}