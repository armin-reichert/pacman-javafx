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

    exports de.amr.pacmanfx.uilib;
    exports de.amr.pacmanfx.uilib.animation;
    exports de.amr.pacmanfx.uilib.assets;
    exports de.amr.pacmanfx.uilib.rendering;
    exports de.amr.pacmanfx.uilib.controls;
    exports de.amr.pacmanfx.uilib.controls.skin;

    exports de.amr.pacmanfx.uilib.entities.hud;
    exports de.amr.pacmanfx.uilib.entities.d3.ghost.comp;
    exports de.amr.pacmanfx.uilib.entities.d3.bonus.anim;
    exports de.amr.pacmanfx.uilib.entities.d3.ghost.system;
    exports de.amr.pacmanfx.uilib.entities.d3.house.comp;
    exports de.amr.pacmanfx.uilib.entities.d3.house.system;
    exports de.amr.pacmanfx.uilib.entities.d3.pac.anim;
    exports de.amr.pacmanfx.uilib.entities.d3.pac.comp;
    exports de.amr.pacmanfx.uilib.entities.d3.pac.system;
    exports de.amr.pacmanfx.uilib.entities.d3.ghost.anim;
    exports de.amr.pacmanfx.uilib.entities.d3.bonus.system;
    exports de.amr.pacmanfx.uilib.entities.d3.bonus.comp;
    exports de.amr.pacmanfx.uilib.entities.d3.score.comp;
    exports de.amr.pacmanfx.uilib.entities.d3.levelcounter.comp;
    exports de.amr.pacmanfx.uilib.entities.d3.messageview.comp;
    exports de.amr.pacmanfx.uilib.entities.d3.messageview.system;
    exports de.amr.pacmanfx.uilib.entities.d3.messageview;
    exports de.amr.pacmanfx.uilib.entities.d3.world;

    exports de.amr.pacmanfx.uilib.widgets;
    exports de.amr.pacmanfx.uilib.widgets.decorationpane;
    exports de.amr.pacmanfx.uilib.widgets.optionmenu;
    exports de.amr.pacmanfx.uilib.entities.d3;
    exports de.amr.pacmanfx.uilib.entities.props.marquee;
    exports de.amr.pacmanfx.uilib.entities.props.clapperboard;
    exports de.amr.pacmanfx.uilib.entities.props.imagedisplay;
}