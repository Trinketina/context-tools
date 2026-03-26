package net.trinketina.contexttools.mixin;


import net.minecraft.client.Minecraft;
import net.trinketina.contexttools.PickTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class ClientItemPickMixin implements PickTool {

    @Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
    private void doItemPick(CallbackInfo info) {
        if (tryPickTool()) {
            info.cancel();
        }
    }
}
