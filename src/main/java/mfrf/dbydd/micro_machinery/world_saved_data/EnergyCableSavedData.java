package mfrf.dbydd.micro_machinery.world_saved_data;

import mfrf.dbydd.micro_machinery.utils.IntegerContainer;
import mfrf.dbydd.micro_machinery.utils.WorldFEContainer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EnergyCableSavedData extends WorldSavedData {
    public static final String NAME = "CableWorldSavedData";

    private Map<Integer, WorldFEContainer> integerWorldFEContainerMap = new HashMap<>();

    public EnergyCableSavedData() {
        super(NAME);
    }

    public static EnergyCableSavedData get(World worldIn) {
        if (!(worldIn instanceof ServerWorld)) {
            throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
        }

        ServerWorld world = (ServerWorld) worldIn;
        DimensionSavedDataManager storage = world.getSavedData();
        return storage.getOrCreate(EnergyCableSavedData::new, NAME);
    }

    @Override
    public void read(CompoundNBT nbt) {
        INBT inbt = nbt.get("list");
        if (inbt instanceof ListNBT) {
            ListNBT list = (ListNBT) inbt;
            for (INBT inbt1 : list) {
                CompoundNBT inbt11 = (CompoundNBT) inbt1;
                int sign = inbt11.getInt("sign");
                WorldFEContainer.deserializeNBT((CompoundNBT) inbt11.get(Integer.toString(sign)));
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT inbts = new ListNBT();
        integerWorldFEContainerMap.forEach((integer, container) -> {
            CompoundNBT compoundNBT = new CompoundNBT();
            compound.putInt("sign", integer);
            compound.put(integer.toString(), container.serializeNBT());
            inbts.add(compound);
        });
        compound.put("list", inbts);
        return compound;
    }

    public int reciveEnergy(int sign, int maxRecive, boolean simulate) {
        int i = integerWorldFEContainerMap.get(sign).receiveEnergy(maxRecive, simulate);
        markDirty();
        return i;
    }

    public int extractEnergy(int sign, int maxExtract, boolean simulate) {
        int i = integerWorldFEContainerMap.get(sign).extractEnergy(maxExtract, simulate);
        markDirty();
        return i;
    }

    public boolean hasContainer(int sign) {
        return !(integerWorldFEContainerMap.get(sign) == null);
    }

    public int createContainer(int maxFE, int currentFE, int minFE) {
        WorldFEContainer container = new WorldFEContainer(BigInteger.valueOf(maxFE), BigInteger.valueOf(currentFE), BigInteger.valueOf(minFE));
        Random random = new Random();
        while (true) {
            int i = random.nextInt();
            if (integerWorldFEContainerMap.get(i) == null) {
                integerWorldFEContainerMap.put(i, container);
                markDirty();
                return i;
            }
        }
    }

    public WorldFEContainer getContainer(int sign) {
        return integerWorldFEContainerMap.get(sign);
    }

    public BigInteger removeCablePart(int sign, IntegerContainer container) {
        WorldFEContainer FEcontainer = integerWorldFEContainerMap.get(sign);
        BigInteger subtractedMax = FEcontainer.getMax().subtract(BigInteger.valueOf(container.getMax()));
        if (subtractedMax.compareTo(BigInteger.ZERO) > 0) {
            BigInteger subtractCurrent = FEcontainer.getCurrent().subtract(BigInteger.valueOf(container.getCurrent()));

            FEcontainer.setMax(subtractedMax);
            FEcontainer.setCurrent(subtractCurrent);
            markDirty();
            return FEcontainer.getMax();
        } else {
            integerWorldFEContainerMap.remove(sign);
            markDirty();
            return BigInteger.ZERO;
        }
    }

    public void addCablePart(int sign, IntegerContainer container) {
        WorldFEContainer targetContainer = integerWorldFEContainerMap.get(sign);

        targetContainer.setMax(targetContainer.getMax().add(BigInteger.valueOf(container.getMax())));
        targetContainer.setCurrent(targetContainer.getCurrent().add(BigInteger.valueOf(container.getCurrent())));

    }

    public void mergeOneInToAnother(int signMergeInto, int signBeMerge) {
        WorldFEContainer container1 = integerWorldFEContainerMap.get(signMergeInto);
        WorldFEContainer container2 = integerWorldFEContainerMap.get(signBeMerge);

        container1.setMax(container1.getMax().add(container2.getMax()));
        container1.setCurrent(container1.getCurrent().add(container2.getCurrent()));

        integerWorldFEContainerMap.remove(signBeMerge);

        markDirty();

    }

    public void splitCable(int sign, Integer sign1Splited, BigInteger sign1MaxEnergy, Integer sign2Splited, BigInteger sign2MaxEnergy) {
        WorldFEContainer container = integerWorldFEContainerMap.get(sign);
        WorldFEContainer containerSplited1 = new WorldFEContainer(BigInteger.ZERO, sign1MaxEnergy, container.getCurrent().multiply(sign1MaxEnergy.divide(container.getMax())));
        WorldFEContainer containerSplited2 = new WorldFEContainer(BigInteger.ZERO, sign2MaxEnergy, container.getCurrent().multiply(sign2MaxEnergy.divide(container.getMax())));
        Random random = new Random();
        integerWorldFEContainerMap.remove(sign);

        while (true) {
            int i = random.nextInt();
            if (integerWorldFEContainerMap.get(i) == null) {
                integerWorldFEContainerMap.put(i, containerSplited1);
                sign1Splited = i;
                break;
            }
        }

        while (true) {
            int i = random.nextInt();
            if (integerWorldFEContainerMap.get(i) == null) {
                integerWorldFEContainerMap.put(i, containerSplited2);
                sign2Splited = i;
                break;
            }
        }

        markDirty();

    }

    public int splitOutPart(int signToBeSplit, BigInteger splitOutMaxEnergy) {
        WorldFEContainer container = integerWorldFEContainerMap.get(signToBeSplit);
        BigInteger subtractedMax = container.getMax().subtract(splitOutMaxEnergy);
        if (subtractedMax.compareTo(BigInteger.ZERO) <= 0) {
            return signToBeSplit;
        }else {
            BigInteger currentSplited = container.getCurrent().multiply(splitOutMaxEnergy.divide(container.getMax()));
            WorldFEContainer containerSplitOut = new WorldFEContainer(BigInteger.ZERO, splitOutMaxEnergy, currentSplited);
            container.setMax(container.getMax().subtract(subtractedMax));
            container.setCurrent(container.getCurrent().subtract(currentSplited));
            Random random = new Random();

            while (true) {
                int i = random.nextInt();
                if (integerWorldFEContainerMap.get(i) == null) {
                    integerWorldFEContainerMap.put(i, containerSplitOut);
                    markDirty();
                    return i;
                }
            }

        }

    }

    public int mergeCable(int sign1, int sign2) {
        WorldFEContainer container1 = integerWorldFEContainerMap.get(sign1);
        WorldFEContainer container2 = integerWorldFEContainerMap.get(sign2);

        WorldFEContainer newContainer = new WorldFEContainer(BigInteger.ZERO, container1.getMax().add(container2.getMax()), container1.getCurrent().add(container2.getCurrent()));

        integerWorldFEContainerMap.remove(sign1);
        integerWorldFEContainerMap.remove(sign2);

        Random random = new Random();

        while (true) {
            int i = random.nextInt();
            if (integerWorldFEContainerMap.get(i) == null) {
                integerWorldFEContainerMap.put(i, newContainer);
                markDirty();
                return i;
            }
        }

    }
}
