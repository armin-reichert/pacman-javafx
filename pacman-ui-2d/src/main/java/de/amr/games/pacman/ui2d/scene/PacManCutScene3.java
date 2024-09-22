/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManGamePacAnimations;
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
        super.init();
        context.setScoreVisible(true);

        pac = new Pac();
        pac.setAnimations(new PacManGamePacAnimations(renderer.spriteSheet()));

        blinky = Ghost.red();
        blinky.setAnimations(new PacManGameGhostAnimations(renderer.spriteSheet(), blinky.id()));

        frame = -1;
    }

    private void startMusic() {
        int number = context.gameState() == GameState.INTERMISSION_TEST
            ? GameState.INTERMISSION_TEST.getProperty("intermissionTestNumber")
            : context.game().intermissionNumber(context.game().levelNumber());
        GameSounds.playIntermissionSound(number);
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
    public void drawSceneContent() {
        renderer.drawAnimatedEntity(g, pac);
        renderer.drawAnimatedEntity(g, blinky);
    }

    @Override
    protected void drawDebugInfo() {
        Vector2i worldSize = context.worldSize();
        renderer.drawTileGrid(g, worldSize.x(), worldSize.y());
        var text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        renderer.drawText(g, text, Color.YELLOW, Font.font("Sans", 16), t(1), t(5));
    }
}