/*
 * Copyright 2025 Nippaku_Zanmu
 * SPDX-License-Identifier: gplv3
 */

package com.kijinseija.seija_printer.events.render;

import com.kijinseija.seija_printer.render.ShapeMode;
import com.kijinseija.seija_printer.settings.core.Color;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Render callback payload and a small immediate-style shape facade. */
public final class Render3DEvent {
    public final WorldRenderContext context;
    public final MatrixStack matrices;
    public final Renderer renderer;

    public Render3DEvent(WorldRenderContext context) {
        this.context = context;
        this.matrices = context.matrixStack();
        this.renderer = new Renderer(context);
    }

    /** Flushes the shared level buffer after all modules have emitted geometry. */
    public void flush() {
        if (context.consumers() instanceof VertexConsumerProvider.Immediate bufferSource) {
            bufferSource.draw();
        }
    }

    public static final class Renderer {
        private final WorldRenderContext context;
        private final MatrixStack matrices;
        private final Vec3d cameraPosition;

        private Renderer(WorldRenderContext context) {
            this.context = context;
            this.matrices = context.matrixStack();
            Camera camera = context.camera();
            this.cameraPosition = camera == null ? Vec3d.ZERO : camera.getPos();
        }

        public void box(Box box, Color sideColor, Color lineColor, ShapeMode mode, int ignoredFlags) {
            if (box == null || mode == null) return;
            if (mode == ShapeMode.Sides || mode == ShapeMode.Both) {
                drawFaces(box, sideColor);
            }
            if (mode == ShapeMode.Lines || mode == ShapeMode.Both) {
                drawEdges(box, lineColor == null ? sideColor : lineColor);
            }
        }

        public void boxSides(double minX, double minY, double minZ,
                             double maxX, double maxY, double maxZ,
                             Color color, int flags) {
            box(new Box(minX, minY, minZ, maxX, maxY, maxZ), color, null, ShapeMode.Sides, flags);
        }

        public void boxLines(double minX, double minY, double minZ,
                             double maxX, double maxY, double maxZ,
                             Color color, int flags) {
            box(new Box(minX, minY, minZ, maxX, maxY, maxZ), null, color, ShapeMode.Lines, flags);
        }

        public void line(double x1, double y1, double z1,
                         double x2, double y2, double z2, Color color) {
            if (color == null) return;
            VertexConsumer vertices = context.consumers().getBuffer(RenderLayer.getLines());
            MatrixStack.Entry pose = matrices.peek();
            float ax = relativeX(x1), ay = relativeY(y1), az = relativeZ(z1);
            float bx = relativeX(x2), by = relativeY(y2), bz = relativeZ(z2);
            float dx = bx - ax, dy = by - ay, dz = bz - az;
            float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length < 1.0e-5f) return;
            float nx = dx / length, ny = dy / length, nz = dz / length;
            vertices.vertex(pose.getPositionMatrix(), ax, ay, az)
                .color(color.r, color.g, color.b, color.a)
                .normal(pose.getNormalMatrix(), nx, ny, nz)
                .next();
            vertices.vertex(pose.getPositionMatrix(), bx, by, bz)
                .color(color.r, color.g, color.b, color.a)
                .normal(pose.getNormalMatrix(), nx, ny, nz)
                .next();
        }

        /** Compatibility no-op for older renderer call sites. */
        public void render(MatrixStack ignored) {
        }

        private void drawFaces(Box box, Color color) {
            if (color == null) return;
            VertexConsumer vertices = context.consumers().getBuffer(RenderLayer.getDebugFilledBox());
            MatrixStack.Entry pose = matrices.peek();
            float x1 = relativeX(box.minX), y1 = relativeY(box.minY), z1 = relativeZ(box.minZ);
            float x2 = relativeX(box.maxX), y2 = relativeY(box.maxY), z2 = relativeZ(box.maxZ);
            int argb = color.argb();
            vertex(vertices, pose, x1, y1, z1, argb); vertex(vertices, pose, x2, y1, z1, argb);
            vertex(vertices, pose, x2, y1, z2, argb); vertex(vertices, pose, x1, y1, z2, argb);
            vertex(vertices, pose, x1, y2, z1, argb); vertex(vertices, pose, x1, y2, z2, argb);
            vertex(vertices, pose, x2, y2, z2, argb); vertex(vertices, pose, x2, y2, z1, argb);
            vertex(vertices, pose, x1, y1, z2, argb); vertex(vertices, pose, x2, y1, z2, argb);
            vertex(vertices, pose, x2, y2, z2, argb); vertex(vertices, pose, x1, y2, z2, argb);
            vertex(vertices, pose, x2, y1, z1, argb); vertex(vertices, pose, x1, y1, z1, argb);
            vertex(vertices, pose, x1, y2, z1, argb); vertex(vertices, pose, x2, y2, z1, argb);
            vertex(vertices, pose, x1, y1, z1, argb); vertex(vertices, pose, x1, y1, z2, argb);
            vertex(vertices, pose, x1, y2, z2, argb); vertex(vertices, pose, x1, y2, z1, argb);
            vertex(vertices, pose, x2, y1, z2, argb); vertex(vertices, pose, x2, y1, z1, argb);
            vertex(vertices, pose, x2, y2, z1, argb); vertex(vertices, pose, x2, y2, z2, argb);
        }

        private void drawEdges(Box box, Color color) {
            if (color == null) return;
            VertexConsumer vertices = context.consumers().getBuffer(RenderLayer.getLines());
            MatrixStack.Entry pose = matrices.peek();
            Box relative = new Box(
                box.minX - cameraPosition.x, box.minY - cameraPosition.y, box.minZ - cameraPosition.z,
                box.maxX - cameraPosition.x, box.maxY - cameraPosition.y, box.maxZ - cameraPosition.z
            );
            WorldRenderer.drawBox(
                matrices,
                vertices,
                relative,
                color.r / 255.0F,
                color.g / 255.0F,
                color.b / 255.0F,
                color.a / 255.0F
            );
        }

        private static void vertex(VertexConsumer vertices, MatrixStack.Entry pose,
                                   float x, float y, float z, int color) {
            vertices.vertex(pose.getPositionMatrix(), x, y, z)
                .color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >>> 24)
                .next();
        }

        private float relativeX(double x) { return (float) (x - cameraPosition.x); }
        private float relativeY(double y) { return (float) (y - cameraPosition.y); }
        private float relativeZ(double z) { return (float) (z - cameraPosition.z); }
    }
}
