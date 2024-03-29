package de.maxhenkel.characterrenderer.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import de.maxhenkel.characterrenderer.EntityPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class RenderManager {
    private static int renderFBO = -1;
    private static int renderTexture = -1;
    private static int renderDepthBuffer = -1;

    private static final ConcurrentLinkedQueue<RenderObject> toRender = new ConcurrentLinkedQueue<>();

    public static class RenderObject {
        int x;
        int y;
        EntityPose playerPose;
        LivingEntity entity;
        File output;
        Consumer<RenderResult> callback;
        boolean replaceExisting;

        RenderObject(int x, int y, EntityPose playerPose, LivingEntity entity, File outputFile, boolean replaceExisting, Consumer<RenderResult> callback) {
            this.x = x;
            this.y = y;
            this.playerPose = playerPose;
            this.entity = entity;
            this.output = outputFile;
            this.replaceExisting = replaceExisting;
            this.callback = callback;
        }
    }

    public static class RenderResult {
        public State err;
        public int err_code; //set if FBO_ERROR, or OPENGL_ERROR
        public File result; //set to the same file that was passed in

        public enum State {
            SUCCESS,
            FILE_ALREADY_EXISTS,
            OPENGL_ERROR,
            FBO_ERROR,
            FILESYSTEM_ERROR
        }

        private RenderResult(RenderObject renderObject, State err) {
            this.err = err;
            this.err_code = -1;
            this.result = renderObject.output;
        }

        private RenderResult(RenderObject renderObject, State err, int err_code) {
            this.err = err;
            this.err_code = err_code;
            this.result = renderObject.output;
        }
    }

    public static void enqeueRender(int x, int y, LivingEntity entity, EntityPose playerPose, File outputFile, Consumer<RenderResult> callback) {
        enqeueRender(x, y, entity, playerPose, outputFile, callback, false);
    }

    public static void enqeueRender(int x, int y, LivingEntity entity, EntityPose playerPose, File outputFile, Consumer<RenderResult> callback, boolean replaceExisting /* = false */) {
        toRender.add(new RenderObject(x, y, playerPose, entity, outputFile, replaceExisting, callback));
    }

    public static void doAllRenders() {
        while (toRender.size() > 0) {
            RenderObject obj = toRender.poll();
            try {
                doRender(obj);
            } catch (Exception e) {
                PoseStack poseStack = RenderSystem.getModelViewStack();
                poseStack.setIdentity();
                e.printStackTrace();
            }

        }
    }

    public static void doRender(RenderObject renderObject) throws Exception {
        int x = renderObject.x;
        int y = renderObject.y;

        if (renderFBO == -1) {
            renderFBO = GL30.glGenFramebuffers();
            checkGLError(renderObject);
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, renderFBO);
        if (renderTexture == -1) {
            renderTexture = GL30.glGenTextures();
            checkGLError(renderObject);
        }
        if (renderDepthBuffer == -1) {
            renderDepthBuffer = GL30.glGenRenderbuffers();
            checkGLError(renderObject);
        }

        GL30.glBindTexture(GL11.GL_TEXTURE_2D, renderTexture);
        GL30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, x, y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0);
        GL30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderDepthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, x, y);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, renderDepthBuffer);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, renderTexture, 0);
        checkGLError(renderObject);
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            renderObject.callback.accept(new RenderResult(renderObject, RenderResult.State.FBO_ERROR, status));
            throw new IllegalStateException("OpenGL FBO Error, " + status);
        }
        GL30.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
        GL30.glViewport(0, 0, x, y);
        GL30.glClearColor(0F, 0F, 0F, 0F);
        GL30.glClear(GL11.GL_COLOR_BUFFER_BIT);
        checkGLError(renderObject);
        RenderSystem.clear(256, Minecraft.ON_OSX);
        int scale = y - 400;
        int k = scale * 8;
        Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, x, 0.0F, y, 0, k);
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.DISTANCE_TO_ORIGIN);
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.setIdentity();
        poseStack.translate(0D, 0D, -(k / 2D));
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        renderEntityInInventory(x / 2, y - 200, scale, renderObject.entity, renderObject.playerPose);
        RenderSystem.replayQueue();
        ByteBuffer render = ByteBuffer.allocateDirect(x * y * 4);
        GL30.glFlush();
        checkGLError(renderObject);
        GL11.glReadPixels(0, 0, x, y, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, render);
        checkGLError(renderObject);
        BufferedImage image = new BufferedImage(x, y, BufferedImage.TYPE_4BYTE_ABGR);
        byte[] imgData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        for (int ix = 0; ix < x; ix++) {
            for (int iy = 0; iy < y; iy++) {
                int pxByte = ((iy * x) + ix) * 4;
                imgData[pxByte] = render.get(pxByte + 3);
                imgData[pxByte + 1] = render.get(pxByte + 2);
                imgData[pxByte + 2] = render.get(pxByte + 1);
                imgData[pxByte + 3] = render.get(pxByte);
            }
        }
        if (renderObject.output.exists()) {
            if (!renderObject.replaceExisting) {
                renderObject.callback.accept(new RenderResult(renderObject, RenderResult.State.FILE_ALREADY_EXISTS));
                throw new FileAlreadyExistsException(renderObject.output.getAbsolutePath());
            }
            if (!renderObject.output.delete()) {
                renderObject.callback.accept(new RenderResult(renderObject, RenderResult.State.FILESYSTEM_ERROR));
                throw new IllegalStateException();
            }
        }
        try {
            if (!renderObject.output.createNewFile()) {
                renderObject.callback.accept(new RenderResult(renderObject, RenderResult.State.FILESYSTEM_ERROR));
                throw new IllegalStateException();
            }
            ImageIO.write(image, "png", renderObject.output);
        } catch (IOException e) {
            renderObject.callback.accept(new RenderResult(renderObject, RenderResult.State.FILESYSTEM_ERROR));
            throw new RuntimeException(e);
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        checkGLError(renderObject);
        poseStack.setIdentity();
        renderObject.callback.accept(new RenderResult(renderObject, RenderResult.State.SUCCESS));
    }

    private static void checkGLError(RenderObject renderObject) throws IllegalStateException {
        int err = GL11.glGetError();
        if (err != GL11.GL_NO_ERROR) {
            renderObject.callback.accept(new RenderResult(renderObject, RenderResult.State.OPENGL_ERROR, err));
            throw new IllegalStateException("OpenGL Error, " + err);
        }
    }

    public static void renderEntityInInventory(int posX, int posY, int scaleInt, LivingEntity entity, EntityPose entityPose) {
        float scale = ((float) scaleInt) * entityPose.scale;
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(posX, posY, 1500D);
        poseStack.scale(1F, 1F, -1F);
        RenderSystem.applyModelViewMatrix();
        PoseStack playerPoseStack = new PoseStack();
        playerPoseStack.translate(0D, 0D, 1050D);
        playerPoseStack.scale(scale, scale, scale);

        Quaternionf playerRotation = rotationDegrees(ZP, 180F);
        Quaternionf cameraOrientation = rotationDegrees(XP, entityPose.bodyRotationY);
        playerRotation.mul(cameraOrientation);
        playerPoseStack.mulPose(playerRotation);
        float yBodyRot = entity.yBodyRot;
        float entityYRot = entity.getYRot();
        float entityXRot = entity.getXRot();
        float yHeadRotO = entity.yHeadRotO;
        float yHeadRot = entity.yHeadRot;
        entity.yBodyRot = 180F + entityPose.bodyRotationX;
        entity.setYRot(180F + entityPose.headRotationX);
        entity.setXRot(-entityPose.headRotationY);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher renderer = Minecraft.getInstance().getEntityRenderDispatcher();
        cameraOrientation.conjugate();
        renderer.overrideCameraOrientation(cameraOrientation);
        renderer.setRenderShadow(false);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            renderer.render(entity, 0D, 0D, 0D, 0F, 1F, playerPoseStack, bufferSource, 15728880);
        });
        bufferSource.endBatch();
        renderer.setRenderShadow(true);
        entity.yBodyRot = yBodyRot;
        entity.setYRot(entityYRot);
        entity.setXRot(entityXRot);
        entity.yHeadRotO = yHeadRotO;
        entity.yHeadRot = yHeadRot;
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    public static Vector3f XP = new Vector3f(1F, 0F, 0F);
    public static Vector3f ZP = new Vector3f(0F, 0F, 1F);

    private static Quaternionf rotationDegrees(Vector3f vec, float degrees) {
        degrees *= ((float) Math.PI / 180F);

        float f = sin(degrees / 2F);
        float i = vec.x() * f;
        float j = vec.y() * f;
        float k = vec.z() * f;
        float r = cos(degrees / 2F);

        return new Quaternionf(i, j, k, r);
    }

    private static float cos(float p_80152_) {
        return (float) Math.cos((double) p_80152_);
    }

    private static float sin(float p_80155_) {
        return (float) Math.sin((double) p_80155_);
    }

}
