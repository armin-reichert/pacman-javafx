/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMapSelectionMode;
import de.amr.pacmanfx.ui.api.ArcadePalette;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.widgets.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.OptionMenuEntry;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.StandardGameVariant.ARCADE_MS_PACMAN_XXL;
import static de.amr.pacmanfx.model.StandardGameVariant.ARCADE_PACMAN_XXL;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_3D_ENABLED;
import static de.amr.pacmanfx.uilib.widgets.OptionMenuStyle.DEFAULT_OPTION_MENU_STYLE;
import static java.util.Objects.requireNonNull;

public class PacManXXL_StartPageMenu extends OptionMenu {

    // State

    private final ObjectProperty<StandardGameVariant> gameVariant = new SimpleObjectProperty<>();
    private final BooleanProperty play3D = new SimpleBooleanProperty();
    private final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty();
    private final ObjectProperty<WorldMapSelectionMode> mapOrder = new SimpleObjectProperty<>();

    public ObjectProperty<StandardGameVariant> gameVariantProperty() {
        return gameVariant;
    }

    public BooleanProperty play3DProperty() {
        return play3D;
    }

    public BooleanProperty cutScenesEnabledProperty() {
        return cutScenesEnabled;
    }

    public ObjectProperty<WorldMapSelectionMode> mapOrderProperty() {
        return mapOrder;
    }

    // Entries

    private final OptionMenuEntry<StandardGameVariant> entryGameVariant = new OptionMenuEntry<>(
        "GAME VARIANT", ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN_XXL)
    {
        @Override
        protected void onValueChanged(int index) {
            gameVariantProperty().set(selectedValue());
        }

        @Override
        public String selectedValueText() {
            final StandardGameVariant gameVariant = gameVariantProperty().get();
            return switch (gameVariant) {
                case null -> "";
                case ARCADE_PACMAN_XXL    -> "PAC-MAN XXL";
                case ARCADE_MS_PACMAN_XXL -> "MS.PAC-MAN XXL";
                default -> "???";
            };
        }
    };

    private final OptionMenuEntry<Boolean> entryPlay3D = new OptionMenuEntry<>("SCENE DISPLAY", true, false) {

        @Override
        protected void onValueChanged(int index) {
            play3DProperty().set(selectedValue());
        }

        @Override
        public String selectedValueText() {
            return play3DProperty().get() ? "3D" : "2D";
        }
    };

    private final OptionMenuEntry<Boolean> entryCutScenesEnabled = new OptionMenuEntry<>("CUTSCENES", true, false) {

        @Override
        protected void onValueChanged(int index) {
            cutScenesEnabledProperty().set(selectedValue());
        }

        @Override
        public String selectedValueText() {
            return cutScenesEnabledProperty().get() ? "ON" : "OFF";
        }
    };

    private final OptionMenuEntry<WorldMapSelectionMode> entryMapOrder = new OptionMenuEntry<>("MAP ORDER",
        WorldMapSelectionMode.CUSTOM_MAPS_FIRST, WorldMapSelectionMode.ALL_RANDOM, WorldMapSelectionMode.NO_CUSTOM_MAPS)
    {
        @Override
        protected void onValueChanged(int index) {
            if (enabled) {
                mapOrderProperty().set(selectedValue());
            }
        }

        @Override
        public String selectedValueText() {
            if (!enabled) {
                return "NO CUSTOM MAPS!";
            }
            return switch (mapOrderProperty().get()) {
                case CUSTOM_MAPS_FIRST -> "CUSTOM MAPS FIRST";
                case ALL_RANDOM -> "RANDOM ORDER";
                case NO_CUSTOM_MAPS -> "NO CUSTOM MAPS";
            };
        }
    };

    private final GameUI ui;
    private final ChaseAnimation chaseAnimation = new ChaseAnimation();
    private final Timeline animationTimer;

