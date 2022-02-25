package me.bymartrixx.vvd.mixin;

import me.bymartrixx.vvd.gui.VVDScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(PackScreen.class)
public class PackScreenMixin extends Screen {
    private static final Text vvd_RESOURCE_PACK_SUBTITLE = new TranslatableText("vvd.resourcePack.subtitle")
            .formatted(Formatting.GRAY);
    @Shadow
    @Final
    private File file;

    protected PackScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At(value = "HEAD"), method = "init")
    private void addvvdButton(CallbackInfo info) {
        // Checks if it is the resource pack screen and not the data pack screen
        if (this.file == this.client.getResourcePackDir()) {
            this.addDrawableChild(new ButtonWidget(this.width / 2 - 75, this.height - 24, 150, 20,
                    new TranslatableText("vvd.resourcePack.button"), button -> {
                        this.client.setScreen(new VVDScreen(this, vvd_RESOURCE_PACK_SUBTITLE));
                    }));
        }
    }
}
