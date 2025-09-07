package net.trinketina.contexttools;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.Objects;

public interface PickTool {
    public default boolean tryPickTool() {
        boolean heldPickaxe = false;
        boolean heldShovel = false;
        boolean heldAxe = false;
        boolean heldHoe = false;


        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player.isCreative()) {
            //only do tool swapping in survival
            return false;
        }

        ItemStack heldItem = client.player.getMainHandStack();
        if (heldItem == null) {
            return false;
        }

        //check if holding a tool, if so of what type
        if (heldItem.isIn(ItemTags.PICKAXES)) {
            heldPickaxe = true;
        }
        if (heldItem.isIn(ItemTags.SHOVELS)) {
            heldShovel = true;
        }
        if (heldItem.isIn(ItemTags.AXES)) {
            heldAxe = true;
        }
        if (heldItem.isIn(ItemTags.HOES)) {
            heldHoe = true;
        }

        if (!heldPickaxe && !heldShovel && !heldAxe && !heldHoe) {
            //if not holding a tool, then skip PickTool
            return false;
        }

        if (client.crosshairTarget != null && client.crosshairTarget.getType() != net.minecraft.util.hit.HitResult.Type.MISS && client.crosshairTarget.getType() != HitResult.Type.ENTITY) {
            boolean pickaxeMineable = false;
            boolean shovelMineable = false;
            boolean axeMineable = false;
            boolean hoeMineable = false;

            HitResult nullableHitResult = client.crosshairTarget;
            Objects.requireNonNull(nullableHitResult);
            HitResult hitResult = nullableHitResult;

            BlockHitResult blockHitResult = (BlockHitResult)hitResult;
            Objects.requireNonNull(blockHitResult);

            BlockState blockState = client.player.getWorld().getBlockState(blockHitResult.getBlockPos());
            Objects.requireNonNull(blockState);


            if (blockState.isIn(BlockTags.PICKAXE_MINEABLE)) {
                pickaxeMineable = true;
            }
            if (blockState.isIn(BlockTags.SHOVEL_MINEABLE)) {
                shovelMineable = true;
            }
            if (blockState.isIn(BlockTags.AXE_MINEABLE)) {
                axeMineable = true;
            }
            if (blockState.isIn(BlockTags.HOE_MINEABLE)) {
                hoeMineable = true;
            }

            if ((heldPickaxe && pickaxeMineable) || (heldShovel && shovelMineable) || (heldAxe && axeMineable) || (heldHoe && hoeMineable)) {
                //already holding proper tool
                return true;
            }

            PlayerInventory inventory = client.player.getInventory();

            ItemStack swapItem = null;
            for (ItemStack item : inventory.main) {
                if (pickaxeMineable && item.isIn(ItemTags.PICKAXES)) {
                    swapItem = item;
                    break;
                }
                if (shovelMineable && item.isIn(ItemTags.SHOVELS)) {
                    swapItem = item;
                    break;
                }
                if (axeMineable && item.isIn(ItemTags.AXES)) {
                    swapItem = item;
                    break;
                }
                if (hoeMineable && item.isIn(ItemTags.HOES)) {
                    swapItem = item;
                    break;
                }
            }
            if (swapItem == null) {
                //no valid tool to swap to
                return false;
            }

            client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId, inventory.getSlotWithStack(swapItem), inventory.selectedSlot, SlotActionType.SWAP, client.player);

            return true;

        }

        return false;
    }
}
