package com.qendolin.betterclouds.mixin.runtime;

//? if =1.21.6 || = 1.21.7 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.qendolin.betterclouds.duck.FogRendererDuck;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.client.render.fog.FogRenderer.FOG_UBO_SIZE;

@SuppressWarnings("UnusedMixin")
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin implements FogRendererDuck {

    @Unique
    private boolean captureApplyFogResult = false;
    @Unique
    private FogRendererDuck.FogApplyResult applyFogResult;
    @Unique
    private final DummyBuffer dummyBuffer = new DummyBuffer(FOG_UBO_SIZE * 8); // larger for safety


    @Shadow public abstract Vector4f applyFog(Camera camera, int viewDistance, boolean thick, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world);

    @Override
    public FogApplyResult betterclouds$applyFog(Camera camera, int viewDistance, boolean thick, RenderTickCounter tickCounter, float skyDarkness, ClientWorld world) {
        applyFogResult = null;
        captureApplyFogResult = true;
        try {
            applyFog(camera, viewDistance, thick, tickCounter, skyDarkness, world);
        } finally {
            captureApplyFogResult = false;
        }
        return applyFogResult;
    }

    @WrapOperation(
        method = "applyFog(Lnet/minecraft/client/render/Camera;IZLnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;mapBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;ZZ)Lcom/mojang/blaze3d/buffers/GpuBuffer$MappedView;"))
    private GpuBuffer.MappedView createDummyEncoder(CommandEncoder instance, GpuBuffer gpuBuffer, boolean read, boolean write, Operation<GpuBuffer.MappedView> original) {
        if(!captureApplyFogResult)
            return original.call(instance, gpuBuffer, read, write);

        dummyBuffer.reset();
        return dummyBuffer;
    }


    @Inject(
        method = "applyFog(Lnet/minecraft/client/render/Camera;IZLnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/fog/FogRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V", shift = At.Shift.AFTER)
    )
    private void captureFogData(CallbackInfoReturnable<Vector4f> cir, @Local(ordinal = 0) FogData fogData, @Share("fogData") LocalRef<FogData> fogDataRef) {
        if(captureApplyFogResult) {
            fogDataRef.set(fogData);
        }
    }

    @ModifyReturnValue(
        method = "applyFog(Lnet/minecraft/client/render/Camera;IZLnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
        at = @At("RETURN")
    )
    private Vector4f captureFogColor(Vector4f color, @Share("fogData") LocalRef<FogData> fogDataRef) {
        if(captureApplyFogResult) {
            applyFogResult = new FogRendererDuck.FogApplyResult(fogDataRef.get(), color);
        }

        return color;
    }
}
//?}

