package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_OptionsScene;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Properties.PROPERTY_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;
import static de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_OptionsScene.*;

public class TengenMsPacMan_OptionsScene_Renderer extends TengenMsPacMan_CommonSceneRenderer {

    private static final int COL_ARROW = 2 * TS;
    private static final int COL_LABEL = 4 * TS;
    private static final int COL_COLON = 19 * TS;
    private static final int COL_VALUE = 21  * TS;

    private static final Color NES_YELLOW = nesColor(0x28);
    private static final Color NES_WHITE = nesColor(0x20);

    public TengenMsPacMan_OptionsScene_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);

        createDefaultDebugInfoRenderer(scene, canvas, spriteSheet);
    }
    
    public void draw(GameScene2D scene) {
        clearCanvas();

        final TengenMsPacMan_OptionsScene optionsScene = (TengenMsPacMan_OptionsScene) scene;
        final TengenMsPacMan_GameModel game = optionsScene.context().game();

        if (optionsScene.initialDelay > 0) return;

        ctx.setFont(arcadeFont8());
        if (PROPERTY_JOYPAD_BINDINGS_DISPLAYED.get()) {
            Joypad joypad = optionsScene.ui().joypad();
            drawJoypadKeyBinding(joypad.currentKeyBinding());
        }

        drawHorizontalBar(nesColor(0x20), nesColor(0x21), optionsScene.sizeInPx().x(), TS, 20);

        float y = 48;
        fillText("MS PAC-MAN OPTIONS", NES_YELLOW, COL_LABEL + 3 * TS, 48);

        y += TS(3);
        // Players (not implemented)
        drawMarkerIfSelected(optionsScene, OPTION_PLAYERS, y, arcadeFont8());
        fillText("TYPE", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_LABEL + 4 * TS + 4, y);
        // grey out
        fillText("1 PLAYER", nesColor(0x10), COL_LABEL + 6 * TS, y);

        y += TS(3);
        // Pac-Booster
        drawMarkerIfSelected(optionsScene, OPTION_PAC_BOOSTER, y, arcadeFont8());
        fillText("PAC BOOSTER", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        String pacBoosterText = switch (game.pacBooster()) {
            case OFF -> "OFF";
            case ALWAYS_ON -> "ALWAYS ON";
            case USE_A_OR_B -> "USE A OR B";
        };
        fillText(pacBoosterText, NES_WHITE, COL_VALUE, y);

        y += TS(3);
        // Game difficulty
        drawMarkerIfSelected(optionsScene, OPTION_DIFFICULTY, y, arcadeFont8());
        fillText("GAME DIFFICULTY", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        fillText(game.difficulty().name(), NES_WHITE, COL_VALUE, y);

        y += TS(3);
        // Maze (type) selection
        drawMarkerIfSelected(optionsScene, OPTION_MAZE_SELECTION, y, arcadeFont8());
        fillText("MAZE SELECTION", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        fillText(game.mapCategory().name(), NES_WHITE, COL_VALUE, y);

        y += TS(3);
        // Starting level number
        drawMarkerIfSelected(optionsScene, OPTION_STARTING_LEVEL, y, arcadeFont8());
        fillText("STARTING LEVEL", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        fillText(String.valueOf(game.startLevelNumber()), NES_WHITE, COL_VALUE, y);
        final int numContinues = game.numContinues();
        if (numContinues < 4) {
            var spriteSheet = (TengenMsPacMan_SpriteSheet) optionsScene.ui().currentConfig().spriteSheet();
            RectShort continuesSprite = spriteSheet.sprite(switch (numContinues) {
                case 0 -> SpriteID.CONTINUES_0;
                case 1 -> SpriteID.CONTINUES_1;
                case 2 -> SpriteID.CONTINUES_2;
                case 3 -> SpriteID.CONTINUES_3;
                default -> throw new IllegalArgumentException("Illegal number of continues: " + numContinues);
            });
            drawSprite(continuesSprite, COL_VALUE + 3 * TS, y - 8, true);
        }

        y += TS(3);
        fillText("MOVE ARROW WITH JOYPAD",      NES_YELLOW, TS(4), y);

        y += TS(1);
        fillText("CHOOSE OPTIONS WITH A AND B", NES_YELLOW, TS(2), y);

        y += TS(1);
        fillText("PRESS START TO START GAME",   NES_YELLOW, TS(3), y);

        drawHorizontalBar(nesColor(0x20), nesColor(0x21), optionsScene.sizeInPx().x(), TS, 212);

        if (scene.debugInfoVisible()) {
            debugInfoRenderer.draw(scene);
        }
    }

    private void drawMarkerIfSelected(TengenMsPacMan_OptionsScene optionsScene, int optionIndex, double y, Font font) {
        if (optionsScene.selectedOption() == optionIndex) {
            ctx.setFill(NES_YELLOW);
            ctx.fillRect(scaled(COL_ARROW + 2.25), scaled(y - 4.5), scaled(7.5), scaled(1.75));
            fillText(">", NES_YELLOW, font, COL_ARROW + 3, y);
        }
    }
}