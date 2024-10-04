package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.tengen.MsPacManTengenGame;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameAction2D;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.util.KeyInput;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.TS;

public class TengenSettingsScene extends GameScene2D {

    @Override
    public void init() {
    }

    @Override
    public void end() {
    }

    @Override
    public void update() {
    }

    @Override
    protected void drawSceneContent(GameWorldRenderer renderer) {
        MsPacManTengenGame tengenGame = (MsPacManTengenGame) context.game();
        int keyX = 2*TS;
        int valueX = 19*TS;
        Color labelColor = Color.YELLOW;
        Color valueColor = Color.WHITE;
        Font font = renderer.scaledArcadeFont(TS);
        renderer.drawText("MS PAC-MAN OPTIONS", labelColor, font, 6*TS, 6*TS);
        renderer.drawText("MAZE SELECTION : ", labelColor, font, keyX, 10*TS);
        renderer.drawText(tengenGame.mapCategory().name(), valueColor, font, valueX, 10*TS);
        renderer.drawText("MOVE ARROW WITH CURSOR KEYS", labelColor, font, keyX, 30*TS);
        renderer.drawText("CHOOSE OPTIONS WITH ENTER", labelColor, font, keyX, 31*TS);
        renderer.drawText("PRESS SPACE TO START GAME", labelColor, font, keyX, 32*TS);

    }

    @Override
    public boolean isCreditVisible() {
        return false;
    }

    @Override
    public void handleInput() {
        if (Keyboard.pressed(KeyCode.ENTER)) {
            // currently, map category is the only value that can be changed
            MsPacManTengenGame tengenGame = (MsPacManTengenGame) context.game();
            MsPacManTengenGame.MapCategory category = tengenGame.mapCategory();
            int ord = category.ordinal();
            if (ord == MsPacManTengenGame.MapCategory.values().length - 1) {
                tengenGame.setMapCategory(MsPacManTengenGame.MapCategory.values()[0]);
            } else {
                tengenGame.setMapCategory(MsPacManTengenGame.MapCategory.values()[ord + 1]);
            }
        } else if (Keyboard.pressed(KeyCode.SPACE)) {
            context.sounds().stopAll();
            context.game().insertCoin();
            context.gameController().changeState(GameState.READY);
        }
    }
}
