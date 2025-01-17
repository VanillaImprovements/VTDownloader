package me.bymartrixx.vvd.gui.widget;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.bymartrixx.vvd.VVDMod;
import me.bymartrixx.vvd.gui.VVDScreen;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;

public class PackListWidget extends EntryListWidget<PackListWidget.PackEntry> {
    public final String categoryName;
    public final boolean oneEntry; // If it should keep only one entry selected at once
    private final boolean displayEntries;

    public PackListWidget(JsonArray packs, String categoryName) {
        super(VVDScreen.getInstance().getClient(), VVDScreen.getInstance().width - 180, VVDScreen.getInstance().height,
                60, VVDScreen.getInstance().height - 40, 32);
        this.displayEntries = true;

        this.setRenderHeader(true, 16);

        this.categoryName = categoryName;
        this.oneEntry = this.categoryName.equals("Menu Panoramas") || this.categoryName.equals("Options Background");

        for (int i = 0; i < packs.size(); ++i) {
            JsonObject pack = packs.get(i).getAsJsonObject();

            this.addEntry(new PackListWidget.PackEntry(pack));
        }
    }

    public PackListWidget() {
        super(VVDScreen.getInstance().getClient(), VVDScreen.getInstance().width - 180, VVDScreen.getInstance().height,
                60, VVDScreen.getInstance().height - 40, 32);
        this.displayEntries = false;
        this.categoryName = "Error!";
        this.oneEntry = false;
    }

    public int getRowWidth() {
        return this.width - 20;
    }

    protected int getScrollbarPositionX() {
        return this.width - 10;
    }

    // TODO: Incompatible packs warning

    public void setSelected(@Nullable PackListWidget.PackEntry entry) {
        this.setSelected(entry, true);
    }

    public void setSelected(@Nullable PackListWidget.PackEntry entry, boolean child) {
        if (entry == null)
            return;

        if (this.children().contains(entry) || !child) {
            String packName = entry.name;
            if (!VVDScreen.getInstance().isPackSelected(this.categoryName, packName)) {
                VVDScreen.getInstance().addSelectedPack(this.categoryName, packName, this.oneEntry);
            } else {
                VVDScreen.getInstance().removeSelectedPack(this.categoryName, packName);
            }
        }
    }

    public boolean isSelected(PackListWidget.PackEntry entry) {
        if (!this.children().contains(entry))
            return false;

        return VVDScreen.getInstance().isPackSelected(this.categoryName, entry.name);
    }

    protected boolean isSelectedEntry(int index) {
        return this.isSelected(this.children().get(index));
    }

