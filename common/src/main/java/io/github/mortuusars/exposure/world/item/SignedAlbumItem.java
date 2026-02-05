package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.world.item.component.album.SignedAlbumContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SignedAlbumItem extends Item {
    public SignedAlbumItem(Properties properties) {
        super(properties);
    }

    public SignedAlbumContent getContent(ItemStack stack) {
        return Exposure.DataComponents.getSignedAlbumContent(stack,SignedAlbumContent.EMPTY);
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        @Nullable SignedAlbumContent content = Exposure.DataComponents.getSignedAlbumContent(stack);
        if (content != null) {
            String title = content.title();
            if (!title.isBlank()) {
                return Component.literal(title);
            }
        }

        return super.getName(stack);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide()) {
            ClientGUI.openAlbumViewScreen(player.getItemInHand(usedHand));
        }
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockPos blockPos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.is(Blocks.LECTERN))
            return LecternBlock.tryPlaceBook(context.getPlayer(), level, blockPos, blockState,
                  context.getItemInHand()) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        @Nullable SignedAlbumContent content = Exposure.DataComponents.getSignedAlbumContent(stack);

        if (content != null) {
            String author = content.author();
            if (!author.isBlank()) {
                tooltipComponents.add(Component.translatable("gui.exposure.album.by_author", author).withStyle(ChatFormatting.GRAY));
            }

            if (Config.Client.ALBUM_PHOTOS_COUNT_TOOLTIP.get()) {
                int photographsCount = (int)content.pages().stream().filter(page -> !page.photograph().isEmpty()).count();
                if (photographsCount > 0)
                    tooltipComponents.add(Component.translatable("item.exposure.album.tooltip.photos_count", photographsCount));
            }
        }

    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return Config.Common.SIGNED_ALBUM_GLINT.get();
    }
}
