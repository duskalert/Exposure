package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.export.ImageExporter;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.client.gui.screen.element.Pager;
import io.github.mortuusars.exposure.client.gui.component.SteppedZoom;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.exposure.client.input.Modifier;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.world.photograph.PhotographType;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class PhotographScreen extends Screen {
    // TODO: MC 26.1 - Screen/Rendering API redesigned. Method bodies stubbed.

    protected final Pager pager = new Pager()
            .setCycled(true)
            .setChangeSound(new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK))
            .onPageChanged(this::pageChanged);

    protected final SteppedZoom zoom = new SteppedZoom()
            .zoomInSteps(4)
            .zoomOutSteps(4)
            .zoomPerStep(1.4)
            .defaultZoom(1);

    protected final KeyBindings keyBindings = KeyBindings.of(
            Key.press(Minecrft.options().keyInventory).executes(this::onClose),
            Key.press(InputConstants.KEY_ADD).or(Key.press(InputConstants.KEY_EQUALS)).executes(zoom::zoomIn),
            Key.press(GLFW.GLFW_KEY_KP_SUBTRACT).or(Key.press(InputConstants.KEY_MINUS)).executes(zoom::zoomOut),
            Key.press(Modifier.CONTROL, InputConstants.KEY_I).executes(this::dropAsItem),
            Key.press(Modifier.CONTROL, InputConstants.KEY_C).executes(this::copyIdentifierToClipboard),
            Key.press(Modifier.CONTROL | Modifier.SHIFT, InputConstants.KEY_C).executes(this::copySavedFilePathToClipboard),
            Key.press(Modifier.CONTROL, InputConstants.KEY_S).executes(this::openSavedFile),
            Key.press(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::previousPage),
            Key.press(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::nextPage),
            Key.release(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::resetCooldown),
            Key.release(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::resetCooldown)
    );

    protected final PhotographProvider photographProvider;

    protected float x;
    protected float y;

    protected final Set<String> savedExposureIds = new HashSet<>();
    protected final Map<String, File> savedExposureFiles = new HashMap<>();

    protected ArrayList<ItemAndStack<PhotographItem>> photographs = new ArrayList<>();

    public PhotographScreen(PhotographProvider photographProvider) {
        super(Component.empty());
        this.photographProvider = photographProvider;
        setPhotographs(photographProvider.get());
        if (shouldQueryAllPhotographsImmediately()) {
            queryAllPhotographs(photographs);
        }
    }

    public PhotographScreen(List<ItemAndStack<PhotographItem>> photographs) {
        this(PhotographProvider.fixed(photographs));
    }

    protected void setPhotographs(List<ItemAndStack<PhotographItem>> photographs) {
        this.photographs.clear();
        this.photographs.addAll(photographs);
        this.pager.setPagesCount(photographs.size());
        this.pager.setPage(0);
    }

    // TODO: MC 26.1 - tick signature
    public void tick() {
        if (photographProvider.shouldRefresh()) {
            setPhotographs(photographProvider.get());
        }
    }

    // TODO: MC 26.1 - init signature
    protected void init() {
        // Stubbed
    }

    protected boolean shouldQueryAllPhotographsImmediately() {
        return true;
    }

    protected void queryAllPhotographs(List<ItemAndStack<PhotographItem>> photographs) {
        for (ItemAndStack<PhotographItem> photograph : photographs) {
            photograph.getItem().getFrame(photograph.getItemStack())
                    .identifier()
                    .ifId(id -> ExposureClient.exposureStore().getOrRequest(id));
        }
    }

    public ItemAndStack<PhotographItem> getCurrentPhotograph() {
        return photographs.getFirst();
    }

    protected void pageChanged(int oldPage, int newPage) {
        int distance = newPage - oldPage;
        Collections.rotate(photographs, -distance);
    }

    // TODO: MC 26.1 - render signature changed, RenderSystem/pushPose/Lightmap API changed
    public void render(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed - MC 26.1 rendering API redesigned
    }

    // TODO: MC 26.1 - renderBackground signature changed
    public void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderFrameInfoHint stubbed
    private void renderFrameInfoHint(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, ItemAndStack<PhotographItem> photograph) {
        // Stubbed
    }

    // TODO: MC 26.1 - keyPressed now takes KeyEvent
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    // TODO: MC 26.1 - keyReleased now takes KeyEvent
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    // TODO: MC 26.1 - mouseScrolled signature
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    // TODO: MC 26.1 - mouseDragged now takes MouseButtonEvent,double,double
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    // TODO: MC 26.1
    public boolean isPauseScreen() {
        return false;
    }

    protected boolean dropAsItem() {
        if (!Minecrft.player().isCreative()) {
            return false;
        }
        ItemStack droppedStack = getCurrentPhotograph().getItemStack().copy();
        Frame frame = droppedStack.getOrDefault(Exposure.DataComponents.PHOTOGRAPH_FRAME, Frame.EMPTY);
        ExposureType type = frame.type();
        droppedStack.set(Exposure.DataComponents.PHOTOGRAPH_TYPE, type);
        Minecrft.gameMode().handleCreativeModeItemDrop(droppedStack);
        Minecrft.player().sendSystemMessage(Component.translatable("gui.exposure.photograph_screen.item_dropped_message",
                droppedStack.getDisplayName()));
        return true;
    }

    protected boolean copyIdentifierToClipboard() {
        Frame frame = getCurrentPhotograph().map(PhotographItem::getFrame);
        if (!Minecrft.player().isCreative() || frame.equals(Frame.EMPTY)) {
            return false;
        }
        String text = frame.identifier().map(id -> id, Identifier::toString);
        Minecrft.get().keyboardHandler.setClipboard(text);
        Minecrft.player().sendSystemMessage(
                Component.translatable("gui.exposure.photograph_screen.copied_message", text));
        return true;
    }

    protected boolean copySavedFilePathToClipboard() {
        return getCurrentPhotograph()
                .map(PhotographItem::getFrame)
                .identifier()
                .mapId(id -> {
                    if (savedExposureFiles.get(id) instanceof File file) {
                        Minecrft.get().keyboardHandler.setClipboard(file.getAbsolutePath());
                        Minecrft.player().sendSystemMessage(
                                Component.translatable("gui.exposure.photograph_screen.copied_message", file.getAbsolutePath()));
                        return true;
                    }
                    return false;
                }).orElse(false);
    }

    protected boolean openSavedFile() {
        return getCurrentPhotograph()
                .map(PhotographItem::getFrame)
                .identifier()
                .mapId(id -> {
                    if (savedExposureFiles.get(id) instanceof File file) {
                        Util.getPlatform().openFile(file);
                        return true;
                    }
                    return false;
                }).orElse(false);
    }

    protected void trySaveToFile(ItemAndStack<PhotographItem> photograph) {
        // TODO: MC 26.1 - stubbed
    }

    protected @NotNull String getFilename(String id, PhotographType photographType) {
        String suffix = photographType.getFileSuffix();
        if (!StringUtil.isNullOrEmpty(suffix)) {
            return id + "_" + suffix;
        }
        return id;
    }

    public interface PhotographProvider {
        boolean shouldRefresh();
        List<ItemAndStack<PhotographItem>> get();

        static PhotographProvider fixed(List<ItemAndStack<PhotographItem>> photographs) {
            Preconditions.checkState(!photographs.isEmpty(), "No photographs to display.");
            return new PhotographProvider() {
                private final List<ItemAndStack<PhotographItem>> list = photographs;

                @Override
                public boolean shouldRefresh() {
                    return false;
                }

                @Override
                public List<ItemAndStack<PhotographItem>> get() {
                    return list;
                }
            };
        }

        static PhotographProvider fromPhotographItem(int slot) {
            return new ItemProvider(() -> Minecrft.player().getInventory().getItem(slot));
        }

        class ItemProvider implements PhotographProvider {
            protected Supplier<ItemStack> itemSupplier;
            protected List<ItemAndStack<PhotographItem>> photographs;

            public ItemProvider(Supplier<ItemStack> itemSupplier) {
                this.itemSupplier = itemSupplier;
                ItemStack stack = itemSupplier.get();
                Preconditions.checkState(stack.getItem() instanceof PhotographItem || stack.getItem() instanceof StackedPhotographsItem,
                        "itemSupplier should supply valid Photograph or Stacked Photographs item stack at the moment of creation.");
                this.photographs = fromItemStack(stack);
            }

            protected List<ItemAndStack<PhotographItem>> fromItemStack(ItemStack stack) {
                if (stack.getItem() instanceof PhotographItem) {
                    return List.of(new ItemAndStack<>(stack));
                }
                if (stack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem) {
                    return stackedPhotographsItem.getPhotographs(stack).photographsItemAndStacks();
                }
                return Collections.emptyList();
            }

            @Override
            public boolean shouldRefresh() {
                ItemStack item = itemSupplier.get();
                List<ItemAndStack<PhotographItem>> newPhotographs = fromItemStack(item);
                if (newPhotographs.isEmpty()) {
                    return false;
                }
                boolean shouldRefresh = !get().equals(newPhotographs);
                if (shouldRefresh) {
                    photographs = newPhotographs;
                }
                return shouldRefresh;
            }

            @Override
            public List<ItemAndStack<PhotographItem>> get() {
                return photographs;
            }
        }
    }
}
