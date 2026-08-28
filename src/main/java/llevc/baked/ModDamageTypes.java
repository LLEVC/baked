package llevc.baked;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

import java.rmi.registry.Registry;

public class ModDamageTypes {

    public static ResourceKey<DamageType> lungCancer = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("baked","lungs"));

    public static void initialize() {
    }

}
