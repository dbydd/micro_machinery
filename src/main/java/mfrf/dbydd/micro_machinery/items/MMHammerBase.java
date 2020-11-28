package mfrf.dbydd.micro_machinery.items;

import mfrf.dbydd.micro_machinery.utils.MultiBlockStructureMaps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;

public class MMHammerBase extends ToolItem {
    public static Map<String, Supplier<Item>> registeries = new HashMap<>();

    /**
     * @param attackDamageIn
     * @param attackSpeedIn
     * @param tier
     */
    public MMHammerBase(float attackDamageIn, float attackSpeedIn, IItemTier tier, Item.Properties builder, String name) {
        super(attackDamageIn, attackSpeedIn, tier, new HashSet<>(), builder);
        registeries.put(name, () -> this);
        this.addPropertyOverride(new ResourceLocation("damage_tier"), (p_call_1_, p_call_2_, p_call_3_) -> {
            float value = (float) p_call_1_.getDamage() / (float) p_call_1_.getMaxDamage();
            return (float) (value <= 0.4 ? 1.0 : value <= 0.6 ? 2.0 : 3.0);
        });
    }

    @Override
    public boolean canHarvestBlock(BlockState blockIn) {
        Block block = blockIn.getBlock();
        int i = this.getTier().getHarvestLevel();
        if (blockIn.getHarvestTool() == net.minecraftforge.common.ToolType.PICKAXE) {
            return i >= blockIn.getHarvestLevel();
        }
        Material material = blockIn.getMaterial();
        return material == Material.ROCK || material == Material.IRON || material == Material.ANVIL;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        Material material = state.getMaterial();
        return material != Material.IRON && material != Material.ANVIL && material != Material.ROCK ? super.getDestroySpeed(stack, state) : this.efficiency;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (!worldIn.isRemote && state.getBlockHardness(worldIn, pos) != 0.0F) {
            stack.damageItem(1, entityLiving, (player) -> {
                player.sendBreakAnimation(EquipmentSlotType.MAINHAND);
            });

            Vec3d lookVec = entityLiving.getLookVec();
            Direction direction = Direction.getFacingFromVector(lookVec.x, lookVec.y, lookVec.z);
            if (direction == Direction.DOWN || direction == Direction.UP) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos position = new BlockPos(pos.getX() + x, pos.getY(), pos.getZ() + z);
                        digBlock(worldIn, position, entityLiving);
                    }
                }
            } else {
                if (direction == Direction.WEST || direction == Direction.EAST) {
                    for (int z = -1; z <= 1; z++) {
                        for (int y = -1; y <= 1; y++) {
                            BlockPos position = new BlockPos(pos.getX(), pos.getY() + y, pos.getZ() + z);
                            digBlock(worldIn, position, entityLiving);
                        }
                    }
                } else {
                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            BlockPos position = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ());
                            digBlock(worldIn, position, entityLiving);
                        }
                    }
                }
            }
        }
        return true;
    }

    private void digBlock(World worldIn, BlockPos position, LivingEntity entityLiving) {
        BlockState blockstate = worldIn.getBlockState(position);
        if (canHarvestBlock(blockstate)) {
            worldIn.destroyBlock(position, true, entityLiving);
        }
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (!context.getWorld().isRemote()) {
            for (Map.Entry<String, MultiBlockStructureMaps.MultiBlockPosBox> stringMultiBlockPosBoxEntry : MultiBlockStructureMaps.getStructureMaps().entrySet()) {
                if (stringMultiBlockPosBoxEntry.getValue().match(context.getPos(), context.getWorld(), context.getFace())) {
                    context.getPlayer().sendMessage(new StringTextComponent("match:" + stringMultiBlockPosBoxEntry.getKey()));
                    return ActionResultType.SUCCESS;
                }
            }
        }

        return super.onItemUse(context);
    }

    public void placeStructure(BlockPos pos, MultiBlockStructureMaps.MultiBlockPosBox blockPosBox, World world, Direction direction) {
        //todo place the structure,require rotate box first
    }

}
