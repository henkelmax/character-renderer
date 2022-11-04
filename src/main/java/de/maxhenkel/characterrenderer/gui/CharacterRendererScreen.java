package de.maxhenkel.characterrenderer.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import de.maxhenkel.characterrenderer.CharacterRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class CharacterRendererScreen extends ScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(CharacterRenderer.MODID, "textures/gui/renderer.png");

    private float headRotationX;
    private float headRotationY;

    private float bodyRotationX;
    private float bodyRotationY;

    private Button headButton;
    private Button bodyButton;

    private boolean modifyHead;
    private boolean modifyBody;

    public CharacterRendererScreen() {
        super(Component.translatable("gui.characterrenderer.renderer"), 248, 204);
    }

    @Override
    protected void init() {
        super.init();

        headButton = new Button(guiLeft + 10, guiTop + 20, 40, 20, Component.translatable("message.characterrenderer.head"), button -> {
            modifyHead = true;
            modifyBody = false;
            headButton.active = false;
            bodyButton.active = true;
        });
        addRenderableWidget(headButton);

        bodyButton = new Button(guiLeft + 10, guiTop + 45, 40, 20, Component.translatable("message.characterrenderer.body"), button -> {
            modifyHead = false;
            modifyBody = true;
            headButton.active = true;
            bodyButton.active = false;
        });
        addRenderableWidget(bodyButton);

        modifyHead = true;
        headButton.active = false;

        addRenderableWidget(new Button(guiLeft + 10, guiTop + ySize - 5 - 20, xSize - 20, 20, Component.translatable("message.characterrenderer.render"), button -> {
            //TODO Render character
        }));
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        int titleWidth = font.width(getTitle());
        font.draw(matrixStack, getTitle().getVisualOrderText(), guiLeft + (xSize - titleWidth) / 2, guiTop + 7, FONT_COLOR);

        fill(matrixStack, guiLeft + 60, guiTop + 20, guiLeft + xSize - 60, guiTop + ySize - 30, 0xFFFFFFFF);
        renderEntityInInventory(guiLeft + xSize / 2, guiTop + ySize - 45, 65, minecraft.player);
    }

    @Override
    public boolean mouseDragged(double posX, double posY, int button, double deltaX, double deltaY) {
        if (modifyHead) {
            headRotationX -= deltaX;
            headRotationY = (float) Math.min(Math.max(headRotationY - deltaY, -90F), 90F);
        }
        if (modifyBody) {
            bodyRotationX -= deltaX;
            bodyRotationY = (float) Math.min(Math.max(bodyRotationY - deltaY, -45F), 45F);
        }
        return super.mouseDragged(posX, posY, button, deltaX, deltaY);
    }

    public void renderEntityInInventory(int posX, int posY, int scale, LivingEntity entity) {
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(posX, posY, 1500D);
        poseStack.scale(1F, 1F, -1F);
        RenderSystem.applyModelViewMatrix();
        PoseStack playerPose = new PoseStack();
        playerPose.translate(0D, 0D, 1050D);
        playerPose.scale((float) scale, (float) scale, (float) scale);
        Quaternion playerRotation = Vector3f.ZP.rotationDegrees(180F);
        Quaternion cameraOrientation = Vector3f.XP.rotationDegrees(bodyRotationY);
        playerRotation.mul(cameraOrientation);
        playerPose.mulPose(playerRotation);
        float yBodyRot = entity.yBodyRot;
        float entityYRot = entity.getYRot();
        float entityXRot = entity.getXRot();
        float yHeadRotO = entity.yHeadRotO;
        float yHeadRot = entity.yHeadRot;
        entity.yBodyRot = 180F + bodyRotationX;
        entity.setYRot(180F + headRotationX);
        entity.setXRot(-headRotationY);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher renderer = Minecraft.getInstance().getEntityRenderDispatcher();
        cameraOrientation.conj();
        renderer.overrideCameraOrientation(cameraOrientation);
        renderer.setRenderShadow(false);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            renderer.render(entity, 0D, 0D, 0D, 0F, 1F, playerPose, bufferSource, 15728880);
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

}
