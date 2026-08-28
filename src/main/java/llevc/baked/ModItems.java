package llevc.baked;

import llevc.baked.items.SmokeItem;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.CommonColors;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.block.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ModItems {
    public static <GenericItem extends Item> GenericItem register(String name, Function<Item.Properties, GenericItem> itemFactory, Item.Properties settings) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Baked.MOD_ID, name));
        GenericItem item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }

    //the baked shi
    public static final Item snortable = register(
            "snortable",
            Item::new,
            new Item.Properties().stacksTo(16)
                    .component(DataComponents.CONSUMABLE, Consumables.defaultFood().consumeSeconds(0.8f).animation(ItemUseAnimation.SPYGLASS).build())
                    .component(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.DYED_COLOR,true))
                    .component(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                    .component(DataComponents.DYED_COLOR,new DyedItemColor(CommonColors.WHITE))
    );
    public static final PotionContents cigContents = new PotionContents(Optional.empty(),Optional.empty(),List.of(
            new MobEffectInstance(MobEffects.REGENERATION,45*20),
            new MobEffectInstance(MobEffects.NAUSEA,45*20)
    ),Optional.empty());
    public static final Item cig = register(
            "cig",
            SmokeItem::new,
            new Item.Properties().durability(32).component(DataComponents.POTION_CONTENTS, cigContents).equippable(EquipmentSlot.HEAD)
    );
    public static final Item vape = register(
            "vape",
            SmokeItem::new,
            new Item.Properties().durability(64).component(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).repairable(ItemTags.REPAIRS_IRON_ARMOR)
    );
    public static final Item pick = register(
            "pick",
            SmokeItem::new,
            new Item.Properties().durability(8).component(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).equippable(EquipmentSlot.HEAD)
    );
    public static final Item cartridge = register(
            "cartridge",
            Item::new,
            new Item.Properties().component(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
    );
    public static ItemStack createSnortable(Block block) {
        ItemStack yes = new ItemStack(ModItems.snortable);
        List<MobEffectInstance> effectInstanceList = new ArrayList<>();

        if (block.getFriction() > 0.75) { // slippppery
            effectInstanceList.add(new MobEffectInstance(MobEffects.SPEED, (int) Math.ceil(400/block.getFriction())));
        }
        if (block.getSpeedFactor() < 1) { // is it sticky?
            effectInstanceList.add(new MobEffectInstance(MobEffects.SLOWNESS, (int) Math.ceil(200/block.getSpeedFactor())));
        }
        if (block.getJumpFactor() < 1) { // sticky 2, only honey has this one
            effectInstanceList.add(new MobEffectInstance(MobEffects.SLOW_FALLING,(int) Math.ceil(200/block.getJumpFactor())));
        } else if (block.getJumpFactor() > 1) { // bouncy but not bouncy
            effectInstanceList.add(new MobEffectInstance(MobEffects.JUMP_BOOST,(int) Math.ceil(200*block.getJumpFactor())));
        }
        if (block instanceof InfestedBlock) { // is infested?
            effectInstanceList.add(new MobEffectInstance(MobEffects.INFESTED,60*20));
        } else if (block instanceof SlimeBlock) { // slimme
            effectInstanceList.add(new MobEffectInstance(MobEffects.OOZING,600));
        } else if (block instanceof WebBlock) { // cobweb
            effectInstanceList.add(new MobEffectInstance(MobEffects.WEAVING,1200));
        }
        if (block.defaultDestroyTime() > 20 || block.defaultDestroyTime() < 0) { // long/impossible to break
            effectInstanceList.add(new MobEffectInstance(MobEffects.MINING_FATIGUE, 1000));
            effectInstanceList.add(new MobEffectInstance(MobEffects.RESISTANCE,1000));
        } else if (block.defaultDestroyTime() < 0.5 && block.defaultDestroyTime() >= 0) { // instant break
            effectInstanceList.add(new MobEffectInstance(MobEffects.HASTE, (int) Math.ceil(200/(0.5+block.defaultDestroyTime()))));
        }

        PotionContents huh = new PotionContents(Optional.empty(),Optional.empty(),effectInstanceList,Optional.empty());
        yes.set(DataComponents.POTION_CONTENTS,huh);
        yes.set(DataComponents.DYED_COLOR, new DyedItemColor(block.defaultMapColor().col));
        yes.set(DataComponents.ITEM_NAME, Component.translatable("item.baked.snortable").append(block.getName()));
        yes.set(DataComponents.CONSUMABLE, Consumables.defaultFood().consumeSeconds(Math.abs(block.defaultDestroyTime()/3)).animation(ItemUseAnimation.SPYGLASS).build());
        return yes;
    }

    public static final ResourceKey<CreativeModeTab> bakedBakedItemGroupKey = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(Baked.MOD_ID, "baked"));
    public static final CreativeModeTab bakedBakedItemGroup = FabricCreativeModeTab.builder().icon(() -> new ItemStack(vape)).displayItems((params,output) -> {
        output.accept(pick);
        for (int i = 0; i < BuiltInRegistries.POTION.size(); i++) {
            Optional<Holder.Reference<Potion>> wchat = BuiltInRegistries.POTION.get(i);
            if (wchat.isPresent()) {
                Potion hey = wchat.get().value();
                output.accept(PotionContents.createItemStack(pick,Holder.direct(hey)));
            }
        }
        output.accept(cig);
        for (int i = 0; i < BuiltInRegistries.POTION.size(); i++) {
            Optional<Holder.Reference<Potion>> wchat = BuiltInRegistries.POTION.get(i);
            if (wchat.isPresent()) {
                Potion hey = wchat.get().value();
                ItemStack yo = PotionContents.createItemStack(cig,Holder.direct(hey));
                yo.set(DataComponents.ITEM_NAME,Component.translatable("item.baked.custom_cig"));
                output.accept(yo);
            }
        }
        output.accept(vape);
        for (int i = 0; i < BuiltInRegistries.POTION.size(); i++) {
            Optional<Holder.Reference<Potion>> wchat = BuiltInRegistries.POTION.get(i);
            if (wchat.isPresent()) {
                Potion hey = wchat.get().value();
                output.accept(PotionContents.createItemStack(vape,Holder.direct(hey)));
            }
        }
        output.accept(cartridge);
        for (int i = 0; i < BuiltInRegistries.POTION.size(); i++) {
            Optional<Holder.Reference<Potion>> wchat = BuiltInRegistries.POTION.get(i);
            if (wchat.isPresent()) {
                Potion hey = wchat.get().value();
                output.accept(PotionContents.createItemStack(cartridge,Holder.direct(hey)));
            }
        }
        output.accept(snortable);
        //for (int i = 0; i < BuiltInRegistries.BLOCK.size(); i++) {
        //    Optional<Holder.Reference<Block>> wchat = BuiltInRegistries.BLOCK.get(i);
        //    if (wchat.isPresent()) {
        //        Block hey = wchat.get().value();
        //        ItemStack woah = createSnortable(hey);
        //        output.accept(woah);
        //    }
        //}
        for (int i = 0; i < BuiltInRegistries.POTION.size(); i++) {
            Optional<Holder.Reference<Potion>> wchat = BuiltInRegistries.POTION.get(i);
            if (wchat.isPresent()) {
                Potion hey = wchat.get().value();
                ItemStack yo = PotionContents.createItemStack(snortable,Holder.direct(hey));
                yo.set(DataComponents.DYED_COLOR,new DyedItemColor(yo.getOrDefault(DataComponents.POTION_CONTENTS,PotionContents.EMPTY).getColorOr(CommonColors.WHITE)));
                yo.set(DataComponents.ITEM_NAME, Component.translatable("item.baked.snortable").append(hey.name()));
                output.accept(yo);
            }
        }}).title(Component.translatable("itemGroup.baked")).build();

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,bakedBakedItemGroupKey,bakedBakedItemGroup);
    }
}
