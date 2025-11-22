package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_IntroScene;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.Marquee;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2DRenderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_IntroScene.*;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;

public class ArcadeMsPacMan_IntroScene_Renderer extends GameScene2DRenderer {

    private final ActorRenderer actorRenderer;

    public ArcadeMsPacMan_IntroScene_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);
        final GameUI_Config uiConfig = scene.ui().currentConfig();
        actorRenderer = configureRendererForGameScene(uiConfig.createActorRenderer(canvas), scene);
        createDefaultDebugInfoRenderer(canvas, uiConfig.spriteSheet());
    }

    public void draw() {
        final ArcadeMsPacMan_IntroScene introScene = scene();

        ctx.setFont(arcadeFont8());
        fillText(TITLE, ARCADE_ORANGE, TITLE_X, TITLE_Y);
        drawMarquee(introScene.marquee);

        introScene.ghosts.forEach(actorRenderer::drawActor);
        actorRenderer.drawActor(introScene.msPacMan);

        switch (introScene.sceneController.state()) {
            case GHOSTS_MARCHING_IN -> {
                String ghostName = GHOST_NAMES[introScene.presentedGhostCharacter];
                Color ghostColor = GHOST_COLORS[introScene.presentedGhostCharacter];
                if (introScene.presentedGhostCharacter == RED_GHOST_SHADOW) {
                    fillText("WITH", ARCADE_WHITE, TITLE_X, TOP_Y + TS(3));
                }
                double x = TITLE_X + (ghostName.length() < 4 ? TS(4) : TS(3));
                double y = TOP_Y + TS(6);
                fillText(ghostName, ghostColor, x, y);
            }
            case MS_PACMAN_MARCHING_IN, READY_TO_PLAY -> {
                fillText("STARRING", ARCADE_WHITE, TITLE_X, TOP_Y + TS(3));
                fillText("MS PAC-MAN", ARCADE_YELLOW, TITLE_X, TOP_Y + TS(6));
            }
        }
        drawMidwayCopyright(introScene.ui().currentConfig().assets().image("logo.midway"), TS(6), TS(28));

        if (scene().debugInfoVisible()) {
            debugInfoRenderer.draw();
        }
    }

    /**
     * 6 of the 96 light bulbs are bright in each frame, shifting counter-clockwise every tick.
     * <p>
     * The bulbs on the left border however are switched off every second frame. This is
     * probably a bug in the original Arcade game.
     * </p>
     */
    public void drawMarquee(Marquee marquee) {
        long tick = marquee.timer().tickCount();
        ctx.setFill(marquee.bulbOffColor());
        for (int bulbIndex = 0; bulbIndex < marquee.totalBulbCount(); ++bulbIndex) {
            drawMarqueeBulb(marquee, bulbIndex);
        }
        int firstBrightIndex = (int) (tick % marquee.totalBulbCount());
        ctx.setFill(marquee.bulbOnColor());
        for (int i = 0; i < marquee.brightBulbsCount(); ++i) {
            drawMarqueeBulb(marquee, (firstBrightIndex + i * marquee.brightBulbsDistance()) % marquee.totalBulbCount());
        }
        // simulate bug from original Arcade game
        ctx.setFill(marquee.bulbOffColor());
        for (int bulbIndex = 81; bulbIndex < marquee.totalBulbCount(); bulbIndex += 2) {
            drawMarqueeBulb(marquee, bulbIndex);
        }
    }

    private void drawMarqueeBulb(Marquee marquee, int bulbIndex) {
        final double minX = marquee.x(), minY = marquee.y();
        final double maxX = marquee.x() + marquee.width(), maxY = marquee.y() + marquee.height();
        double x, y;
        if (bulbIndex <= 33) { // lower edge left-to-right
            x = minX + 4 * bulbIndex;
            y = maxY;
        }
        else if (bulbIndex <= 48) { // right edge bottom-to-top
            x = maxX;
            y = 4 * (70 - bulbIndex);
        }
        else if (bulbIndex <= 81) { // upper edge right-to-left
            x = 4 * (marquee.totalBulbCount() - bulbIndex);
            y = minY;
        }
        else { // left edge top-to-bottom
            x = minX;
            y = 4 * (bulbIndex - 59);
        }
        ctx.fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }

    public void drawMidwayCopyright(Image logo, double x, double y) {
        ctx.drawImage(logo, scaled(x), scaled(y + 2), scaled(TS(4) - 2), scaled(TS(4)));
        ctx.setFont(arcadeFont8());
        ctx.setFill(ARCADE_RED);
        ctx.fillText("©", scaled(x + TS(5)), scaled(y + TS(2)) + 2);
        ctx.fillText("MIDWAY MFG CO", scaled(x + TS(7)), scaled(y + TS(2)));
        ctx.fillText("1980/1981", scaled(x + TS(8)), scaled(y + TS(4)));
    }
}
