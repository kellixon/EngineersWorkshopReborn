package engineers.workshop.client.gui.page;

import engineers.workshop.client.gui.GuiBase;
import engineers.workshop.client.gui.GuiTable;
import engineers.workshop.client.gui.container.slot.SlotBase;
import engineers.workshop.common.table.TileTable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class Page {

    private String name;
    protected TileTable table;
    private int id;

    public Page(TileTable table, String name) {
        this.id = table.getPages().size();
        this.table = table;
        this.name = name;
    }

    public String getName() {
        return name.toUpperCase();
    }

    public abstract int createSlots(int id);
    public abstract String getDesc();

    protected void addSlot(SlotBase slot) {
        table.addSlot(slot);
    }

    @SideOnly(Side.CLIENT)
    public void draw(GuiBase gui, int mX, int mY) {
        gui.drawString(name.toUpperCase(), 8, 6, 0x1E1E1E);
    }

    @SideOnly(Side.CLIENT)
    public void onClick(GuiBase gui, int mX, int mY, int button) {}

    public int getId() {
        return id;
    }

    public void onUpdate() {}

    @SideOnly(Side.CLIENT)
    public void onRelease(GuiTable gui, int mX, int mY, int button) {}
}
