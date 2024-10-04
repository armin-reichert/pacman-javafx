/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.variant.pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.lib.Globals.v2i;

/**
 * @author Armin Reichert
 */
public class PacManCutScene3 extends GameScene2D {

    static final short ANIMATION_START = 120;

    private int frame;
    private Pac pac;
    private Ghost blinky;

    @Override
    public boolean isCreditVisible() {
        return !context.game().hasCredit();
    }

    @Override
    public void init() {
        context.setScoreVisible(true);

        pac = new Pac();
        pac.setAnimations(new PacManGamePacAnimations(context.spriteSheet()));

        blinky = Ghost.blinky();
        blinky.setAnimations(new PacManGameGhostAnimations(context.spriteSheet(), blinky.id()));

        frame = -1;
    }

    @Override
    public void end() {
    }

    @Override
    public void update() {
        ++frame;
        if (frame >= ANIMATION_START) {
            pac.move();
            blinky.move();
        }
        switch (frame) {
            case ANIMATION_START -> {
                startMusic();
                pac.centerOverTile(v2i(29, 20));
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.25f);
                pac.show();
                pac.selectAnimation(Pac.ANIM_MUNCHING);
                pac.startAnimation();
                blinky.centerOverTile(v2i(35, 20));
                blinky.setMoveAndWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.show();
                blinky.selectAnimation(Ghost.ANIM_BLINKY_PATCHED);
                blinky.startAnimation();
            }
            case ANIMATION_START + 400 -> {
                blinky.centerOverTile(v2i(-1, 20));
                blinky.setMoveAndWishDir(Direction.RIGHT);
                blinky.selectAnimation(Ghost.ANIM_BLINKY_NAKED);
                blinky.startAnimation();
            }
            case ANIMATION_START + 700 -> context.gameState().timer().expire();
            default -> {}
        }
    }

    @Override
    public void drawSceneContent(GameWorldRenderer renderer) {
        renderer.drawAnimatedEntity(pac);
        renderer.drawAnimatedEntity(blinky);
    }

    @Override
    protected void drawDebugInfo(GameWorldRenderer renderer) {
        renderer.drawTileGrid(context.worldSizeTilesOrDefault());
        var text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        renderer.drawText(text, Color.YELLOW, Font.font("Sans", 16), t(1), t(5));
    }

    private void startMusic() {
        int number = context.gameState() == GameState.INTERMISSION_TEST
            ? GameState.INTERMISSION_TEST.getProperty("intermissionTestNumber")
            : context.game().intermissionNumber(context.game().levelNumber());
        context.sounds().playIntermissionSound(number);
    }
}