package engineers.workshop.client.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/*
    This is a client and therefore extends Container, however, to clean it all up all the Container code is included
    in this class and it's therefore not using any code in the Container class. However, to make this compatible as a
    client for other classes (interfaces and the current open client a player has for instance) it must still
    extend Container.
 */

@SuppressWarnings("unused")
public abstract class ContainerBase extends Container {

    @SideOnly(Side.CLIENT)
    private short transactionID;

	private int dragMouseButton = -1;
    private int dragMode;
    private final Set<Slot> draggedSlots = new HashSet<>();

    private Set<EntityPlayer> invalidPlayers = new HashSet<>();

    private List<ItemStack> getItems() {
        return inventoryItemStacks;
    }

    private List<Slot> getSlots() {
        return inventorySlots;
    }

    @Override
    protected Slot addSlotToContainer(Slot slot) {
        slot.slotNumber = this.inventorySlots.size();
        getSlots().add(slot);
        getItems().add(ItemStack.EMPTY);
        return slot;
    }

    @Override
    public NonNullList<ItemStack> getInventory() {

	    NonNullList<ItemStack> result = NonNullList.withSize(getSlots().size(), ItemStack.EMPTY);
	    getSlots().forEach(slot -> result.add(slot.getStack()));
        return result;
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int slotId) {
        return false;
    }

    @Override
    public Slot getSlot(int slotId) {
        return getSlots().get(slotId);
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer player, int slotId) {
        return ItemStack.EMPTY;
    }

    //TODO: Ewy, fix this!
    //TODO: JORDAN FIX UR COMMENTING OUT SHIT. USE /* STUFF BETWEEN HERE */ U FUCKTARD!
    
    private static final int MOUSE_LEFT_CLICK = 0;
    private static final int MOUSE_RIGHT_CLICK = 1;

    private static final int FAKE_SLOT_ID = -999;

    private static final int CLICK_MODE_NORMAL = 0;
    private static final int CLICK_MODE_SHIFT = 1;
    private static final int CLICK_MODE_KEY = 2;
    private static final int CLICK_MODE_PICK_ITEM = 3;
    private static final int CLICK_MODE_OUTSIDE = 4;
    private static final int CLICK_DRAG_RELEASE = 5;
    private static final int CLICK_MODE_DOUBLE_CLICK = 6;

    private static final int CLICK_DRAG_MODE_PRE = 0;
    private static final int CLICK_DRAG_MODE_SLOT = 1;
    private static final int CLICK_DRAG_MODE_POST = 2;

