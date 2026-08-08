package com.seija.printer.player;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class Rotations {
    public static float serverYaw;
    public static float serverPitch;
    public static int rotationTimer;
    public static boolean rotating;

    private static final float DEFAULT_ROTATION_SPEED = 15.0f;
    private static final float MIN_STEP = 0.5f;

    private Rotations() {
    }

    public static void init() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            serverYaw = minecraft.player.getYRot();
            serverPitch = minecraft.player.getXRot();
        }
    }

    public static void rotate(double yaw, double pitch, int priority, Runnable callback) {
        smoothRotate((float) yaw, (float) pitch, DEFAULT_ROTATION_SPEED);
        if (callback != null) callback.run();
    }

    public static void rotate(double yaw, double pitch, int priority, boolean clientSide, Runnable callback) {
        rotate(yaw, pitch, priority, callback);
    }

    public static void rotate(double yaw, double pitch, int priority) { rotate(yaw, pitch, priority, null); }
    public static void rotate(double yaw, double pitch, Runnable callback) { rotate(yaw, pitch, 0, callback); }
    public static void rotate(double yaw, double pitch) { rotate(yaw, pitch, 0, null); }

    public static void smoothRotate(float targetYaw, float targetPitch, float speed) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        float currentYaw = minecraft.player.getYRot();
        float currentPitch = minecraft.player.getXRot();

        float yawDelta = Mth.wrapDegrees(targetYaw - currentYaw);
        float pitchDelta = targetPitch - currentPitch;

        float yawStep = Math.abs(yawDelta) < MIN_STEP ? yawDelta : yawDelta * speed * 0.05f;
        float pitchStep = Math.abs(pitchDelta) < MIN_STEP ? pitchDelta : pitchDelta * speed * 0.05f;

        float newYaw = currentYaw + yawStep;
        float newPitch = Mth.clamp(currentPitch + pitchStep, -90.0f, 90.0f);

        serverYaw = newYaw;
        serverPitch = newPitch;
        minecraft.player.setYRot(newYaw);
        minecraft.player.setXRot(newPitch);
        minecraft.player.setYHeadRot(newYaw);
    }

    public static void setCamRotation(double yaw, double pitch) {
        smoothRotate((float) yaw, (float) pitch, DEFAULT_ROTATION_SPEED);
    }

    public static float getYawDelta(float currentYaw, float targetYaw) {
        return Mth.wrapDegrees(targetYaw - currentYaw);
    }

    public static double getYaw(Vec3 target) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return 0;
        Vec3 origin = minecraft.player.getEyePosition();
        return Math.toDegrees(Math.atan2(target.z - origin.z, target.x - origin.x)) - 90.0;
    }

    public static double getYaw(Entity target) { return getYaw(target.position()); }

    public static double getPitch(Vec3 target) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return 0;
        Vec3 origin = minecraft.player.getEyePosition();
        double horizontal = Math.sqrt(
                (target.x - origin.x) * (target.x - origin.x) +
                        (target.z - origin.z) * (target.z - origin.z)
        );
        return -Math.toDegrees(Math.atan2(target.y - origin.y, horizontal));
    }

    public static double getPitch(Entity target) { return getPitch(target.position()); }
}