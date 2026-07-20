package io.github.mortuusars.exposure.client.capture.action;

import net.minecraft.resources.Identifier;

// TODO: MC 26.1 - GameRenderer shader API removed. Needs rewrite.
@Deprecated
public class SetPostEffectAction implements CaptureAction {
    public SetPostEffectAction(Identifier effect) {}

    @Override public void beforeCapture() {}
    @Override public void afterCapture() {}
}
