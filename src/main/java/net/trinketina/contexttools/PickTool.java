package net.trinketina.contexttools;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Objects;

public interface PickTool {
    public default boolean tryPickTool() {
        boolean heldPickaxe = false;
        boolean heldShovel = false;
        boolean heldAxe = false;
        boolean heldHoe = false;


        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player.isInCreativeMode()) {
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
            for (ItemStack item : inventory) {
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
            if (swapItem == null || swapItem.isEmpty()) {
                //no valid tool to swap to, run default behavior
                return false;
            }
            int slotID = inventory.getSlotWithStack(swapItem);
            ToolPickerModClient.LOGGER.info("slot: " + slotID);

            if (PlayerInventory.isValidHotbarIndex(slotID)) {
                //if the correct tool is in the hotbar, then just move the selected slot to that tool
                ToolPickerModClient.LOGGER.info("hotbar");
                inventory.setSelectedSlot(slotID);
                return true;
            }
            if (swapItem.equals(inventory.getStack(PlayerInventory.OFF_HAND_SLOT))) {
                ToolPickerModClient.LOGGER.info("offhand");
                //if the correct tool is in the offhand, request to swap with offhand
                client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                return true;
            }

            if (slotID < 0) {
                ToolPickerModClient.LOGGER.warn("could not find slot with " + swapItem + "\nif a tool swap was supposed to occur, please report this as a bug!");
                return false;
            }

            ToolPickerModClient.LOGGER.info("inventory");
            client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId, slotID, inventory.getSelectedSlot(), SlotActionType.SWAP, client.player);

            return true;

        }

        return false;
    }
}
