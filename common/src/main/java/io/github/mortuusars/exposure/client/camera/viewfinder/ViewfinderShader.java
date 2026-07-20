package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Filters;
import io.github.mortuusars.exposure.world.item.camcom.Attachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ViewfinderShader implements AutoCloseable {
    private final Minecraft minecraft;
    private final Camera camera;
    private final Viewfinder viewfinder;

    @Nullable
    private PostChain shader;
    private boolean active;

    public ViewfinderShader(Camera camera, Viewfinder viewfinder) {
        this.minecraft = Minecrft.get();
        this.camera = camera;
        this.viewfinder = viewfinder;
        this.update();
    }

    public void apply(Identifier shaderLocation) {
    }

    public void resize(int width, int height) {
    }

    /**
     * Processes current viewfinder shader (if it is present and active).
     */
    public void process() {
    }

    public void update() {
        setActive(viewfinder.isLookingThrough());
        if (active) {
            ItemStack filterStack = Attachment.FILTER.get(camera.getItemStack()).getForReading();
            Filters.of(Minecrft.registryAccess(), filterStack).map(Filter::shader).ifPresentOrElse(this::apply, this::remove);
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void remove() {
        if (shader != null) {
            shader.close();
        }

        shader = null;
    }

    @Override
    public void close() {
        remove();
    }
}
