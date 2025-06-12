/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.ArcadePalette;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import de.amr.pacmanfx.uilib.widgets.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.OptionMenuEntry;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.arcade.ArcadePacMan_GameModel.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static de.amr.pacmanfx.ui.PacManGames_Env.theAssets;
import static de.amr.pacmanfx.ui.PacManGames_Env.theUI;
import static de.amr.pacmanfx.ui.PacManGames_UI.PY_3D_ENABLED;
import static de.amr.pacmanfx.uilib.input.Keyboard.nude;
import static de.amr.pacmanfx.uilib.widgets.OptionMenuStyle.DEFAULT_OPTION_MENU_STYLE;

public class PacManXXL_Common_StartPageMenu extends OptionMenu {

    public static class MenuState {
        String gameVariant;
        boolean play3D;
        boolean cutScenesEnabled;
        MapSelectionMode mapOrder;
    }

    private static class ChaseAnimation {
        private final GraphicsContext ctx;
        private final Pac pac;
        private final Ghost[] ghosts;
        private GameRenderer renderer;
        private boolean chasingGhosts;

        ChaseAnimation(Canvas canvas) {
            ctx = canvas.getGraphicsContext2D();
            pac = createPac();
            ghosts = new Ghost[] {createRedGhost(), createPinkGhost(), createCyanGhost(), createOrangeGhost()};
        }

        void reset() {
            chasingGhosts = false;

            pac.setX(42 * TS);
            pac.setMoveAndWishDir(Direction.LEFT);
            pac.setSpeed(1.0f);
            pac.setVisible(true);

            for (Ghost ghost : ghosts) {
                ghost.setX(46 * TS + ghost.personality() * 18);
                ghost.setMoveAndWishDir(Direction.LEFT);
                ghost.setSpeed(1.05f);
                ghost.setVisible(true);
            }
        }

        void update() {
            if (ghosts[3].x() < -4 * TS && !chasingGhosts) {
                chasingGhosts = true;
                pac.setMoveAndWishDir(pac.moveDir().opposite());
                pac.setX(-36 * TS);
                for (Ghost ghost : ghosts) {
                    ghost.setVisible(true);
                    ghost.setX(pac.x() + 22 * TS + ghost.personality() * 18);
                    ghost.setMoveAndWishDir(ghost.moveDir().opposite());
                    ghost.setSpeed(0.58f);
                    ghost.playAnimation(ANIM_GHOST_FRIGHTENED);
                }
            }
            else if (pac.x() > 56 * TS && chasingGhosts) {
                chasingGhosts = false;
                pac.setMoveAndWishDir(Direction.LEFT);
                pac.setX(42 * TS);
                for (Ghost ghost : ghosts) {
                    ghost.setVisible(true);
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setX(46 * TS + ghost.personality() * 2 * TS);
                    ghost.setSpeed(1.05f);
                    ghost.playAnimation(ANIM_GHOST_NORMAL);
                }
            }
            else if (chasingGhosts) {
                for (int i = 0; i < 4; ++i) {
                    if (Math.abs(pac.x() - ghosts[i].x()) < 1) {
                        ghosts[i].selectAnimation(ANIM_GHOST_NUMBER, i);
                        if (i > 0) {
                            ghosts[i-1].setVisible(false);
                        }
                    }
                }
            }
            pac.move();
            for (Ghost ghost : ghosts) {
                ghost.move();
            }
        }

        void draw(float scaling) {
            ctx.save();
            ctx.translate(0, 23.5 * TS * scaling);
            ctx.setImageSmoothing(false);
            renderer.setScaling(scaling);
            for (Ghost ghost : ghosts) { renderer.drawActor(ghost); }
            renderer.drawActor(pac);
            ctx.restore();
        }

        void setGameVariant(String gameVariant) {
            final PacManGames_UIConfig config = theUI().configuration(gameVariant);
            renderer = config.createRenderer(ctx.getCanvas());
            pac.setAnimations(config.createPacAnimations(pac));
            pac.playAnimation(ANIM_PAC_MUNCHING);
            for (Ghost ghost : ghosts) {
                if (ghost.animations().isEmpty()) {
                    ghost.setAnimations(config.createGhostAnimations(ghost));
                    ghost.playAnimation(ANIM_GHOST_NORMAL);
                }
            }
        }
    }

    // Entries

    private final OptionMenuEntry<String> entryGameVariant = new OptionMenuEntry<>("GAME VARIANT",
        "PACMAN_XXL", "MS_PACMAN_XXL") {

        @Override
        protected void onValueChanged(int index) {
            state.gameVariant = selectedValue();
            chaseAnimation.setGameVariant(state.gameVariant);
            logState();
        }

        @Override
        public String selectedValueText() {
            return switch (state.gameVariant) {
                case "PACMAN_XXL" -> "PAC-MAN";
                case "MS_PACMAN_XXL" -> "MS.PAC-MAN";
                default -> "";
            };
        }
    };

    private final OptionMenuEntry<Boolean> entryPlay3D = new OptionMenuEntry<>("SCENE DISPLAY", true, false) {

        @Override
        protected void onValueChanged(int index) {
            state.play3D = selectedValue();
            PY_3D_ENABLED.set(state.play3D);
            logState();
        }

        @Override
        public String selectedValueText() {
            return state.play3D ? "3D" : "2D";
        }
    };

