/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.GameUI;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.uilib.SpriteAnimation;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.model.actors.ActorAnimations.*;
import static de.amr.games.pacman.ui.Globals.THE_UI;

/**
 * @author Armin Reichert
 */
public class CutScene2 extends GameScene2D {

    static final short ANIMATION_START = 120;

    private int frame;
    private Pac pac;
    private Ghost blinky;
    private SpriteAnimation blinkyNormal;
    private SpriteAnimation blinkyStretching;
    private SpriteAnimation blinkyDamaged;
    private MediaPlayer music;

    @Override
    public void bindGameActions() {}

    @Override
    public void doInit() {
        game().setScoreVisible(true);

        pac = new Pac();
        blinky = ArcadePacMan_GameModel.blinky();
        blinky.setSpeed(0);
        blinky.hide();

        music = THE_UI.sound().makeSound("intermission");
        music.setCycleCount(1);

        var spriteSheet = (ArcadePacMan_SpriteSheet) THE_UI.configurations().current().spriteSheet();
        pac.setAnimations(new PacAnimations(spriteSheet));

        var blinkyAnimations = new GhostAnimations(spriteSheet, blinky.id());
        blinkyNormal = blinkyAnimations.animation(ANIM_GHOST_NORMAL);
        blinkyStretching = blinkyAnimations.animation(ANIM_BLINKY_STRETCHED);
        blinkyDamaged = blinkyAnimations.animation(GhostAnimations.ANIM_BLINKY_DAMAGED);
        blinky.setAnimations(blinkyAnimations);

        frame = -1;
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        ++frame;
        if (frame >= ANIMATION_START) {
            pac.move();
            blinky.move();
        }
        switch (frame) {
            case ANIMATION_START -> music.play();
            case ANIMATION_START + 1 -> blinkyStretching.setFrameIndex(0); // Show nail
            case ANIMATION_START + 25 -> {
                pac.placeAtTile(28, 20, 0, 0);
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.15f);
                pac.selectAnimation(ANIM_PAC_MUNCHING);
                pac.startAnimation();
                pac.show();
            }
            case ANIMATION_START + 111 -> {
                blinky.placeAtTile(28, 20, -3, 0);
                blinky.setMoveAndWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.selectAnimation(ANIM_GHOST_NORMAL);
                blinky.startAnimation();
                blinky.show();
            }
            case ANIMATION_START + 194 -> {
                blinky.setSpeed(0.09f);
                blinkyNormal.setFrameTicks(32);
            }
            case ANIMATION_START + 198,
                 ANIMATION_START + 226,
                 ANIMATION_START + 248 -> blinkyStretching.nextFrame(); // Stretched S-M-L
            case ANIMATION_START + 328 -> {
                blinky.setSpeed(0);
                blinkyStretching.nextFrame(); // Rapture
            }
            case ANIMATION_START + 329 -> blinky.selectAnimation(GhostAnimations.ANIM_BLINKY_DAMAGED); // Eyes up
            case ANIMATION_START + 389 -> blinkyDamaged.nextFrame(); // Eyes right-down
            case ANIMATION_START + 508 -> {
                blinky.setVisible(false);
                THE_GAME_CONTROLLER.terminateCurrentState();
            }
            default -> {}
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        gr.setScaling(scaling());
        gr.clearCanvas(backgroundColor());
        if (game().isScoreVisible()) {
            Font font = THE_UI.assets().scaledArcadeFont(scaled(TS));
            gr.drawScores(Color.web(Arcade.Palette.WHITE), font);
        }
        gr.drawSpriteScaled(blinkyStretching.currentSprite(), tiles2Px(14), tiles2Px(19) + 3);
        gr.drawAnimatedActor(blinky);
        gr.drawAnimatedActor(pac);
        gr.drawLevelCounter(sizeInPx().x() - 4 * TS, sizeInPx().y() - 2 * TS);
    }

    @Override
    public void drawDebugInfo() {
        super.drawDebugInfo();
        String text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        gr.drawText(text, Color.YELLOW, GameUI.DEBUG_TEXT_FONT, tiles2Px(1), tiles2Px(5));
    }
}