    public PacManXXL_StartPageMenu(GameUI ui) {
        super(42, 36, 6, 20);

        this.ui = requireNonNull(ui);

        final var style = new OptionMenuStyle(
            Font.font(GameUI.FONT_PAC_FONT_GOOD.getFamily(), 32),
            GameUI.FONT_ARCADE_8,
            Ufx.colorWithOpacity(DEFAULT_OPTION_MENU_STYLE.backgroundFill(), 0.5),
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

        final int freq = 60;
        animationTimer = new Timeline(freq,
            new KeyFrame(Duration.seconds(1.0 / freq), _ -> {
                draw();
                chaseAnimation.update();
                chaseAnimation.draw();
            }));
        animationTimer.setCycleCount(Animation.INDEFINITE);
    }

    public void init(GameUI ui) {
        final Game game = ui.context().currentGame();

        if (StandardGameVariant.ARCADE_PACMAN_XXL.name().equals(ui.context().gameVariantName())) {
            gameVariantProperty().set(StandardGameVariant.ARCADE_PACMAN_XXL);
        } else if (StandardGameVariant.ARCADE_MS_PACMAN_XXL.name().equals(ui.context().gameVariantName())) {
            gameVariantProperty().set(StandardGameVariant.ARCADE_MS_PACMAN_XXL);
        } else {
            Logger.error("Illegal game variant for XXL start page menu");
        }

        final var mapSelector = (PacManXXL_MapSelector) game.mapSelector();
        mapSelector.loadMapPrototypes();

        final boolean customMapsExist = !mapSelector.customMapPrototypes().isEmpty();
        entryMapOrder.setEnabled(customMapsExist);

        play3DProperty().set(PROPERTY_3D_ENABLED.get());
        cutScenesEnabledProperty().set(game.cutScenesEnabled());
        mapOrder.set(mapSelector.selectionMode());

        soundEnabledProperty().bind(ui.currentConfig().soundManager().muteProperty().not());

        initAnimation();
    }

    public void initAnimation() {
        chaseAnimation.init(ui.currentConfig());
        animationTimer.play();
    }

    public void startAnimation() {
        animationTimer.play();
    }

    public void stopAnimation() {
        animationTimer.stop();
    }

    @Override
    protected void drawUsageInfo() {
        final GraphicsContext ctx = renderer.ctx();
        final Color normal = style.hintTextFill(), bright = style.entryValueFill();

        ctx.setFont(style.textFont());

        double y = TS(numTilesY() - 8);
        ctx.setFill(normal);
        ctx.fillText("SELECT OPTIONS WITH", TS(6), y);
        ctx.setFill(bright);
        ctx.fillText("UP", TS(26), y);
        ctx.setFill(normal);
        ctx.fillText("AND", TS(29), y);
        ctx.setFill(bright);
        ctx.fillText("DOWN", TS(33), y);

        y += TS(2);
        ctx.setFill(normal);
        ctx.fillText("PRESS", TS(8), y);
        ctx.setFill(bright);
        ctx.fillText("SPACE", TS(14), y);
        ctx.setFill(normal);
        ctx.fillText("TO CHANGE VALUE", TS(20), y);

        y += TS(2);
        ctx.setFill(normal);
        ctx.fillText("PRESS", TS(10), y);
        ctx.setFill(bright);
        ctx.fillText("E", TS(16), y);
        ctx.setFill(normal);
        ctx.fillText("TO OPEN EDITOR", TS(18), y);

        y += TS(2);
        ctx.setFill(normal);
        ctx.fillText("PRESS", TS(11), y);
        ctx.setFill(bright);
        ctx.fillText("ENTER", TS(17), y);
        ctx.setFill(normal);
        ctx.fillText("TO START", TS(23), y);
    }

    private void logState() {
        Logger.info("Menu state: gameVariant={} play3D={} cutScenesEnabled={} mapOrder={}",
            gameVariantProperty().get(), play3DProperty().get(), cutScenesEnabledProperty().get(), mapOrderProperty().get());
    }

    public void startGame(Game game) {
        final var mapSelector = (PacManXXL_MapSelector) game.mapSelector();
        mapSelector.setSelectionMode(mapOrderProperty().get());
        mapSelector.loadMapPrototypes();
        game.setCutScenesEnabled(cutScenesEnabledProperty().get());
        ui.selectGameVariant(gameVariantProperty().get().name());
    }

    private class ChaseAnimation {
        private Pac pac;
        private List<Ghost> ghosts;
        private ActorRenderer actorRenderer;
        private boolean ghostsChased;
        private boolean running;

        public void init(GameUI_Config uiConfig) {
            requireNonNull(uiConfig);

            pac = ArcadePacMan_GameModel.createPacMan();
            pac.setAnimationManager(uiConfig.createPacAnimations());

            ghosts = List.of(
                uiConfig.createGhostWithAnimations(RED_GHOST_SHADOW),
                uiConfig.createGhostWithAnimations(PINK_GHOST_SPEEDY),
                uiConfig.createGhostWithAnimations(CYAN_GHOST_BASHFUL),
                uiConfig.createGhostWithAnimations(ORANGE_GHOST_POKEY)
            );

            actorRenderer = uiConfig.createActorRenderer(renderer.ctx().getCanvas());
            reset();
            start();
        }

        public void start() {
            running = true;
            pac.playAnimation(Pac.AnimationID.PAC_MUNCHING);
            ghosts.forEach(ghost -> ghost.playAnimation(Ghost.AnimationID.GHOST_NORMAL));
        }

        public void reset() {
            pac.setX(42 * TS);
            pac.setMoveDir(Direction.LEFT);
            pac.setWishDir(Direction.LEFT);
            pac.setSpeed(1.0f);
            pac.setVisible(true);

            for (Ghost ghost : ghosts) {
                ghost.setX(46 * TS + ghost.personality() * 18);
                ghost.setMoveDir(Direction.LEFT);
                ghost.setWishDir(Direction.LEFT);
                ghost.setSpeed(1.05f);
                ghost.setVisible(true);
            }
            ghostsChased = false;
        }

        public void update() {
            if (!running) return;

            if (ghosts.getLast().x() < -4 * TS && !ghostsChased) {
                ghostsChased = true;
                pac.setMoveDir(pac.moveDir().opposite());
                pac.setWishDir(pac.moveDir().opposite());
                pac.setX(-36 * TS);
                for (Ghost ghost : ghosts) {
                    ghost.setVisible(true);
                    ghost.setX(pac.x() + 22 * TS + ghost.personality() * 18);
                    ghost.setMoveDir(ghost.moveDir().opposite());
                    ghost.setWishDir(ghost.moveDir().opposite());
                    ghost.setSpeed(0.58f);
                    ghost.playAnimation(Ghost.AnimationID.GHOST_FRIGHTENED);
                }
            }
            else if (pac.x() > 56 * TS && ghostsChased) {
                ghostsChased = false;
                pac.setMoveDir(Direction.LEFT);
                pac.setWishDir(Direction.LEFT);
                pac.setX(42 * TS);
                for (Ghost ghost : ghosts) {
                    ghost.setVisible(true);
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setX(46 * TS + ghost.personality() * 2 * TS);
                    ghost.setSpeed(1.05f);
                    ghost.playAnimation(Ghost.AnimationID.GHOST_NORMAL);
                }
            }
            else if (ghostsChased) {
                IntStream.range(0, 4).forEach(i -> {
                    if (Math.abs(pac.x() - ghosts.get(i).x()) < 1) {
                        ghosts.get(i).selectAnimationAt(Ghost.AnimationID.GHOST_POINTS, i);
                        if (i > 0) {
                            ghosts.get(i-1).setVisible(false);
                        }
                    }
                });
            }
            pac.move();
            for (Ghost ghost : ghosts) {
                ghost.move();
            }
        }

        public void draw() {
            if (actorRenderer != null) {
                actorRenderer.setScaling(scaling());
                actorRenderer.setImageSmoothing(true);
                actorRenderer.ctx().save();
                actorRenderer.ctx().translate(0, TS(23.5f) * scaling());
                ghosts.forEach(actorRenderer::drawActor);
                actorRenderer.drawActor(pac);
                actorRenderer.ctx().restore();
            }
        }
    }
}