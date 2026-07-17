package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class SetPostEffectAction implements CaptureAction {
    @Nullable
    private Identifier currentEffect;
    private boolean effectActive;

    private final Identifier effect;

    public SetPostEffectAction(Identifier effect) {
        this.effect = effect;
    }

    @Override
    public void beforeCapture() {
        PostChain currentEffect = Minecrft.get().gameRenderer.currentEffect();
        if (currentEffect != null) {
            this.currentEffect = Identifier.parse(currentEffect.getName());
        }
        this.effectActive = Minecrft.get().gameRenderer.effectActive;

        Minecrft.get().gameRenderer.loadEffect(effect);
        Minecrft.get().gameRenderer.effectActive = true;
    }

    @Override
    public void afterCapture() {
        Minecrft.get().gameRenderer.effectActive = effectActive;
        if (currentEffect != null) {
            Minecrft.get().gameRenderer.loadEffect(currentEffect);
        } else {
            Minecrft.get().gameRenderer.shutdownEffect();
        }
    }
}
