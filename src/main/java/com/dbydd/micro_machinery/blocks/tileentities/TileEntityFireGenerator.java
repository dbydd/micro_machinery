package com.dbydd.micro_machinery.blocks.tileentities;

import com.dbydd.micro_machinery.EnumType.EnumMMFETileEntityStatus;
import com.dbydd.micro_machinery.blocks.machine.FireGenerator;
import com.dbydd.micro_machinery.init.ModRecipes;
import com.dbydd.micro_machinery.recipes.RecipeHelper;
import com.dbydd.micro_machinery.recipes.firegenerator.FireGeneratorRecipe;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class TileEntityFireGenerator extends MMFEMachineBase implements ITickable, IFluidHandler {

    private ItemStackHandler fuelHandler = new ItemStackHandler(1);
    private FluidTank tank = new FluidTank(4000);
    private int maxBurnTime = 0;
    private int currentBurnTime = 0;
    private int generateFEPerTick = 0;
    private int waterNeededPerTick = 0;
    private boolean isGenerating = false;

    public TileEntityFireGenerator(int maxEnergyCapacity) {
        super(maxEnergyCapacity, EnumMMFETileEntityStatus.OUTPUT, 0);

    }

    public boolean isGenerating() {
        return isGenerating;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("inventory", fuelHandler.serializeNBT());
        compound.setInteger("maxBurnTime", maxBurnTime);
        compound.setInteger("currentBurnTime", currentBurnTime);
        compound.setInteger("generateFEPerTick", generateFEPerTick);
        compound.setInteger("waterNeededPerTick", waterNeededPerTick);
        compound.setBoolean("isGenerating", isGenerating);
        return super.writeToNBT(tank.writeToNBT(compound));
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        fuelHandler.deserializeNBT(compound.getCompoundTag("inventory"));
        maxBurnTime = compound.getInteger("maxBurnTime");
        currentBurnTime = compound.getInteger("currentBurnTime");
        generateFEPerTick = compound.getInteger("generateDEPerTick");
        waterNeededPerTick = compound.getInteger("waterNeededPerTick");
        isGenerating = compound.getBoolean("isGenerating");
        tank.readFromNBT(compound);
        super.readFromNBT(compound);
    }

    public boolean reciveBehindBlockEnergy() {
        EnumFacing facing = world.getBlockState(pos).getValue(BlockHorizontal.FACING);
        BlockPos behindBlock = pos.offset(facing.getOpposite());
        return pushEnergy(behindBlock, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) fuelHandler;
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (T) this;
        if (capability == CapabilityEnergy.ENERGY) return (T) this;
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return true;
        if (capability == CapabilityEnergy.ENERGY) return true;
        return super.hasCapability(capability, facing);
    }


    @Override
    public void update() {
        if (!world.isRemote) {
            if (!isGenerating) {
                for (FireGeneratorRecipe recipe : ModRecipes.fireGenerateRecipes) {
                    if (RecipeHelper.isStackABiggerThanStackB(fuelHandler.getStackInSlot(0), recipe.getFuel())) {
                        maxBurnTime = recipe.getMaxBurnTime();
                        generateFEPerTick = recipe.getGenerateFEPerTick();
                        waterNeededPerTick = recipe.getWaterNeededPerTick();
                        isGenerating = true;
                        FireGenerator.setState(isGenerating, world, pos);
                        markDirty();
                        syncToTrackingClients();
                    }
                }
            } else {
                currentBurnTime++;
                if (tank.getFluidAmount() >= 0) {
                    tank.drain(waterNeededPerTick, true);
                    if (energyStored + generateFEPerTick >= maxEnergyCapacity) {
                        energyStored = maxEnergyCapacity;
                    } else energyStored += generateFEPerTick;
                }
                if (currentBurnTime >= maxBurnTime) {
                    isGenerating = false;
                    currentBurnTime = 0;
                    maxBurnTime = 0;
                    generateFEPerTick = 0;
                    waterNeededPerTick = 0;
                    FireGenerator.setState(isGenerating, world, pos);
                }
                markDirty();
                syncToTrackingClients();
            }

            if (energyStored > 0) {
                if (reciveBehindBlockEnergy()) {
                    markDirty();
                    syncToTrackingClients();
                }

            }
        }
    }


    @Override
    public IFluidTankProperties[] getTankProperties() {
        return tank.getTankProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource.getFluid().getBlock() == Blocks.WATER) return tank.fill(resource, doFill);
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return tank.drain(resource, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return tank.drain(maxDrain, doDrain);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound tag = pkt.getNbtCompound();
        readFromNBT(tag);
    }
}