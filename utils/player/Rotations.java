package com.kijinseija.seija_printer.utils.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/** Lightweight rotation controller for the printer's placement callbacks. */
public final class Rotations {
    public static float serverYaw;
    public static float serverPitch;
    public static int rotationTimer;
    public static boolean rotating;

    private Rotations() {
    }

    public static void init() {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.player != null) {
            serverYaw = minecraft.player.getYaw();
            serverPitch = minecraft.player.getPitch();
        }
    }

    public static void rotate(double yaw, double pitch, int priority, Runnable callback) {
        setCamRotation(yaw, pitch);
        if (callback != null) callback.run();
    }

    public static void rotate(double yaw, double pitch, int priority, boolean clientSide, Runnable callback) {
        rotate(yaw, pitch, priority, callback);
    }

    public static void rotate(double yaw, double pitch, int priority) { rotate(yaw, pitch, priority, null); }
    public static void rotate(double yaw, double pitch, Runnable callback) { rotate(yaw, pitch, 0, callback); }
    public static void rotate(double yaw, double pitch) { rotate(yaw, pitch, 0, null); }

    public static double getYaw(Vec3d target) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.player == null) return 0;
        Vec3d origin = minecraft.player.getEyePos();
        return Math.toDegrees(Math.atan2(target.z - origin.z, target.x - origin.x)) - 90.0;
    }

    public static double getYaw(Entity target) { return getYaw(target.getPos()); }
    public static double getPitch(Vec3d target) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.player == null) return 0;
        Vec3d origin = minecraft.player.getEyePos();
        double horizontal = Math.sqrt((target.x - origin.x) * (target.x - origin.x) + (target.z - origin.z) * (target.z - origin.z));
        return -Math.toDegrees(Math.atan2(target.y - origin.y, horizontal));
    }
    public static double getPitch(Entity target) { return getPitch(target.getPos()); }

    public static void setCamRotation(double yaw, double pitch) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.player == null) return;
        serverYaw = (float) yaw;
        serverPitch = (float) pitch;
        minecraft.player.setYaw((float) yaw);
        minecraft.player.setPitch((float) pitch);
        minecraft.player.setHeadYaw((float) yaw);
    }
}