    private final OptionMenuEntry<Boolean> entryCutScenesEnabled = new OptionMenuEntry<>("CUTSCENES", true, false) {

        @Override
        protected void onValueChanged(int index) {
            state.cutScenesEnabled = selectedValue();
            logState();
        }

        @Override
        public String selectedValueText() {
            return state.cutScenesEnabled ? "ON" : "OFF";
        }
    };

    private final OptionMenuEntry<MapSelectionMode> entryMapOrder = new OptionMenuEntry<>("MAP ORDER",
        MapSelectionMode.CUSTOM_MAPS_FIRST, MapSelectionMode.ALL_RANDOM, MapSelectionMode.NO_CUSTOM_MAPS)
    {
        @Override
        protected void onValueChanged(int index) {
            if (enabled) {
                state.mapOrder = selectedValue();
            }
            logState();
        }

        @Override
        public String selectedValueText() {
            if (!enabled) {
                return "NO CUSTOM MAPS!";
            }
            return switch (state.mapOrder) {
                case CUSTOM_MAPS_FIRST -> "CUSTOM MAPS FIRST";
                case ALL_RANDOM -> "RANDOM ORDER";
                case NO_CUSTOM_MAPS -> "NO CUSTOM MAPS";
            };
        }
    };

    private final MenuState state = new MenuState();
    private final ChaseAnimation chaseAnimation;

    public PacManXXL_Common_StartPageMenu() {
        super(42, 36, 6, 20);

        state.gameVariant = "PACMAN_XXL";
        state.play3D = false;
        state.cutScenesEnabled = true;
        state.mapOrder = MapSelectionMode.CUSTOM_MAPS_FIRST;

        var style = new OptionMenuStyle(
            theAssets().font("font.pacfontgood", 32),
            theAssets().arcadeFont(8),
            DEFAULT_OPTION_MENU_STYLE.backgroundFill(),
            DEFAULT_OPTION_MENU_STYLE.borderStroke(),
            ArcadePalette.ARCADE_RED,
            ArcadePalette.ARCADE_YELLOW,
            ArcadePalette.ARCADE_WHITE,
            DEFAULT_OPTION_MENU_STYLE.entryValueDisabledFill(),
            ArcadePalette.ARCADE_YELLOW,
            DEFAULT_OPTION_MENU_STYLE.entrySelectedSound(),
            DEFAULT_OPTION_MENU_STYLE.valueSelectedSound()
        );
        setStyle(style);
        setTitle("Pac-Man XXL");
        addEntry(entryGameVariant);
        addEntry(entryPlay3D);
        addEntry(entryCutScenesEnabled);
        addEntry(entryMapOrder);
        setCommandTexts(
            "SELECT OPTIONS WITH UP AND DOWN",
            "PRESS SPACE TO CHANGE VALUE",
            "PRESS E TO OPEN EDITOR",
            "PRESS ENTER TO START"
        );
        chaseAnimation = new ChaseAnimation(canvas);
        chaseAnimation.reset();
        chaseAnimation.setGameVariant("PACMAN_XXL");
    }

    public void syncMenuState() {
        final GameModel game = theGameController().game(state.gameVariant);
        final var mapSelector = (PacManXXL_Common_MapSelector) game.mapSelector();
        mapSelector.loadAllMaps();
        final boolean customMapsExist = !mapSelector.customMaps().isEmpty();

        state.play3D = PY_3D_ENABLED.get();
        state.cutScenesEnabled = game.cutScenesEnabled();
        state.mapOrder = mapSelector.mapSelectionMode();
        logState();

        entryGameVariant.selectValue(state.gameVariant);
        entryPlay3D.selectValue(state.play3D);
        entryCutScenesEnabled.selectValue(state.cutScenesEnabled);
        entryMapOrder.selectValue(state.mapOrder);
        entryMapOrder.setEnabled(customMapsExist);

        chaseAnimation.setGameVariant(state.gameVariant);
        chaseAnimation.reset();
    }

    @Override
    protected void handleKeyPress(KeyEvent e) {
        if (nude(KeyCode.E).match(e)) {
            theUI().showEditorView();
        } else if (nude(KeyCode.ENTER).match(e)) {
            startGame();
        } else {
            super.handleKeyPress(e);
        }
    }

    @Override
    protected void updateAnimation() {
        chaseAnimation.update();
    }

    @Override
    public void draw() {
        super.draw();
        chaseAnimation.draw(scalingProperty().get());
    }

    public MenuState state() { return state; }

    private void logState() {
        Logger.info("Menu state: gameVariant={} play3D={} cutScenesEnabled={} mapOrder={}",
            state.gameVariant, state.play3D, state.cutScenesEnabled, state.mapOrder);
    }

    private void startGame() {
        GameModel game = theGameController().game(state.gameVariant);
        game.setCutScenesEnabled(state.cutScenesEnabled);
        var mapSelector = (PacManXXL_Common_MapSelector) game.mapSelector();
        mapSelector.setMapSelectionMode(state.mapOrder);
        mapSelector.loadAllMaps();
        theUI().selectGameVariant(state.gameVariant);
    }
}