    /*@Override
    public ItemStack slotClick(int slotId, int mouseButton, ClickType clickMode, EntityPlayer player) {
        if (slotId < 0 && slotId != FAKE_SLOT_ID) {
            return null;
        }

        ItemStack result = null;
        InventoryPlayer inventoryPlayer = player.inventory;

        if (clickMode == ClickType.QUICK_CRAFT) {
            int oldDragMode = dragMode;
            dragMode = mouseButton & 3;

            if ((oldDragMode != CLICK_DRAG_MODE_SLOT || dragMode != CLICK_DRAG_MODE_POST) && oldDragMode != dragMode) {
                resetDragging();
            }else if (inventoryPlayer.getItemStack() == null) {
                resetDragging();

            }else if (dragMode == CLICK_DRAG_MODE_PRE) {
                dragMouseButton = (mouseButton >> 2) & 3;

                if (dragMouseButton == MOUSE_LEFT_CLICK || dragMouseButton == MOUSE_RIGHT_CLICK) {
                    dragMode = CLICK_DRAG_MODE_SLOT;
                    draggedSlots.clear();

                }else {
                    resetDragging();
                }

            }else if (dragMode == CLICK_DRAG_MODE_SLOT) {
                Slot slot = getSlot(slotId);

                if (slot != null && canItemBePickedUp(slot, inventoryPlayer.getItemStack(), true) && slot.isItemValid(inventoryPlayer.getItemStack()) && inventoryPlayer.getItemStack().stackSize > draggedSlots.size() && canDragIntoSlot(slot)) {
                    draggedSlots.add(slot);
                }

            }else if (dragMode == CLICK_DRAG_MODE_POST) {

                if (!draggedSlots.isEmpty()) {
                    ItemStack playerItem = inventoryPlayer.getItemStack().copy();
                    int remainingItems = inventoryPlayer.getItemStack().stackSize;

                    for (Slot slot : draggedSlots) {
                        if (slot != null && canItemBePickedUp(slot, inventoryPlayer.getItemStack(), true) && slot.isItemValid(inventoryPlayer.getItemStack()) && inventoryPlayer.getItemStack().stackSize >= draggedSlots.size() && canDragIntoSlot(slot)) {
                            int currentCount = slot.getHasStack() ? slot.getStack().stackSize : 0;

                            int totalSize = currentCount;
                            if (dragMouseButton == MOUSE_LEFT_CLICK) {
                                totalSize += MathHelper.floor_float((float)playerItem.stackSize / (float)draggedSlots.size());
                            }else {
                                totalSize += 1;
                            }

                            int maxSize = Math.min(playerItem.getMaxStackSize(), getSlotStackLimit(slot, playerItem));
                            if (totalSize > maxSize) {
                                totalSize = maxSize;
                            }

                            if (totalSize > 0) {
                                remainingItems -= totalSize - currentCount;

                                ItemStack newItem = playerItem.copy();
                                newItem.stackSize = totalSize;
                                slot.putStack(newItem);
                            }
                        }
                    }

                    playerItem.stackSize = remainingItems;

                    if (playerItem.stackSize <= 0) {
                        playerItem = null;
                    }

                    inventoryPlayer.setItemStack(playerItem);
                }

                resetDragging();

            }else{
                resetDragging();
            }
        }else if (dragMode != CLICK_DRAG_MODE_PRE) {
            resetDragging();

        }else if ((clickMode == ClickType.PICKUP || clickMode == ClickType.QUICK_MOVE) && (mouseButton == MOUSE_LEFT_CLICK || mouseButton == MOUSE_RIGHT_CLICK)) {
            if (slotId == FAKE_SLOT_ID) {
                ItemStack dropItem = inventoryPlayer.getItemStack();
                if (dropItem != null) {
                    if (mouseButton == MOUSE_LEFT_CLICK) {
                        player.dropItem(dropItem, true);
                        inventoryPlayer.setItemStack(null);
                    }else{
                        player.dropItem(dropItem.splitStack(1), true);

                        if (dropItem.stackSize == 0) {
                            inventoryPlayer.setItemStack(null);
                        }
                    }
                }

            }else if (clickMode == ClickType.QUICK_MOVE) {
                Slot slot = getSlot(slotId);

                if (slot != null && slot.canTakeStack(player)) {
                    ItemStack transferResult = transferStackInSlot(player, slotId);

                    if (transferResult != null) {
                        Item item = transferResult.getItem();
                        result = transferResult.copy();

                        if (slot.getStack() != null && slot.getStack().getItem() == item) {
                            retrySlotClick(slotId, mouseButton, true, player);
                        }
                    }
                }

            }else {
                Slot slot = getSlot(slotId);

                if (slot != null) {
                    ItemStack slotItem = slot.getStack();
                    ItemStack playerItem = inventoryPlayer.getItemStack();

                    if (slotItem != null) {
                        result = slotItem.copy();
                    }

                    if (playerItem == null || playerItem.stackSize > 0) {
                        if (slotItem == null) {
                            if (playerItem != null && slot.isItemValid(playerItem)) {
                                int moveSize = mouseButton == MOUSE_LEFT_CLICK ? playerItem.stackSize : 1;

                                int maxSize = getSlotStackLimit(slot, playerItem);
                                if (moveSize > maxSize) {
                                    moveSize = maxSize;
                                }

                                if (moveSize > 0) {
                                    slot.putStack(playerItem.splitStack(moveSize));
                                }
                            }

                        }else if (slot.canTakeStack(player)) {
                            if (playerItem == null)  {
                                int moveSize = mouseButton == MOUSE_LEFT_CLICK ? slotItem.stackSize : (slotItem.stackSize + 1) / 2;
                                inventoryPlayer.setItemStack(playerItem = slot.decrStackSize(moveSize));

                                if (slotItem.stackSize == 0) {
                                    slot.putStack(null);
                                }

                                slot.onPickupFromSlot(player, playerItem);

                            }else if (slot.isItemValid(playerItem) && getSlotStackLimit(slot, playerItem) > slotItem.stackSize) {
                                if (slotItem.getItem() == playerItem.getItem() && slotItem.getItemDamage() == playerItem.getItemDamage() && ItemStack.areItemStackTagsEqual(slotItem, playerItem)) {
                                    int moveSize = mouseButton == MOUSE_LEFT_CLICK ? playerItem.stackSize : 1;

                                    int maxMoveSize = Math.min(getSlotStackLimit(slot, playerItem), playerItem.getMaxStackSize()) - slotItem.stackSize;
                                    if (moveSize > maxMoveSize) {
                                        moveSize = maxMoveSize;
                                    }

                                    if (moveSize > 0) {
                                        playerItem.splitStack(moveSize);
                                        slotItem.stackSize += moveSize;
                                    }

                                }else if (playerItem.stackSize <= getSlotStackLimit(slot, playerItem)) {
                                    slot.putStack(playerItem);
                                    inventoryPlayer.setItemStack(slotItem);
                                }

                            }else if (slotItem.getItem() == playerItem.getItem() && playerItem.getMaxStackSize() > 1 && (!slotItem.getHasSubtypes() || slotItem.getItemDamage() == playerItem.getItemDamage()) && ItemStack.areItemStackTagsEqual(slotItem, playerItem))  {
                                int moveSize = slotItem.stackSize;

                                if (moveSize > 0 && moveSize + playerItem.stackSize <= playerItem.getMaxStackSize()) {
                                    playerItem.stackSize += moveSize;
                                    slotItem = slot.decrStackSize(moveSize);

                                    if (slotItem.stackSize == 0) {
                                        slot.putStack(null);
                                    }

                                    slot.onPickupFromSlot(player, playerItem);
                                }
                            }
                        }
                    }

                    if (playerItem != null && playerItem.stackSize == 0) {
                        inventoryPlayer.setItemStack(null);
                    }

                    slot.onSlotChanged();
                }
            }

        } else if(slotId != FAKE_SLOT_ID) {
            if (clickMode == ClickType.SWAP && mouseButton >= 0 && mouseButton < 9) {
                Slot slot = getSlot(slotId);

                if (slot.canTakeStack(player)) {
                    ItemStack hotbarItem = inventoryPlayer.getStackInSlot(mouseButton);
                    boolean hasHotbarItem = hotbarItem != null;
                    boolean hasPlayerSlot = slot.inventory == inventoryPlayer;
                    boolean canMoveToSlot = slot.isItemValid(hotbarItem);

                    if (slot.getHasStack()) {
                        boolean flag = !hasHotbarItem || hasPlayerSlot && canMoveToSlot;

                        if (flag || inventoryPlayer.getFirstEmptyStack() >= 0) {
                            ItemStack slotItem = slot.getStack();
                            inventoryPlayer.setInventorySlotContents(mouseButton, slotItem.copy());

                            if (flag) {
                                slot.decrStackSize(slotItem.stackSize);
                                slot.putStack(hotbarItem);
                                slot.onPickupFromSlot(player, slotItem);

                            }else {
                                inventoryPlayer.addItemStackToInventory(hotbarItem);
                                slot.decrStackSize(slotItem.stackSize);
                                slot.putStack(null);
                                slot.onPickupFromSlot(player, slotItem);
                            }

                        }
                    }else if(hasHotbarItem && canMoveToSlot){
                        inventoryPlayer.setInventorySlotContents(mouseButton, null);
                        slot.putStack(hotbarItem);
                    }
                }

            }else if (clickMode == ClickType.CLONE && player.capabilities.isCreativeMode && inventoryPlayer.getItemStack() == null){
                Slot slot = getSlot(slotId);

                if (slot != null && slot.getHasStack()) {
                    ItemStack cloneItem = slot.getStack().copy();
                    cloneItem.stackSize = cloneItem.getMaxStackSize();
                    inventoryPlayer.setItemStack(cloneItem);
                }

            }else if (clickMode == ClickType.THROW && inventoryPlayer.getItemStack() == null){
                Slot slot = getSlot(slotId);

                if (slot != null && slot.getHasStack() && slot.canTakeStack(player)) {
                    ItemStack dropItem = slot.decrStackSize(mouseButton == MOUSE_LEFT_CLICK ? 1 : slot.getStack().stackSize);
                    slot.onPickupFromSlot(player, dropItem);
                    player.dropItem(dropItem, true);
                }

            }else if (clickMode == ClickType.PICKUP_ALL) {
                Slot slot = getSlot(slotId);
                ItemStack playerItem = inventoryPlayer.getItemStack();

                if (playerItem != null && (slot == null || !slot.getHasStack() || !slot.canTakeStack(player)))  {
                    int start = mouseButton == MOUSE_LEFT_CLICK ? 0 : getSlots().size() - 1;
                    int direction = mouseButton == MOUSE_LEFT_CLICK ? 1 : -1;

                    for (int iteration = 0; iteration < 2; iteration++) {
                        for (int i = start; i >= 0 && i < this.inventorySlots.size() && playerItem.stackSize < playerItem.getMaxStackSize(); i += direction) {
                            Slot loopSlot = getSlot(i);

                            boolean canTakeItem = loopSlot.getHasStack() && canItemBePickedUp(loopSlot, playerItem, true) && loopSlot.canTakeStack(player) && canItemBePickedUpByDoubleClick(playerItem, loopSlot);

                            if (iteration == 0 && canTakeItem) {
                                canTakeItem = loopSlot.getStack().stackSize != loopSlot.getStack().getMaxStackSize();
                            }

                            if (canTakeItem) {
                                int moveSize = Math.min(playerItem.getMaxStackSize() - playerItem.stackSize, loopSlot.getStack().stackSize);
                                ItemStack moveItem = loopSlot.decrStackSize(moveSize);
                                playerItem.stackSize += moveSize;

                                if (moveItem.stackSize <= 0) {
                                    loopSlot.putStack(null);
                                }

                                loopSlot.onPickupFromSlot(player, moveItem);
                            }
                        }
                    }
                }

                detectAndSendChanges();
            }
        }

        return result;
    }*/

