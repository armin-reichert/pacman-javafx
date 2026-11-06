module de.amr.basics.ui {
    requires de.amr.basics;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.media;
    requires org.tinylog.api;

    exports de.amr.basics.ui;
    exports de.amr.basics.ui.animation;
    exports de.amr.basics.ui.assets;
    exports de.amr.basics.ui.ecs.comp;
    exports de.amr.basics.ui.ecs.system;
    exports de.amr.basics.ui.entities.hud;
    exports de.amr.basics.ui.entities.hud.levelCounter;
    exports de.amr.basics.ui.entities.hud.livescounter;
    exports de.amr.basics.ui.entities.hud.score;
    exports de.amr.basics.ui.entities.props;
    exports de.amr.basics.ui.entities.props.bonuspoints;
    exports de.amr.basics.ui.entities.props.clapperboard;
    exports de.amr.basics.ui.entities.props.ghostpoints;
    exports de.amr.basics.ui.entities.props.imagedisplay;
    exports de.amr.basics.ui.entities.props.marquee;
    exports de.amr.basics.ui.entities.props.messageview;
    exports de.amr.basics.ui.entities.props.stork;
    exports de.amr.basics.ui.entities.props.textview;
    exports de.amr.basics.ui.rendering;
    exports de.amr.basics.ui.spriteanim;
}