    protected void renderHeader(MatrixStack matrices, int x, int y, Tessellator tessellator) {
        Text text = new LiteralText(this.categoryName).formatted(Formatting.BOLD, Formatting.UNDERLINE);
        VVDScreen.getInstance().getTextRenderer().draw(matrices, text,
                ((float) (this.width / 2 - VVDScreen.getInstance().getTextRenderer().getWidth(text) / 2)),
                Math.min(this.top + 3, y), 16777215);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.displayEntries) {
            // super.render(matrices, mouseX, mouseY, delta);

            // Cursed fix for #2, copied from EntryListWidget#render, TODO: Remove
            this.renderBackground(matrices);
            int i = this.getScrollbarPositionX();
            int j = i + 6;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            // this.hoveredEntry = (E)(this.isMouseOver(mouseX, mouseY) ?
            // this.getEntryAtPosition(mouseX, mouseY) : null);
            // if (this.renderBackground) { // Private field, true
            RenderSystem.setShaderTexture(0, DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            float f = 32.0F;
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(this.left, this.bottom, 0.0)
                    .texture((float) this.left / 32.0F, (float) (this.bottom + (int) this.getScrollAmount()) / 32.0F)
                    .color(32, 32, 32, 255).next();
            bufferBuilder.vertex(this.right, this.bottom, 0.0)
                    .texture((float) this.right / 32.0F, (float) (this.bottom + (int) this.getScrollAmount()) / 32.0F)
                    .color(32, 32, 32, 255).next();
            bufferBuilder.vertex(this.right, this.top, 0.0)
                    .texture((float) this.right / 32.0F, (float) (this.top + (int) this.getScrollAmount()) / 32.0F)
                    .color(32, 32, 32, 255).next();
            bufferBuilder.vertex(this.left, this.top, 0.0)
                    .texture((float) this.left / 32.0F, (float) (this.top + (int) this.getScrollAmount()) / 32.0F)
                    .color(32, 32, 32, 255).next();
            tessellator.draw();
            // }

            int k = this.getRowLeft();
            int l = this.top + 4 - (int) this.getScrollAmount();
            // if (this.renderHeader) { // Private field, true
            this.renderHeader(matrices, k, l, tessellator);
            // }

            this.renderList(matrices, k, l, mouseX, mouseY, delta);
            // if (this.renderHorizontalShadows) { // Private field, true
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            float g = 32.0F;
            int m = -100;
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(this.left, this.top, -100.0).texture(0.0F, (float) this.top / 32.0F)
                    .color(64, 64, 64, 255).next();
            bufferBuilder.vertex(this.left + this.width, this.top, -100.0)
                    .texture((float) this.width / 32.0F, (float) this.top / 32.0F).color(64, 64, 64, 255).next();
            bufferBuilder.vertex(this.left + this.width, 0.0, -100.0).texture((float) this.width / 32.0F, 0.0F)
                    .color(64, 64, 64, 255).next();
            bufferBuilder.vertex(this.left, 0.0, -100.0).texture(0.0F, 0.0F).color(64, 64, 64, 255).next();
            bufferBuilder.vertex(this.left, this.height, -100.0).texture(0.0F, (float) this.height / 32.0F)
                    .color(64, 64, 64, 255).next();
            bufferBuilder.vertex(this.left + this.width, this.height, -100.0)
                    .texture((float) this.width / 32.0F, (float) this.height / 32.0F).color(64, 64, 64, 255).next();
            bufferBuilder.vertex(this.left + this.width, this.bottom, -100.0)
                    .texture((float) this.width / 32.0F, (float) this.bottom / 32.0F).color(64, 64, 64, 255).next();
            bufferBuilder.vertex(this.left, this.bottom, -100.0).texture(0.0F, (float) this.bottom / 32.0F)
                    .color(64, 64, 64, 255).next();
            tessellator.draw();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.class_4535.SRC_ALPHA,
                    GlStateManager.class_4534.ONE_MINUS_SRC_ALPHA, GlStateManager.class_4535.ZERO,
                    GlStateManager.class_4534.ONE);
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            int n = 4;
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(this.left, this.top + 4, 0.0).color(0, 0, 0, 0).next();
            bufferBuilder.vertex(this.right, this.top + 4, 0.0).color(0, 0, 0, 0).next();
            bufferBuilder.vertex(this.right, this.top, 0.0).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(this.left, this.top, 0.0).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(this.left, this.bottom, 0.0).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(this.right, this.bottom, 0.0).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(this.right, this.bottom - 4, 0.0).color(0, 0, 0, 0).next();
            bufferBuilder.vertex(this.left, this.bottom - 4, 0.0).color(0, 0, 0, 0).next();
            tessellator.draw();
            // }

            int o = this.getMaxScroll();
            if (o > 0) {
                RenderSystem.disableTexture();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                int p = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top))
                        / (float) this.getMaxPosition());
                p = MathHelper.clamp(p, 32, this.bottom - this.top - 8);
                int q = (int) this.getScrollAmount() * (this.bottom - this.top - p) / o + this.top;
                if (q < this.top) {
                    q = this.top;
                }

                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                bufferBuilder.vertex(i, this.bottom, 0.0).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(j, this.bottom, 0.0).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(j, this.top, 0.0).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(i, this.top, 0.0).color(0, 0, 0, 255).next();
                bufferBuilder.vertex(i, q + p, 0.0).color(128, 128, 128, 255).next();
                bufferBuilder.vertex(j, q + p, 0.0).color(128, 128, 128, 255).next();
                bufferBuilder.vertex(j, q, 0.0).color(128, 128, 128, 255).next();
                bufferBuilder.vertex(i, q, 0.0).color(128, 128, 128, 255).next();
                bufferBuilder.vertex(i, q + p - 1, 0.0).color(192, 192, 192, 255).next();
                bufferBuilder.vertex(j - 1, q + p - 1, 0.0).color(192, 192, 192, 255).next();
                bufferBuilder.vertex(j - 1, q, 0.0).color(192, 192, 192, 255).next();
                bufferBuilder.vertex(i, q, 0.0).color(192, 192, 192, 255).next();
                tessellator.draw();
            }

            this.renderDecorations(matrices, mouseX, mouseY);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            // End of cursed fix for #2
        } else {
            Text msgHeader = new TranslatableText("vvd.packError.title.1").formatted(Formatting.BOLD,
                    Formatting.ITALIC);
            Text msgHeader2 = new TranslatableText("vvd.packError.title.2").formatted(Formatting.BOLD,
                    Formatting.ITALIC);
            Text msgBody = new TranslatableText("vvd.packError.body.1");
            Text msgBody2 = new TranslatableText("vvd.packError.body.2");
            Text msgBody3 = new TranslatableText("vvd.packError.body.3", VVDMod.BASE_URL);

            VVDScreen.getInstance().getTextRenderer().draw(matrices, msgHeader,
                    ((float) (this.width / 2 - VVDScreen.getInstance().getTextRenderer().getWidth(msgHeader) / 2)),
                    ((float) (this.height / 2) - 32), 16777215);
            VVDScreen.getInstance().getTextRenderer().draw(matrices, msgHeader2,
                    ((float) (this.width / 2 - VVDScreen.getInstance().getTextRenderer().getWidth(msgHeader2) / 2)),
                    ((float) (this.height / 2) - 16), 16777215);
            VVDScreen.getInstance().getTextRenderer().draw(matrices, msgBody,
                    ((float) (this.width / 2 - VVDScreen.getInstance().getTextRenderer().getWidth(msgBody) / 2)),
                    ((float) (this.height / 2)), 16777215);
            VVDScreen.getInstance().getTextRenderer().draw(matrices, msgBody2,
                    ((float) (this.width / 2 - VVDScreen.getInstance().getTextRenderer().getWidth(msgBody2) / 2)),
                    ((float) (this.height / 2) + 16), 16777215);
            VVDScreen.getInstance().getTextRenderer().draw(matrices, msgBody3,
                    ((float) (this.width / 2 - VVDScreen.getInstance().getTextRenderer().getWidth(msgBody3) / 2)),
                    ((float) (this.height / 2) + 32), 16777215);
        }
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }

    public class PackEntry extends EntryListWidget.Entry<PackListWidget.PackEntry> {
        public final String name;
        public final String displayName;
        public final String description;
        public final String[] incompatiblePacks;

        PackEntry(JsonObject pack) {
            this.name = pack.get("name").getAsString();

            this.displayName = pack.get("display").getAsString();
            this.description = pack.get("description").getAsString();

            Iterator<JsonElement> incompatiblePacksIterator = pack.get("incompatible").getAsJsonArray().iterator();
            ArrayList<String> incompatiblePacks = new ArrayList<>();

            while (incompatiblePacksIterator.hasNext()) {
                incompatiblePacks.add(incompatiblePacksIterator.next().getAsString());
            }

            this.incompatiblePacks = incompatiblePacks.toArray(new String[0]);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                this.setSelected();
            }

            return false;
        }

        private void setSelected() {
            PackListWidget.this.setSelected(this);
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
                int mouseY, boolean hovered, float tickDelta) {
            VVDScreen.getInstance().getTextRenderer().drawWithShadow(matrices, this.displayName,
                    ((float) (PackListWidget.this.width / 2
                            - VVDScreen.getInstance().getTextRenderer().getWidth(this.displayName) / 2)),
                    y + 1, 16777215);
            this.renderDescription(matrices, y);
        }

        private void renderDescription(MatrixStack matrices, int y) {
            int textWidth = VVDScreen.getInstance().getTextRenderer().getWidth(this.description);
            int maxWidth = Math.min(280, PackListWidget.this.getRowWidth() - 4);

            if (textWidth > maxWidth) {
                String description = VVDScreen.getInstance().getTextRenderer().trimToWidth(this.description,
                        maxWidth - VVDScreen.getInstance().getTextRenderer().getWidth("...")) + "...";
                VVDScreen.getInstance().getTextRenderer().drawWithShadow(matrices, description,
                        ((float) (PackListWidget.this.width / 2
                                - VVDScreen.getInstance().getTextRenderer().getWidth(description) / 2)),
                        y + 13, 16777215);
            } else {
                VVDScreen.getInstance().getTextRenderer().drawWithShadow(matrices, this.description,
                        ((float) (PackListWidget.this.width / 2
                                - VVDScreen.getInstance().getTextRenderer().getWidth(this.description) / 2)),
                        y + 13, 16777215);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof String) {
                return equals((String) obj);
            }

            return super.equals(obj);
        }

        public boolean equals(String packName) {
            return this.name.equals(packName);
        }
    }
}