    public static boolean canItemBePickedUp(Slot slot, ItemStack playerItem, boolean partiallyMove) {
        if (slot != null && slot.getHasStack()) {
            ItemStack slotItem = slot.getStack();
            if (playerItem != null && playerItem.isItemEqual(slotItem) && ItemStack.areItemStackTagsEqual(slotItem, playerItem)) {
                int moveSize = partiallyMove ? 0 : playerItem.getCount();
                return slot.getStack().getCount() + moveSize <= playerItem.getMaxStackSize();
            }else{
                return false;
            }
        }else{
            return true;
        }
    }

    protected boolean canItemBePickedUpByDoubleClick(ItemStack itemStack, Slot slot) {
        return true;
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        detectAndSendChanges();
    }

    @Override
    public void putStackInSlot(int slotId, ItemStack item) {
        getSlot(slotId).putStack(item);
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void putStacksInSlots(ItemStack[] items) {
//        for (int i = 0; i < items.length; ++i) {
//            putStackInSlot(i, items[i]);
//        }
//    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {}


    @SideOnly(Side.CLIENT)
    public short getNextTransactionID(InventoryPlayer inventory) {
        transactionID++;
        return transactionID;
    }


    protected boolean isPlayerValid(EntityPlayer player) {
        return !invalidPlayers.contains(player);
    }


    protected void setValidState(EntityPlayer player, boolean valid) {
        if (valid) {
            invalidPlayers.remove(player);
        }else{
            invalidPlayers.add(player);
        }
    }


    @Override
    public abstract boolean canInteractWith(EntityPlayer player);

    @Override
    protected boolean mergeItemStack(ItemStack item, int start, int end, boolean invert) {
        boolean moved = false;
        int index = start;

        if (invert) {
            index = end - 1;
        }


        if (item.isStackable()) {
            while (item.getCount() > 0 && (!invert && index < end || invert && index >= start)) {
                Slot slot = getSlot(index);
                ItemStack slotItem = slot.getStack();

                if (!slotItem.isEmpty() && slotItem.getItem() == item.getItem() && (!item.getHasSubtypes() || item.getItemDamage() == slotItem.getItemDamage()) && ItemStack.areItemStackTagsEqual(item, slotItem)) {
                    int newSize = slotItem.getCount() + item.getCount();

                    if (newSize <= item.getMaxStackSize()) {
                        item.setCount(0);
                        slotItem.setCount(newSize);
                        slot.onSlotChanged();
                        moved = true;

                    }else if (slotItem.getCount() < item.getMaxStackSize()) {
                        item.shrink(item.getMaxStackSize() - slotItem.getCount());
                        slotItem.setCount(item.getMaxStackSize());
                        slot.onSlotChanged();
                        moved = true;
                    }
                }

                index += invert ? -1 : 1;
            }
        }

        if (item.getCount() > 0) {
            if (invert) {
                index = end - 1;
            }else {
                index = start;
            }

            while (!invert && index < end || invert && index >= start) {
                Slot slot = getSlot(index);
                ItemStack slotItem = slot.getStack();

                if (slotItem.isEmpty()) {
                    slot.putStack(item.copy());
                    slot.onSlotChanged();
                    item.setCount(0);
                    moved = true;
                    break;
                }

                index += invert ? -1 : 1;
            }
        }

        return moved;
    }



    protected void resetDragging() {
        dragMode = 0;
        draggedSlots.clear();
    }

    @Override
    public boolean canDragIntoSlot(Slot slot) {
        return true;
    }

      /*
       =============================================
                Extracted methods
       =============================================

       The following methods are methods whose functionality
       is a part of bigger methods in the vanilla Container.
       These still do the same things but are extracted into
       their own methods so they can be overridden in sub-
       classes.
     */

    protected int getSlotStackLimit(Slot slot, ItemStack itemStack) {
        return slot.getSlotStackLimit();
    }


    /*
       =============================================
                Original named methods
       =============================================

       The following methods are the original named methods
       of non-private methods. These simply return or call
       properly/better named methods. These methods are made
       final, to override them, override the methods they are
       calling.
     */

}