/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.ArcadePacMan_GhostAnimationMap;
import de.amr.pacmanfx.arcade.ArcadePacMan_PacAnimationMap;
import de.amr.pacmanfx.arcade.ArcadePalette;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GhostAnimationMap;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_PacAnimationMap;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.PacManGames_UIConfiguration;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import de.amr.pacmanfx.uilib.input.Keyboard;
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
import static de.amr.pacmanfx.ui.PacManGames_Env.*;
import static de.amr.pacmanfx.uilib.widgets.OptionMenuStyle.DEFAULT_OPTION_MENU_STYLE;
import static java.util.Objects.requireNonNull;

public class PacManXXL_Common_StartPageMenu extends OptionMenu {

    public static class MenuState {
        GameVariant gameVariant = GameVariant.PACMAN_XXL;
        boolean play3D;
        boolean cutScenesEnabled;
        MapSelectionMode mapOrder;
    }

    private static class MenuAnimation {
        private final GraphicsContext ctx;
        private Pac pac;
        private Ghost[] ghosts = new Ghost[4];
        private GameRenderer renderer;
        private boolean chasingGhosts;

        MenuAnimation(Canvas canvas) {
            ctx = canvas.getGraphicsContext2D();
        }

        void setGameVariant(GameVariant gameVariant) {
            PacManGames_UIConfiguration config = theUIConfig().configuration(gameVariant);
            renderer = config.createRenderer(ctx.getCanvas());

            switch (gameVariant) {
                case PACMAN_XXL -> pac.setAnimations(new ArcadePacMan_PacAnimationMap(config.spriteSheet()));
                case MS_PACMAN_XXL -> pac.setAnimations(new ArcadeMsPacMan_PacAnimationMap(config.spriteSheet()));
            }
            pac.playAnimation(ANIM_ANY_PAC_MUNCHING);

            for (Ghost ghost : ghosts) {
                if (ghost.animations().isEmpty()) {
                    switch (gameVariant) {
                        case PACMAN_XXL ->
                            ghost.setAnimations(new ArcadePacMan_GhostAnimationMap(config.spriteSheet(), ghost.personality()));
                        case MS_PACMAN_XXL ->
                            ghost.setAnimations(new ArcadeMsPacMan_GhostAnimationMap(config.spriteSheet(), ghost.personality()));
                    }
                    ghost.playAnimation(ANIM_GHOST_NORMAL);
                }
            }
        }

        void reset() {
            chasingGhosts = false;

            pac = createPac();
            pac.setX(42 * TS);
            pac.setMoveAndWishDir(Direction.LEFT);
            pac.setSpeed(1.0f);
            pac.setVisible(true);

            ghosts = new Ghost[] {
                createRedGhost(),
                createPinkGhost(),
                createCyanGhost(),
                createOrangeGhost()
            };
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
    }

    private final MenuState state = new MenuState();
    private final MenuAnimation animation;

    // Entries
    private final OptionMenuEntry<GameVariant> entryGameVariant = new OptionMenuEntry<>("GAME VARIANT",
        GameVariant.PACMAN_XXL, GameVariant.MS_PACMAN_XXL) {

        @Override
        protected void onValueChanged(int index) {
            state.gameVariant = selectedValue();
            animation.setGameVariant(state.gameVariant);
            logMenuState();
        }

        @Override
        public String selectedValueText() {
            return switch (state.gameVariant) {
                case PACMAN_XXL -> "PAC-MAN";
                case MS_PACMAN_XXL -> "MS.PAC-MAN";
                default -> "";
            };
        }
    };

    private final OptionMenuEntry<Boolean> entryPlay3D = new OptionMenuEntry<>("SCENE DISPLAY", true, false) {

        @Override
        protected void onValueChanged(int index) {
            state.play3D = selectedValue();
            PY_3D_ENABLED.set(state.play3D);
            logMenuState();
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
            logMenuState();
        }

        @Override
        public String selectedValueText() {
            return state.cutScenesEnabled ? "ON" : "OFF";
        }
    };

    private final OptionMenuEntry<MapSelectionMode> entryMapOrder = new OptionMenuEntry<>("MAP ORDER",
        MapSelectionMode.CUSTOM_MAPS_FIRST, MapSelectionMode.ALL_RANDOM) {

        @Override
        protected void onValueChanged(int index) {
            if (enabled) {
                state.mapOrder = selectedValue();
            }
            logMenuState();
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

    PacManXXL_Common_StartPageMenu() {
        super(42, 36, 6, 20);

        var style = new OptionMenuStyle(
            theAssets().font("font.pacfontgood", 32),
            theAssets().arcadeFontAtSize(8),
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
        animation = new MenuAnimation(canvas);
        animation.reset();
        animation.setGameVariant(GameVariant.PACMAN_XXL);
    }

    public MenuState state() { return state; }

    void initState() {
        GameModel game = theGameController().game(state.gameVariant);
        var mapSelector = (PacManXXL_Common_MapSelector) game.mapSelector();
        mapSelector.loadAllMaps();
        boolean customMapsExist = !mapSelector.customMaps().isEmpty();

        entryGameVariant.selectValue(state.gameVariant);
        setPlay3D(PY_3D_ENABLED.get());
        setCutScenesEnabled(game.cutScenesEnabledProperty().get());
        setMapOrder(mapSelector.mapSelectionMode(), customMapsExist);

        animation.reset();
        animation.setGameVariant(state.gameVariant);

        logMenuState();
        Logger.info("Option menu initialized");
    }

    private void setPlay3D(boolean play3D) {
        state.play3D = play3D;
        entryPlay3D.selectValue(play3D);
    }

    private void setCutScenesEnabled(boolean cutScenesEnabled) {
        state.cutScenesEnabled = cutScenesEnabled;
        entryCutScenesEnabled.selectValue(cutScenesEnabled);
    }

    private void setMapOrder(MapSelectionMode mapOrder, boolean customMapsExist) {
        state.mapOrder = requireNonNull(mapOrder);
        entryMapOrder.selectValue(mapOrder);
        entryMapOrder.setEnabled(customMapsExist);
    }

    private void logMenuState() {
        Logger.info("gameVariant={} play3D={} cutScenesEnabled={} mapOrder={}",
            state.gameVariant, state.play3D, state.cutScenesEnabled, state.mapOrder);
    }

    @Override
    protected void handleKeyPress(KeyEvent e) {
        if (Keyboard.naked(KeyCode.E).match(e)) {
            theUI().showEditorView();
        } else if (Keyboard.naked(KeyCode.ENTER).match(e)) {
            startGame();
        } else {
            super.handleKeyPress(e);
        }
    }

    @Override
    protected void animationStep() {
        animation.update();
        draw();
    }

    @Override
    public void draw() {
        super.draw();
        animation.draw(scalingProperty().get());
    }

    private void startGame() {
        if (state.gameVariant == GameVariant.PACMAN_XXL || state.gameVariant == GameVariant.MS_PACMAN_XXL) {
            GameModel game = theGameController().game(state.gameVariant);
            game.cutScenesEnabledProperty().set(state.cutScenesEnabled);
            var mapSelector = (PacManXXL_Common_MapSelector) game.mapSelector();
            mapSelector.setMapSelectionMode(state.mapOrder);
            mapSelector.loadAllMaps();
            theUI().selectGameVariant(state.gameVariant);
        } else {
            Logger.error("Game variant {} is not allowed for XXL game", state.gameVariant);
        }
    }

}
