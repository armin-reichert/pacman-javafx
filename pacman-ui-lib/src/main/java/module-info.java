// module is open to allow access to non-class resources
open module de.amr.pacmanfx.uilib {
    requires java.prefs;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires org.tinylog.api;
    requires com.google.gson;

    requires de.amr.basics;
    requires de.amr.meshbuilder;
    requires de.amr.objparser;
    requires de.amr.pacmanfx.core;
    requires de.amr.basics.ui;

    exports de.amr.pacmanfx.uilib;
    exports de.amr.pacmanfx.uilib.rendering;
    exports de.amr.pacmanfx.uilib.controls;
    exports de.amr.pacmanfx.uilib.controls.skin;

    exports de.amr.pacmanfx.uilib.entities3d.ghost.comp;
    exports de.amr.pacmanfx.uilib.entities3d.bonus.anim;
    exports de.amr.pacmanfx.uilib.entities3d.ghost.system;
    exports de.amr.pacmanfx.uilib.entities3d.house.comp;
    exports de.amr.pacmanfx.uilib.entities3d.house.system;
    exports de.amr.pacmanfx.uilib.entities3d.pac.anim;
    exports de.amr.pacmanfx.uilib.entities3d.pac.comp;
    exports de.amr.pacmanfx.uilib.entities3d.pac.system;
    exports de.amr.pacmanfx.uilib.entities3d.ghost.anim;
    exports de.amr.pacmanfx.uilib.entities3d.bonus.system;
    exports de.amr.pacmanfx.uilib.entities3d.bonus.comp;
    exports de.amr.pacmanfx.uilib.entities3d.score.comp;
    exports de.amr.pacmanfx.uilib.entities3d.levelcounter.comp;
    exports de.amr.pacmanfx.uilib.entities3d.messageview.comp;
    exports de.amr.pacmanfx.uilib.entities3d.messageview.system;
    exports de.amr.pacmanfx.uilib.entities3d.messageview;
    exports de.amr.pacmanfx.uilib.entities3d.world;

    exports de.amr.pacmanfx.uilib.widgets;
    exports de.amr.pacmanfx.uilib.widgets.decorationpane;
    exports de.amr.pacmanfx.uilib.widgets.optionmenu;
    exports de.amr.pacmanfx.uilib.entities3d;
    exports de.amr.pacmanfx.uilib.dashboard;
    exports de.amr.pacmanfx.uilib.renderer;
}