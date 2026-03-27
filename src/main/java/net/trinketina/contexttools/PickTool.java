package net.trinketina.contexttools;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.trinketina.contexttools.config.ConfigData;

import java.util.Objects;

public interface PickTool {
    default boolean tryPickTool() {
        boolean heldPickaxe = false;
        boolean heldShovel = false;
        boolean heldAxe = false;
        boolean heldHoe = false;


        Minecraft client = Minecraft.getInstance();

        if (!client.player.isCrouching() && ConfigData.CONFIG.requireCrouching) {
            //require crouching if enabled
            return false;
        }
        if (!client.hasControlDown() && ConfigData.CONFIG.requireControlHeldDown) {
            //require holding control if enabled
            return false;
        }

        if (client.player.isCreative()) {
            //only do tool swapping in survival
            return false;
        }

        ItemStack heldItem = client.player.getMainHandItem();
        if (heldItem.isEmpty() && ConfigData.CONFIG.requireToolInHand) {
            //require tool in hand if enabled
            return false;
        }


        //check if holding a tool, if so of what type
        if ( heldItem.is(ItemTags.PICKAXES)) {
            heldPickaxe = true;
        }
        if (heldItem.is(ItemTags.SHOVELS)) {
            heldShovel = true;
        }
        if (heldItem.is(ItemTags.AXES)) {
            heldAxe = true;
        }
        if (heldItem.is(ItemTags.HOES)) {
            heldHoe = true;
        }

        if (!heldPickaxe && !heldShovel && !heldAxe && !heldHoe && ConfigData.CONFIG.requireToolInHand) {
            //require tool in hand if enabled
            return false;
        }

        if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.BLOCK && client.hitResult instanceof BlockHitResult blockHitResult ) {
            boolean pickaxeMineable = false;
            boolean shovelMineable = false;
            boolean axeMineable = false;
            boolean hoeMineable = false;

            Objects.requireNonNull(blockHitResult);

            //BlockState blockState = client.player.getEntityWorld().getBlockState(blockHitResult.getBlockPos());
            BlockState blockState = Objects.requireNonNull(client.level).getBlockState(blockHitResult.getBlockPos());
            Objects.requireNonNull(blockState);


            if (blockState.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
                pickaxeMineable = true;
            }
            if (blockState.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
                shovelMineable = true;
            }
            if (blockState.is(BlockTags.MINEABLE_WITH_AXE)) {
                axeMineable = true;
            }
            if (blockState.is(BlockTags.MINEABLE_WITH_HOE)) {
                hoeMineable = true;
            }

            if ((heldPickaxe && pickaxeMineable) || (heldShovel && shovelMineable) || (heldAxe && axeMineable) || (heldHoe && hoeMineable)) {
                //already holding proper tool
                return true;
            }

            Inventory inventory = client.player.getInventory();

            ItemStack swapItem = null;
            for (ItemStack item : inventory) {
                if (pickaxeMineable && item.is(ItemTags.PICKAXES)) {
                    swapItem = item;
                    break;
                }
                if (shovelMineable && item.is(ItemTags.SHOVELS)) {
                    swapItem = item;
                    break;
                }
                if (axeMineable && item.is(ItemTags.AXES)) {
                    swapItem = item;
                    break;
                }
                if (hoeMineable && item.is(ItemTags.HOES)) {
                    swapItem = item;
                    break;
                }
            }
            if (swapItem == null || swapItem.isEmpty()) {
                //no valid tool to swap to, run default behavior
                return false;
            }
            //int slotID = inventory.getSlotWithStack(swapItem);
            int slotID = inventory.findSlotMatchingItem(swapItem);

            if (Inventory.isHotbarSlot(slotID)) {
                //if the correct tool is in the hotbar, then just move the selected slot to that tool
                inventory.setSelectedSlot(slotID);
                return true;
            }
            if (swapItem.equals(inventory.getItem(Inventory.SLOT_OFFHAND))) {
                //if the correct tool is in the offhand, request to swap with offhand
                //client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                client.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                return true;
            }

            if (slotID < 0) {
                ToolPickerModClient.LOGGER.warn("could not find slot with " + swapItem + "\nif a tool swap was supposed to occur, please report this as a bug!");
                return false;
            }

            assert client.gameMode != null;


            client.gameMode.handleContainerInput(client.player.containerMenu.containerId, slotID, inventory.getSelectedSlot(), ContainerInput.SWAP, client.player);


            return true;

        }

        return false;
    }
}
