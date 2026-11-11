package dev.lopyluna.gnkinetics.mixins.remappings;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.InsertableLinkedOpenCustomHashSet;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(BuildCreativeModeTabContentsEvent.class)
public abstract class BuildCreativeModeTabContentsEventMixin {
    @Shadow
    public abstract ObjectSortedSet<ItemStack> getParentEntries();

    @Shadow
    public abstract ObjectSortedSet<ItemStack> getSearchEntries();

    @WrapWithCondition(method = {"accept", "insertBefore", "insertAfter", "insertFirst"}, at = @At(value = "INVOKE", target = "/add.*/"))
    private boolean preventDuplicateItemStacks(InsertableLinkedOpenCustomHashSet<ItemStack> instance, Object first, @Coerce Object second, @Local(argsOnly = true) CreativeModeTab.TabVisibility visibility) {
        var list = visibility == CreativeModeTab.TabVisibility.PARENT_TAB_ONLY || visibility == CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS ? this.getParentEntries() : this.getSearchEntries();
        // first argument is only the item to be added if the second argument does not exist
        ItemStack newEntry = (ItemStack) (second != null ? second : first);
        return !list.contains(newEntry);
    }
}
