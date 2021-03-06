package mfrf.dbydd.micro_machinery.network.tile_sync_to_server;

import mfrf.dbydd.micro_machinery.blocks.machines.MMTileBase;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class TileClientToServerSyncPackage {
    private CompoundNBT nbt;
    private BlockPos pos;

    public TileClientToServerSyncPackage(PacketBuffer buffer) {
        nbt = buffer.readCompoundTag();
        pos = buffer.readBlockPos();
    }

    public TileClientToServerSyncPackage(CompoundNBT nbt, BlockPos pos) {
        this.nbt = nbt;
        this.pos = pos;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeCompoundTag(nbt);
        buf.writeBlockPos(pos);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if(context.getDirection().getReceptionSide()== LogicalSide.SERVER)
            context.enqueueWork(() -> {
                ServerWorld world = Objects.requireNonNull(context.getSender()).getServerWorld();
                if(world.isAreaLoaded(pos, 1))
                {
                    TileEntity tile = world.getTileEntity(pos);
                    if(tile instanceof MMTileBase){
                        ((MMTileBase)tile).handleNetWorkSyncFromClient(nbt);
                    }
                }
            });
    }
}
