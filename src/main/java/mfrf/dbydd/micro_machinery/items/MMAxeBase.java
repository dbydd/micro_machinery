package mfrf.dbydd.micro_machinery.items;

import net.minecraft.item.AxeItem;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MMAxeBase extends AxeItem {
    public static Map<String, Supplier<Item>> registeries = new HashMap<>();

    /**
     * @param material_tier        材料等级，见枚举类EnumToolTier
     * @param damage_correct_value 伤害修正值，同MMSwordBase
     */
    public MMAxeBase(IItemTier material_tier, int damage_correct_value, float attack_speed, Properties properties, String name) {
        super(material_tier, damage_correct_value, attack_speed, properties);
        registeries.put(name, () -> this);
    }

}
