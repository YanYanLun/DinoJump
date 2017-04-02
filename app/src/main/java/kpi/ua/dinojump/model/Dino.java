package kpi.ua.dinojump.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import java.util.HashMap;
import java.util.Map;

import kpi.ua.dinojump.Constants;


public class Dino extends BaseEntity {

    private static final int HEIGHT = 47;
    private static final int WIDTH = 44;
    private static final int WIDTH_DUCK = 59;
    private static int GROUND_POS = Constants.HEIGHT - HEIGHT - Constants.BOTTOM_PAD;

    //Possible dino states
    public enum DinoState {
        CRASHED, DUCKING, JUMPING, RUNNING, WAITING
    }

    private DinoState preCrashState = null;

    private double velocityY;
    private boolean playingIntro;

    private Rect collisionBox;
    private int xPos, yPos, animFrameX, animFrameY, currentFrame;
    private DinoState currStatus;
    private Map<DinoState, AnimFrames> animFrames;
    private int[] currentAnimFrames;

    public Dino(Point s) {
        spritePos = s;
        // Position when on the ground.
        currStatus = DinoState.WAITING;
        init();
    }

    private void init() {
        initAnimationFrames();
        collisionBox = new Rect(xPos, yPos, WIDTH, HEIGHT);
        yPos = GROUND_POS;
        xPos = 100;
        update(DinoState.WAITING);
    }

    private void initAnimationFrames() {
        animFrames = new HashMap<>();
        animFrames.put(DinoState.WAITING, new AnimFrames(new int[]{44, 0}, 3));
        animFrames.put(DinoState.RUNNING, new AnimFrames(new int[]{88, 132}, 12));
        animFrames.put(DinoState.CRASHED, new AnimFrames(new int[]{220}, 60));
        animFrames.put(DinoState.JUMPING, new AnimFrames(new int[]{0}, 60));
        animFrames.put(DinoState.DUCKING, new AnimFrames(new int[]{262, 321}, 8));
    }

    private void defaultUpdate() {
        if (isJumping()) {
            updateJump();
        }
        draw(currentAnimFrames[currentFrame], 0);
        currentFrame = currentFrame == currentAnimFrames.length - 1 ? 0 : currentFrame + 1;
        collisionBox.left = xPos;
        collisionBox.top = yPos;
        collisionBox.right = xPos + (isDucking() ? WIDTH_DUCK : WIDTH);
        collisionBox.bottom = yPos + HEIGHT;
    }

    public void update() {
        defaultUpdate();
    }

    public void update(DinoState status) {
        if (status != null) {
            if (status == DinoState.CRASHED) {
                preCrashState = currStatus;
            }
            currStatus = status;
            currentFrame = 0;
            currentAnimFrames = animFrames.get(status).frames;
        }
        defaultUpdate();
    }

    private void updateJump() {
        yPos += Math.round(velocityY/* * speedDropCoefficient*/);
        velocityY += Constants.GRAVITY;

        // Back down at ground level. Jump completed.
        if (yPos > GROUND_POS) {
            reset();
        }
    }

    public void reset() {
        yPos = GROUND_POS;
        velocityY = 0;
        update(DinoState.RUNNING);
    }

    private void draw(int vx, int vy) {
        animFrameX = vx;
        animFrameY = vy;
    }

    public void startDuck() {
        if (currStatus == DinoState.RUNNING) {
            update(DinoState.DUCKING);
        }
    }

    public void endDuck() {
        if (isDucking()) {
            update(DinoState.RUNNING);
        }
    }

    public void startJump() {
        if (!isJumping()) {
            update(DinoState.JUMPING);
            velocityY = Constants.INITIAL_JUMP_VELOCITY;
        }
    }

    public void endJump() {
        if (velocityY < -6.0) {
            velocityY = -6.0;
        }
    }

    public void draw(Canvas canvas) {
        Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        int animFrameX = this.animFrameX;
        int animFrameY = this.animFrameY;
        int sourceWidth = isDucking() ? WIDTH_DUCK : WIDTH;
        int sourceHeight = HEIGHT;
        // Adjustments for sprite sheet position.
        animFrameX += spritePos.x;
        animFrameY += spritePos.y;
        Paint p = new Paint();
        p.setColor(Color.RED);
        p.setStrokeWidth(2f);
        if (currStatus == DinoState.CRASHED && preCrashState == DinoState.DUCKING) {
            xPos++;
        }
        Rect sRect = getScaledSource(animFrameX, animFrameY, sourceWidth, sourceHeight);
        Rect tRect = getScaledTarget(xPos, yPos, WIDTH, HEIGHT);
        canvas.drawBitmap(getBaseBitmap(), sRect, tRect, bitmapPaint);
    }

    public Rect getCollisionBox() {
        return collisionBox;
    }

    private class AnimFrames {
        final int[] frames;
        final int FPS;

        AnimFrames(int[] f, int fps) {
            FPS = fps;
            frames = f;
        }
    }

    public void setPlayingIntro(boolean playingIntro) {
        this.playingIntro = playingIntro;
    }

    private boolean isDucking() {
        return currStatus == DinoState.DUCKING;
    }

    private boolean isJumping() {
        return currStatus == DinoState.JUMPING;
    }
}