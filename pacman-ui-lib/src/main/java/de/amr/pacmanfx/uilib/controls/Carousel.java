/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.controls;

import de.amr.pacmanfx.uilib.controls.skin.CarouselSkin;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.net.URL;

public class Carousel extends Control {

    public static final String STYLE_CLASS = "carousel";

    public static final String STYLESHEET = "carousel.css";

    public static final PseudoClass NAV_NEXT_PC = PseudoClass.getPseudoClass("nav-next");

    public static final PseudoClass NAV_PREV_PC = PseudoClass.getPseudoClass("nav-prev");

    private final ObservableList<Node> items = FXCollections.observableArrayList();

    private final IntegerProperty selectedIndex = new SimpleIntegerProperty(this, "selectedIndex", -1);

    private final BooleanProperty navigationLocked = new SimpleBooleanProperty(this, "navigationLocked", false);

    private final DoubleProperty changeDuration = new SimpleDoubleProperty(this, "changeDuration", 5.0);

    private final BooleanProperty progressRunning = new SimpleBooleanProperty(false);

    public Carousel() {
        getStyleClass().add(STYLE_CLASS);
        setFocusTraversable(true);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CarouselSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        final URL url = getClass().getResource(STYLESHEET);
        return url != null ? url.toExternalForm() : null;
    }

    // --- Items ---

    public ObservableList<Node> getItems() { return items; }

    // --- Selected Index ---

    public IntegerProperty selectedIndexProperty() { return selectedIndex; }

    public int getSelectedIndex() { return selectedIndex.get(); }

    public void setSelectedIndex(int pageIndex) {
        if (pageIndex < -1 || pageIndex >= items.size()) {
            throw new IndexOutOfBoundsException("Invalid carousel page index: " + pageIndex);
        }
        selectedIndex.set(pageIndex);
    }

    // --- Progress timer control

    public BooleanProperty progressRunningProperty() {
        return progressRunning;
    }

    public void startProgress() {
        progressRunning.set(true);
    }

    public void pauseProgress() {
        progressRunning.set(false);
    }

    // --- Navigation Locked ---

    public BooleanProperty navigationLockedProperty() { return navigationLocked; }

    public boolean isNavigationLocked() { return navigationLocked.get(); }

    public void setNavigationLocked(boolean b) { navigationLocked.set(b); }

    // --- Change Duration ---

    public DoubleProperty changeDurationProperty() { return changeDuration; }

    public double getChangeDuration() { return changeDuration.get(); }

    public void setChangeDuration(double seconds) { changeDuration.set(seconds); }

    // --- Navigation API

    public void next() {
        pseudoClassStateChanged(NAV_NEXT_PC, true);
        pseudoClassStateChanged(NAV_NEXT_PC, false);
    }

    public void previous() {
        pseudoClassStateChanged(NAV_PREV_PC, true);
        pseudoClassStateChanged(NAV_PREV_PC, false);
    }
}
