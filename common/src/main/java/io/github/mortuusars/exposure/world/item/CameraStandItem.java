package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.render.CameraStandEntityRenderer;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class CameraStandItem extends Item {
    public CameraStandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction direction = context.getClickedFace();
        if (direction == Direction.DOWN) return InteractionResult.FAIL;

        Level level = context.getLevel();
        BlockPlaceContext blockPlaceContext = new BlockPlaceContext(context);
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        ItemStack itemStack = context.getItemInHand();
        Vec3 pos = Vec3.atBottomCenterOf(blockPos);
        AABB aabb = Exposure.EntityTypes.CAMERA_STAND.get().getDimensions().makeBoundingBox(pos.x(), pos.y(), pos.z());
        if (!level.noCollision(null, aabb) || !level.getEntities(null, aabb).isEmpty()) return InteractionResult.FAIL;

        if (level instanceof ServerLevel serverLevel) {
            Consumer<CameraStandEntity> consumer = EntityType.createDefaultStackConfig(serverLevel, itemStack, context.getPlayer());
            CameraStandEntity cameraStand = Exposure.EntityTypes.CAMERA_STAND.get().create(serverLevel, consumer, blockPos, MobSpawnType.SPAWN_EGG, true, true);
            if (cameraStand == null) {
                return InteractionResult.FAIL;
            }

            cameraStand.moveTo(cameraStand.getX(), cameraStand.getY(), cameraStand.getZ(), 0.0F, 0.0F);
            serverLevel.addFreshEntityWithPassengers(cameraStand);
            level.playSound(null, cameraStand.getX(), cameraStand.getY(), cameraStand.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
            cameraStand.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
        }

        itemStack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
