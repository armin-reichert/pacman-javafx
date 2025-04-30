/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.Globals.THE_ASSETS;
import static de.amr.games.pacman.ui.Globals.THE_SOUND;

/**
 * @author Armin Reichert
 */
public class ArcadeMsPacMan_StartScene extends GameScene2D {

    @Override
    public void bindActions() {
        bindDefaultArcadeActions();
    }

    @Override
    public void doInit() {
        game().scoreVisibleProperty().set(true);
    }

    @Override
    public void update() {
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        Font font = fontPy.get();
        Font font6 = THE_ASSETS.arcadeFontAtSize(scaled(6));
        gr.setScaling(scaling());
        gr.fillCanvas(backgroundColor());
        if (game().isScoreVisible()) {
            gr.drawScores(game(), Color.web(Arcade.Palette.WHITE), font);
        }
        GameSpriteSheet spriteSheet = gr.spriteSheet();
        Color orange = Color.web(Arcade.Palette.ORANGE), red = Color.web(Arcade.Palette.RED), white = Color.web(Arcade.Palette.WHITE);
        gr.fillTextAtScaledTilePosition("PUSH START BUTTON", orange, font, 6, 16);
        gr.fillTextAtScaledTilePosition("1 PLAYER ONLY", orange, font, 8, 18);
        gr.fillTextAtScaledTilePosition("ADDITIONAL    AT 10000", orange, font, 2, 25);
        gr.drawSpriteScaled(spriteSheet.livesCounterSprite(), tiles_to_px(13), tiles_to_px(23) + 1); //TODO check this
        gr.fillTextAtScaledTilePosition("PTS", orange, font6, 25, 25);
        if (gr instanceof ArcadeMsPacMan_GameRenderer r) {
            r.drawMidwayCopyright(6, 28, red, font);
        }
        gr.fillTextAtScaledPosition("CREDIT %2d".formatted(THE_COIN_MECHANISM.numCoins()), white, font,
            tiles_to_px(2), sizeInPx().y() - 2);
        gr.drawLevelCounter(game().levelCounter(), sizeInPx());
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        THE_SOUND.playInsertCoinSound();
    }
}