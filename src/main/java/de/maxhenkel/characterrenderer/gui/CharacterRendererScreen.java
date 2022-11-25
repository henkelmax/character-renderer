package de.maxhenkel.characterrenderer.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.characterrenderer.CharacterRenderer;
import de.maxhenkel.characterrenderer.EntityPose;
import de.maxhenkel.characterrenderer.entity.DummyPlayer;
import de.maxhenkel.characterrenderer.render.RenderManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.phys.AABB;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CharacterRendererScreen extends ScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(CharacterRenderer.MODID, "textures/gui/renderer.png");
    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");

    private EntityPose entityPose;
    private LivingEntity entity;

    private Button headButton;
    private Button bodyButton;

    private Button hatButton;
    private Button capeButton;
    private Button jacketButton;
    private Button sleeveButton;
    private Button pantsButton;

    private boolean modifyHead;
    private boolean modifyBody;

    public CharacterRendererScreen() {
        this(new DummyPlayer(Minecraft.getInstance().getUser().getGameProfile(), Minecraft.getInstance().player));
    }

    public CharacterRendererScreen(LivingEntity entity) {
        super(Component.translatable("gui.characterrenderer.renderer"), 274, 204);
        entityPose = new EntityPose();
        this.entity = entity;
        AABB boundingBox = entity.getBoundingBox();
        if (boundingBox == null) {
            entityPose.scale = 0.5F;
        } else {
            double maxSize = Math.max(boundingBox.getXsize(), Math.max(boundingBox.getYsize(), boundingBox.getZsize()));
            entityPose.scale = (float) (1D / maxSize);

            if (boundingBox.getXsize() >= boundingBox.getYsize() && boundingBox.getZsize() >= boundingBox.getYsize()) {
                entityPose.scale *= 0.75F;
            }
        }
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

        int posY = guiTop + 20;

        hatButton = new Button(guiLeft + xSize - 10 - 40, posY, 40, 20, Component.translatable("message.characterrenderer.hat"), button -> {
            togglePart(PlayerModelPart.HAT);
        });
        addRenderableWidget(hatButton);
        posY += 25;

        capeButton = new Button(guiLeft + xSize - 10 - 40, posY, 40, 20, Component.translatable("message.characterrenderer.cape"), button -> {
            togglePart(PlayerModelPart.CAPE);
        });
        addRenderableWidget(capeButton);
        posY += 25;

        jacketButton = new Button(guiLeft + xSize - 10 - 40, posY, 40, 20, Component.translatable("message.characterrenderer.jacket"), button -> {
            togglePart(PlayerModelPart.JACKET);
        });
        addRenderableWidget(jacketButton);
        posY += 25;

        sleeveButton = new Button(guiLeft + xSize - 10 - 40, posY, 40, 20, Component.translatable("message.characterrenderer.sleeves"), button -> {
            togglePart(PlayerModelPart.LEFT_SLEEVE);
            togglePart(PlayerModelPart.RIGHT_SLEEVE);
        });
        addRenderableWidget(sleeveButton);
        posY += 25;

        pantsButton = new Button(guiLeft + xSize - 10 - 40, posY, 40, 20, Component.translatable("message.characterrenderer.pants"), button -> {
            togglePart(PlayerModelPart.LEFT_PANTS_LEG);
            togglePart(PlayerModelPart.RIGHT_PANTS_LEG);
        });
        addRenderableWidget(pantsButton);

        modifyBody = true;
        bodyButton.active = false;

        addRenderableWidget(new Button(guiLeft + 10, guiTop + ySize - 5 - 20, xSize - 20, 20, Component.translatable("message.characterrenderer.render"), button -> {
            Path outputFile = CharacterRenderer.CLIENT_CONFIG.getSaveFolder().resolve("%s.png".formatted(FILE_DATE_FORMAT.format(Calendar.getInstance().getTime())));

            RenderManager.enqeueRender(CharacterRenderer.CLIENT_CONFIG.renderWidth.get(), CharacterRenderer.CLIENT_CONFIG.renderHeight.get(), entity, entityPose, outputFile.toFile(), (result) -> {
                if (result.err.equals(RenderManager.RenderResult.State.SUCCESS)) {
                    sendMessage(Component.translatable("message.characterrenderer.render_success", Component.literal(result.result.getName())
                            .withStyle(ChatFormatting.UNDERLINE)
                            .withStyle(style -> style
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.characterrenderer.open_folder")))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, result.result.toPath().getParent().normalize().toString()))))
                    );
                } else {
                    sendMessage(Component.translatable("message.characterrenderer.render_error", Component.literal(result.err.toString()).withStyle(ChatFormatting.GRAY)));
                }
            }, true);
        }));

        boolean isPlayer = entity instanceof DummyPlayer;
        hatButton.active = isPlayer;
        capeButton.active = isPlayer;
        jacketButton.active = isPlayer;
        sleeveButton.active = isPlayer;
        pantsButton.active = isPlayer;
    }

    private void togglePart(PlayerModelPart part) {
        if (entity instanceof DummyPlayer player) {
            player.showPart(part, !player.isModelPartShown(part));
        }
    }

    private boolean isPartShown(PlayerModelPart part) {
        if (entity instanceof DummyPlayer player) {
            return player.isModelPartShown(part);
        }
        return false;
    }

    private void sendMessage(Component component) {
        CharacterRenderer.LOGGER.info(component.getString());
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        player.sendSystemMessage(component);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        int titleWidth = font.width(getTitle());
        font.draw(matrixStack, getTitle().getVisualOrderText(), guiLeft + (xSize - titleWidth) / 2, guiTop + 7, FONT_COLOR);

        fill(matrixStack, guiLeft + 60, guiTop + 20, guiLeft + xSize - 60, guiTop + ySize - 30, 0xFFFFFFFF);
        RenderManager.renderEntityInInventory(guiLeft + xSize / 2, guiTop + ySize - 45, 128, entity, entityPose);
    }

    @Override
    public boolean mouseDragged(double posX, double posY, int button, double deltaX, double deltaY) {
        if (modifyHead) {
            entityPose.headRotationX -= deltaX;
            entityPose.headRotationY = (float) Math.min(Math.max(entityPose.headRotationY - deltaY, -90F), 90F);
        }
        if (modifyBody) {
            entityPose.bodyRotationX -= deltaX;
            entityPose.bodyRotationY = (float) Math.min(Math.max(entityPose.bodyRotationY - deltaY, -45F), 45F);
        }
        return super.mouseDragged(posX, posY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        if (Screen.hasShiftDown()) {
            amount /= 5D;
        }

        entityPose.scale = (float) Math.min(Math.max(entityPose.scale + amount / 50F, 0.01F), 10F);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
