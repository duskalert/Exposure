package io.github.mortuusars.exposure.client.gui.screen.album;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.screen.element.Pager;
import io.github.mortuusars.exposure.client.gui.screen.element.TextBlock;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.HorizontalAlignment;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.util.Side;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.component.album.AlbumContent;
import io.github.mortuusars.exposure.world.item.component.album.AlbumPage;
import io.github.mortuusars.exposure.world.item.component.album.SignedAlbumContent;
import io.github.mortuusars.exposure.world.item.component.album.SignedAlbumPage;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class AlbumViewScreen extends Screen {
    // TODO: MC 26.1 - Screen/Rendering API redesigned. Method bodies stubbed.

    protected final Pager pager = new Pager()
            .setChangeSound(new SoundEffect(() -> SoundEvents.BOOK_PAGE_TURN))
            .onPageChanged(this::onSpreadChanged);

    protected final KeyBindings keyBindings = KeyBindings.of(
            Key.press(Minecrft.options().keyInventory).executes(this::onClose),
            Key.press(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::previousPage),
            Key.press(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::nextPage),
            Key.release(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::resetCooldown),
            Key.release(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::resetCooldown)
    );

    protected final List<Page> pages = new ArrayList<>();

    protected AlbumAccess albumAccess;
    protected int imageWidth;
    protected int imageHeight;
    protected int leftPos;
    protected int topPos;

    public AlbumViewScreen(AlbumAccess albumAccess) {
        super(Component.empty());
        this.albumAccess = albumAccess;
    }

    // TODO: MC 26.1
    public boolean isPauseScreen() {
        return false;
    }

    // TODO: MC 26.1
    protected void init() {
        // Stubbed
    }

    protected Page createPage(Side side, int xOffset) {
        return null;
    }

    public List<PageContent> getPages() {
        return albumAccess.pages();
    }

    public Optional<PageContent> getPage(int pageIndex) {
        if (pageIndex <= getPages().size() - 1)
            return Optional.ofNullable(getPages().get(pageIndex));
        return Optional.empty();
    }

    public Optional<PageContent> getPage(Side side) {
        return getPage(getCurrentSpreadIndex() * 2 + side.getIndex());
    }

    public int getCurrentSpreadIndex() {
        return pager.getPage();
    }

    protected void onSpreadChanged(int oldSpread, int newSpread) {
        // Stubbed
    }

    public void setAlbumAccess(AlbumAccess albumAccess) {
        this.albumAccess = albumAccess;
        pager.setPagesCount(albumAccess.getPageCount() / 2);
    }

    // TODO: MC 26.1 - render signature changed
    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderTooltip stubbed
    protected void renderTooltip(GuiGraphicsExtractor guiGraphics, int x, int y) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderBackground signature changed
    public void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - drawPageNumbers stubbed
    protected void drawPageNumbers(GuiGraphicsExtractor guiGraphics, int currentSpreadIndex, int mouseX, int mouseY) {
        // Stubbed
    }

    // TODO: MC 26.1 - mouseClicked now takes MouseButtonEvent
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    // TODO: MC 26.1 - handleComponentClicked signature changed
    public boolean handleComponentClicked(@Nullable Style style) {
        return false;
    }

    protected void forcePage(int pageIndex) {
        pager.changePage(pageIndex / 2);
    }

    // TODO: MC 26.1 - keyPressed now takes KeyEvent
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    // TODO: MC 26.1 - keyReleased now takes KeyEvent
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    protected void inspectPhotograph(ItemStack photograph) {
        if (!(photograph.getItem() instanceof PhotographItem)) {
            return;
        }
        Minecrft.get().setScreen(new ChildPhotographScreen(this, List.of(new ItemAndStack<>(photograph))));
        Minecrft.get().getSoundManager()
                .play(SimpleSoundInstance.forUI(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(),
                        Minecrft.level().getRandom().nextFloat() * 0.2f + 1.3f, 0.75f));
    }

    protected void forEachPage(Consumer<Page> pageAction) {
        for (Page page : pages) {
            pageAction.accept(page);
        }
    }

    protected record Page(Side side, PhotographSlotWidget photographWidget, TextBlock noteWidget) { }

    public record AlbumAccess(List<PageContent> pages) {
        public static final AlbumAccess EMPTY = new AlbumAccess(Collections.emptyList());

        public int getPageCount() {
            return this.pages.size();
        }

        public static AlbumAccess fromItem(ItemStack stack) {
            if (stack.get(Exposure.DataComponents.ALBUM_CONTENT) instanceof AlbumContent content) {
                return new AlbumAccess(content.pages().stream().map(PageContent::new).toList());
            }
            if (stack.get(Exposure.DataComponents.SIGNED_ALBUM_CONTENT) instanceof SignedAlbumContent content) {
                return new AlbumAccess(content.pages().stream().map(PageContent::new).toList());
            }
            return EMPTY;
        }
    }

    public record PageContent(ItemStack photograph, Component note) {
        public static final PageContent EMPTY = new PageContent(ItemStack.EMPTY, Component.empty());

        public PageContent(AlbumPage page) {
            this(page.photograph(), Component.literal(page.note()));
        }

        public PageContent(SignedAlbumPage page) {
            this(page.photograph(), page.note());
        }
    }
}
