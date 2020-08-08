package com.dbydd.micro_machinery.blocks.mathines.hand_generator;

import com.dbydd.micro_machinery.blocks.mathines.MMTileBase;
import com.dbydd.micro_machinery.registery_lists.Registered_Tileentitie_Types;
import com.dbydd.micro_machinery.utils.FEContainer;
import com.dbydd.micro_machinery.utils.IntegerContainer;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileHandGenerator extends MMTileBase implements ITickable {
    private FEContainer container = new FEContainer(0, 16) {
        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }

        @Override
        public int self_add() {
            return add(16, false);
        }
    };
    private IntegerContainer progress = new IntegerContainer(0, 21);

    public TileHandGenerator() {
        super(Registered_Tileentitie_Types.TILE_HAND_GENERATOR.get());
    }

    public IntegerContainer getProgress() {
        return progress;
    }

    @Override
    public void read(CompoundNBT compound) {
        container.deserializeNBT(compound.getCompound("energy_container"));
        progress.deserializeNBT(compound.getCompound("progress"));
        super.read(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put("energy_container", container.serializeNBT());
        compound.put("progress", container.serializeNBT());
        return super.write(compound);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY && isBackDirection(side)) {
            return LazyOptional.of(() -> container).cast();
        }
        return super.getCapability(cap, side);
    }

    public void OnActivated() {
        if (!world.isRemote() && progress.atMinValue()) {
            container.self_add();
            Direction backDirection = getBackDirection();
            if (backDirection != null) {
                container = pushEnergyToDirection(backDirection, container);
                progress.self_add();
            }
            markDirty2();
        }
    }

    @Override
    public void tick() {
        if (!world.isRemote()) {
            if (!progress.atMinValue()) {
                progress.self_add();
            }

            if (progress.atMaxValue()) {
                progress = new IntegerContainer(0, 21);
            }
        }
    }
}
