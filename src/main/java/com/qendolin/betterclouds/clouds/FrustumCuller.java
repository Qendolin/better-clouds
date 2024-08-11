package com.qendolin.betterclouds.clouds;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Box;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

public class FrustumCuller {

    private final Matrix4d projection = new Matrix4d();
    private final Matrix4d view = new Matrix4d();
    private final Matrix4d rotation = new Matrix4d();
    private final Matrix4d inverseProjection = new Matrix4d();
    private final Matrix4d inverseView = new Matrix4d();
    private final Matrix4d inverseRotation = new Matrix4d();

    private final Vector3d origin = new Vector3d();
    private final Vector4d xzPlane = new Vector4d(0, 1, 0, 0);

    private final Vector4d tl = new Vector4d();
    private final Vector4d tr = new Vector4d();
    private final Vector4d bl = new Vector4d();
    private final Vector4d br = new Vector4d();

    private final Vector3d top = new Vector3d();
    private final Vector3d right = new Vector3d();
    private final Vector3d bottom = new Vector3d();
    private final Vector3d left = new Vector3d();

    private double zNear;
    private double zFar;

    public static boolean DEBUG_LOCK = false;

    public void update(Matrix4f viewMatrix, Vector3d cam, Matrix4f projectionMatrix, float height) {
        if(!DEBUG_LOCK) {
            projection.set(projectionMatrix);
            view.set(viewMatrix);
            rotation.set(viewMatrix).setTranslation(0, 0, 0);

            inverseProjection.set(projection).invert();
            inverseView.set(view).invert();
            inverseRotation.set(rotation).invert();

            origin.set(cam);
            xzPlane.w = height - origin.y;
        }

        Vector4d farPlane = new Vector4d(0, 0, 1, 1);
        Vector4d nearPlane = new Vector4d(0, 0, -1, 1);
        inverseProjection.transform(farPlane);
        inverseProjection.transform(nearPlane);
        zNear = -nearPlane.z / nearPlane.w;
        zFar = -farPlane.z / farPlane.w;

        tl.set(-1, +1, 1, 1);
        tr.set(+1, +1, 1, 1);
        bl.set(-1, -1, 1, 1);
        br.set(+1, -1, 1, 1);

        tl.mul(inverseProjection);
        tr.mul(inverseProjection);
        bl.mul(inverseProjection);
        br.mul(inverseProjection);

        tl.div(tl.w);
        tr.div(tr.w);
        bl.div(bl.w);
        br.div(br.w);

        tl.mul(inverseView);
        tr.mul(inverseView);
        bl.mul(inverseView);
        br.mul(inverseView);

        // Calculate the 4 bounding planes, go clock-wise so they point "inwards"
        // The planes go through the origin by definition
        Vector4d topFace = new Vector4d(
            tl.y * tr.z - tl.z * tr.y,
            tl.z * tr.x - tl.x * tr.z,
            tl.x * tr.y - tl.y * tr.x,
            0
        );
        Vector4d rightFace = new Vector4d(
            tr.y * br.z - tr.z * br.y,
            tr.z * br.x - tr.x * br.z,
            tr.x * br.y - tr.y * br.x,
            0
        );
        Vector4d bottomFace = new Vector4d(
            br.y * bl.z - br.z * bl.y,
            br.z * bl.x - br.x * bl.z,
            br.x * bl.y - br.y * bl.x,
            0
        );
        Vector4d leftFace = new Vector4d(
            bl.y * tl.z - bl.z * tl.y,
            bl.z * tl.x - bl.x * tl.z,
            bl.x * tl.y - bl.y * tl.x,
            0
        );


        // Calculate intersecting line based on https://math.stackexchange.com/q/475953/1014081
        Vector3d topIntersection = new Vector3d(
            topFace.y * xzPlane.z - topFace.z * xzPlane.y,
            topFace.z * xzPlane.x - topFace.x * xzPlane.z,
            topFace.x * xzPlane.y - topFace.y * xzPlane.x
        ).normalize();
        Vector3d topOrigin = new Vector3d(tl.x, tl.y, tl.z).mul(xzPlane.w / tl.y);
        Vector3d rightIntersection = new Vector3d(
            rightFace.y * xzPlane.z - rightFace.z * xzPlane.y,
            rightFace.z * xzPlane.x - rightFace.x * xzPlane.z,
            rightFace.x * xzPlane.y - rightFace.y * xzPlane.x
        ).normalize();
        Vector3d rightOrigin = new Vector3d(tr.x, tr.y, tr.z).mul(xzPlane.w / tr.y);
        Vector3d bottomIntersection = new Vector3d(
            bottomFace.y * xzPlane.z - bottomFace.z * xzPlane.y,
            bottomFace.z * xzPlane.x - bottomFace.x * xzPlane.z,
            bottomFace.x * xzPlane.y - bottomFace.y * xzPlane.x
        ).normalize();
        Vector3d bottomOrigin = new Vector3d(br.x, br.y, br.z).mul(xzPlane.w / br.y);
        Vector3d leftIntersection = new Vector3d(
            leftFace.y * xzPlane.z - leftFace.z * xzPlane.y,
            leftFace.z * xzPlane.x - leftFace.x * xzPlane.z,
            leftFace.x * xzPlane.y - leftFace.y * xzPlane.x
        ).normalize();
        Vector3d leftOrigin = new Vector3d(bl.x, bl.y, bl.z).mul(xzPlane.w / bl.y);

        // Calculate straight line equation in hesse normal form
        // Normals will point inward
        top.set(topIntersection.z, -topIntersection.x, 0);
        top.z = top.x * topOrigin.x + top.y * topOrigin.z; // dot product with one of the points gives the distance

        right.set(rightIntersection.z, -rightIntersection.x, 0);
        right.z = right.x * rightOrigin.x + right.y * rightOrigin.z;

        bottom.set(bottomIntersection.z, -bottomIntersection.x, 0);
        bottom.z = bottom.x * bottomOrigin.x + bottom.y * bottomOrigin.z;

        left.set(leftIntersection.z, -leftIntersection.x, 0);
        left.z = left.x * leftOrigin.x + left.y * leftOrigin.z;

        // https://math.stackexchange.com/q/1992153/1014081
        Vector2d topRightCorner = new Vector2d(
            (top.z * right.y - top.y * right.z) / (top.x * right.y - top.y * right.x),
            (top.x * right.z - top.z * right.x) / (top.x * right.y - top.y * right.x));

        Vector2d topLeftCorner = new Vector2d(
            (top.z * left.y - top.y * left.z) / (top.x * left.y - top.y * left.x),
            (top.x * left.z - top.z * left.x) / (top.x * left.y - top.y * left.x));

        Vector2d bottomLeftCorner = new Vector2d(
            (bottom.z * left.y - bottom.y * left.z) / (bottom.x * left.y - bottom.y * left.x),
            (bottom.x * left.z - bottom.z * left.x) / (bottom.x * left.y - bottom.y * left.x));

        Vector2d bottomRightCorner = new Vector2d(
            (bottom.z * right.y - bottom.y * right.z) / (bottom.x * right.y - bottom.y * right.x),
            (bottom.x * right.z - bottom.z * right.x) / (bottom.x * right.y - bottom.y * right.x));


        if(!DEBUG_LOCK) return;

        Matrix4f mat = new Matrix4f().translate((float) -cam.x, (float) -cam.y, (float) -cam.z);

        mat.translate((float) origin.x, (float) origin.y, (float) origin.z);

        var lines = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        // draw far-z square
        lines.vertex(mat, (float) tl.x, (float) tl.y, (float) tl.z)
            .color(0f,1f,1f,1f);
        lines.vertex(mat, (float) tr.x, (float) tr.y, (float) tr.z)
            .color(1f,1f,1f,1f);

        lines.vertex(mat, (float) tr.x, (float) tr.y, (float) tr.z)
            .color(1f,1f,1f,1f);
        lines.vertex(mat, (float) br.x, (float) br.y, (float) br.z)
            .color(1f,0f,1f,1f);

        lines.vertex(mat, (float) br.x, (float) br.y, (float) br.z)
            .color(1f,0f,1f,1f);
        lines.vertex(mat, (float) bl.x, (float) bl.y, (float) bl.z)
            .color(0f,0f,1f,1f);

        lines.vertex(mat, (float) bl.x, (float) bl.y, (float) bl.z)
            .color(0f,0f,1f,1f);
        lines.vertex(mat, (float) tl.x, (float) tl.y, (float) tl.z)
            .color(0f,1f,1f,1f);

        lines.vertex(mat, (float) bl.x, (float) bl.y, (float) bl.z)
            .color(0f,0f,0f,1f);
        lines.vertex(mat, (float) tr.x, (float) tr.y, (float) tr.z)
            .color(0f,0f,0f,1f);

        lines.vertex(mat, (float) br.x, (float) br.y, (float) br.z)
            .color(0f,0f,0f,1f);
        lines.vertex(mat, (float) tl.x, (float) tl.y, (float) tl.z)
            .color(0f,0f,0f,1f);

        // draw frustum edges
        lines.vertex(mat, 0, 0, 0)
            .color(0f,1f,0.0f,1f);
        lines.vertex(mat, (float) tl.x, (float) tl.y, (float) tl.z)
            .color(0f,1f,1.0f,1f);

        lines.vertex(mat, 0, 0, 0)
            .color(1f,1f,0.0f,1f);
        lines.vertex(mat, (float) tr.x, (float) tr.y, (float) tr.z)
            .color(1f,1f,1.0f,1f);

        lines.vertex(mat, 0, 0, 0)
            .color(1f,0f,0.0f,1f);
        lines.vertex(mat, (float) br.x, (float) br.y, (float) br.z)
            .color(1f,0f,1.0f,1f);

        lines.vertex(mat, 0, 0, 0)
            .color(0f,0f,0.0f,1f);
        lines.vertex(mat, (float) bl.x, (float) bl.y, (float) bl.z)
            .color(0f,0f,1.0f,1f);

        mat.translate((float) 0, (float) xzPlane.w, 0);

        lines.vertex(mat, (float) topLeftCorner.x, (float) 0, (float) topLeftCorner.y)
            .color(0.5f, 1f, 0.5f,1f);
        lines.vertex(mat, (float) topRightCorner.x, (float) 0, (float) topRightCorner.y)
            .color(0.5f, 1f, 0.5f,1f);

        lines.vertex(mat, (float) topRightCorner.x, (float) 0, (float) topRightCorner.y)
            .color(1.0f, 0.5f, 0.5f,1f);
        lines.vertex(mat, (float) bottomRightCorner.x, (float) 0, (float) bottomRightCorner.y)
            .color(1.0f, 0.5f, 0.5f,1f);

        lines.vertex(mat, (float) bottomRightCorner.x, (float) 0, (float) bottomRightCorner.y)
            .color(0.5f, 0f, 0.5f,1f);
        lines.vertex(mat, (float) bottomLeftCorner.x, (float) 0, (float) bottomLeftCorner.y)
            .color(0.5f, 0f, 0.5f,1f);

        lines.vertex(mat, (float) bottomLeftCorner.x, (float) 0, (float) bottomLeftCorner.y)
            .color(0f, 0.5f, 0.5f,1f);
        lines.vertex(mat, (float) topLeftCorner.x, (float) 0, (float) topLeftCorner.y)
            .color(0f, 0.5f, 0.5f,1f);

        lines.vertex(mat, (float) 0, (float) 0, (float) 0)
            .color(0f, 0f, 0f,1f);
        lines.vertex(mat, (float) 0, (float) 1, 0)
            .color(0f, 0f, 0f,1f);

        BuiltBuffer linesBuiltBuffer = lines.end();

        var faces = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // projected plane
        faces.vertex(mat, -100, 0, -100)
            .color(0.0f,0.0f,0.0f,0.1f)
            .vertex(mat, -100, 0, 100)
            .color(0.0f,0.0f,0.0f,0.1f)
            .vertex(mat, 100, 0, 100)
            .color(0.0f,0.0f,0.0f,0.1f)
            .vertex(mat, 100, 0, -100)
            .color(0.0f,0.0f,0.0f,0.1f);

        mat.translate((float) 0, (float) -xzPlane.w, 0);

        // frustum faces
        faces.vertex(mat, (float) tl.x, (float) tl.y, (float) tl.z)
            .color(0.5f,1f,0.5f,0.25f)
            .vertex(mat, 0, 0 ,0)
            .color(0.5f,1f,0.5f,0.25f)
            .vertex(mat, (float) tr.x, (float) tr.y, (float) tr.z)
            .color(0.5f,1f,0.5f,0.25f)
            .vertex(mat, 0,0,0)
            .color(0.5f,1f,0.5f,0.25f);

        faces.vertex(mat, (float) tr.x, (float) tr.y, (float) tr.z)
            .color(1f,0.5f,0.5f,0.25f)
            .vertex(mat, 0, 0 ,0)
            .color(1f,0.5f,0.5f,0.25f)
            .vertex(mat, (float) br.x, (float) br.y, (float) br.z)
            .color(1f,0.5f,0.5f,0.25f)
            .vertex(mat, 0,0,0)
            .color(1f,0.5f,0.5f,0.25f);

        faces.vertex(mat, (float) br.x, (float) br.y, (float) br.z)
            .color(0.5f,0f,0.5f,0.25f)
            .vertex(mat, 0, 0 ,0)
            .color(0.5f,0f,0.5f,0.25f)
            .vertex(mat, (float) bl.x, (float) bl.y, (float) bl.z)
            .color(0.5f,0f,0.5f,0.25f)
            .vertex(mat, 0,0,0)
            .color(0.5f,0f,0.5f,0.25f);

        faces.vertex(mat, (float) bl.x, (float) bl.y, (float) bl.z)
            .color(0.0f,0.5f,0.5f,0.25f)
            .vertex(mat, 0, 0 ,0)
            .color(0.0f,0.5f,0.5f,0.25f)
            .vertex(mat, (float) tl.x, (float) tl.y, (float) tl.z)
            .color(0.0f,0.5f,0.5f,0.25f)
            .vertex(mat, 0,0,0)
            .color(0.0f,0.5f,0.5f, 0.25f);

        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        GL32.glEnable(GL32.GL_DEPTH_CLAMP);
        BufferRenderer.drawWithGlobalProgram(linesBuiltBuffer);
        BufferRenderer.drawWithGlobalProgram(faces.end());
        GL32.glDisable(GL32.GL_DEPTH_CLAMP);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    public boolean test(Box box) {
        // FIXME: Bad assumption: Testing the corners is not enough. It is possible that only part of an edge intersects.
        return test(box.minX, box.minZ) || test(box.minX, box.maxZ) || test(box.maxX, box.minZ) || test(box.maxX, box.maxZ);
    }

    public boolean test(double x, double z) {
        x -= origin.x;
        z -= origin.z;
        double dTop = x * top.x + z * top.y - top.z;
        double dRight = x * right.x + z * right.y - right.z;
        double dBottom = x * bottom.x + z * bottom.y - bottom.z;
        double dLeft = x * left.x + z * left.y - left.z;
        return dTop > 0 && dRight > 0 && dBottom > 0 && dLeft > 0;
    }
}
