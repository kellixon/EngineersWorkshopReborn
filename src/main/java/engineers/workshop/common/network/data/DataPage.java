package engineers.workshop.common.network.data;

import engineers.workshop.common.table.TileTable;
import net.minecraft.nbt.NBTTagCompound;

public class DataPage extends DataBase {

	@Override
	public void save(TileTable table, NBTTagCompound dw, int id) {
		dw.setInteger("id", table.getSelectedPage().getId());
	}

	@Override
	public void load(TileTable table, NBTTagCompound dr, int id) {
		table.setSelectedPage(table.getPages().get(dr.getInteger("id")));
	}
}
