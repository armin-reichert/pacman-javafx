package de.amr.pacmanfx.tengenmspacman.entities.messageview;

import de.amr.basics.ecs.system.MovementSystem;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.entities.props.messageview.MessageView;

public class MessageAnimationSystem {

    private final MovementSystem motor;

    public MessageAnimationSystem(MovementSystem motor) {
        this.motor = motor;
    }

    public void start(MessageView messageView, Vector2f startPosition, int delayTicks) {
        if (!messageView.hasComp(MessageAnimationComp.class)) {
            return;
        }
        final MessageAnimationComp animation = messageView.reqComp(MessageAnimationComp.class);
        animation.setDelayTicks(delayTicks);
        animation.setStartPosition(startPosition);
        animation.setWrapped(false);
        animation.setFinished(false);
        animation.setRunning(true);
        messageView.pos().set(startPosition);
        messageView.show();
    }

    public void update(MessageView messageView) {
        if (!messageView.hasComp(MessageAnimationComp.class)) {
            return;
        }
        final MessageAnimationComp animation = messageView.reqComp(MessageAnimationComp.class);
        if (!animation.running() || animation.finished()) {
            return;
        }
        if (animation.delayTicks() > 0) {
            animation.setDelayTicks(animation.delayTicks() - 1);
            return;
        }
        if (animation.wrapped()) {
            if (messageView.pos().x() >= animation.startPosition().x()) {
                motor.setVelocity(messageView, 0, 0);
                animation.setRunning(false);
                animation.setFinished(true);
                return;
            }
        }
        else if (messageView.pos().x() > animation.wrapX()) {
            messageView.pos().setX(-0.5 * animation.width());
            animation.setWrapped(true);
        }
        motor.setVelocityX(messageView, 1.0f);
        motor.move(messageView);
    }
}
