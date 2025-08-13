/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel.createGhost;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel.createPac;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;

/**
 * Third cut scene in Arcade Pac-Man game:<br>
 * Red ghost in damaged dress chases Pac-Man from right to left over the screen.
 * After they have disappeared, a naked, shaking ghost runs from left to right over the screen.
 */
public class ArcadePacMan_CutScene3 extends GameScene2D {

    private static final String MUSIC_ID = "audio.intermission";
    static final short ANIMATION_START = 120;

    private int frame;
    private Pac pac;
    private Ghost blinky;

    public ArcadePacMan_CutScene3(GameUI ui) {
        super(ui);
    }
    
    @Override
    public void doInit() {
        frame = -1;

        pac = createPac(null);
        pac.setAnimations(ui.currentConfig().createPacAnimations(pac));

        blinky = createGhost(null, RED_GHOST_SHADOW);
        blinky.setAnimations(ui.currentConfig().createGhostAnimations(blinky));

        actorsInZOrder.add(pac);
        actorsInZOrder.add(blinky);

        gameContext().game().hudData().credit(false).score(true).levelCounter(true).livesCounter(false);
    }

    @Override
    protected void doEnd() {
        ui.soundManager().stop(MUSIC_ID);
        Logger.info("{} ends", getClass().getSimpleName());
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
                ui.soundManager().play(MUSIC_ID, 2);
                pac.placeAtTile(29, 20);
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.25f);
                pac.show();
                pac.playAnimation(ANIM_PAC_MUNCHING);
                blinky.placeAtTile(35, 20);
                blinky.setMoveDir(Direction.LEFT);
                blinky.setWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.show();
                blinky.playAnimation(ANIM_BLINKY_PATCHED);
            }
            case ANIMATION_START + 400 -> {
                blinky.placeAtTile(-1, 20);
                blinky.setMoveDir(Direction.RIGHT);
                blinky.setWishDir(Direction.RIGHT);
                blinky.playAnimation(ANIM_BLINKY_NAKED);
            }
            case ANIMATION_START + 700 -> gameContext().gameController().letCurrentGameStateExpire();
            default -> {}
        }
    }

    @Override
    public Vector2f sizeInPx() { return ARCADE_MAP_SIZE_IN_PIXELS; }

    @Override
    public void drawSceneContent() {
        gameRenderer.drawActors(actorsInZOrder);
    }

    @Override
    protected void drawDebugInfo() {
        super.drawDebugInfo();
        String text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        gameRenderer.fillTextAtScaledPosition(text, debugTextFill, debugTextFont, TS(1), TS(5));
    }
}