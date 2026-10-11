package de.amr.pacmanfx.ui.views;

import de.amr.basics.Named;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.ui.EngineUIContext;
import javafx.beans.property.ObjectProperty;

import java.util.Optional;

public interface GameViewManager {

    <V extends GameView> V assertView(Named id, Class<V> viewClass);

    default GameView assertView(Named viewID) {
        return assertView(viewID, GameView.class);
    }

    <V extends GameView> Optional<V> optView(Named id, Class<V> viewClass);

    ObjectProperty<Named> currentViewIDProperty();

    Named currentViewID();

    Optional<GameView> optCurrentView();

    default GameView assertCurrentView() {
        return assertView(currentViewID());
    }

    //TODO dubious!
    void onGameVariantChanged();

    void onGameSuspended(GameScene gameScene);

    boolean isSelected(Named viewID);
}
