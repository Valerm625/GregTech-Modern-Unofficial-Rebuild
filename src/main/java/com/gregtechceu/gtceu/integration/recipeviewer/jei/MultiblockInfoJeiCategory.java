package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget.CandidateInteractionMode;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget.CandidateSlotDescriptor;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.widget.sizer.Area;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiblockInfoJeiCategory extends
                                       SizedModularUIRecipeCategory<MultiblockInfoJeiCategory.MultiblockPreviewInfoWrapper> {

    public final static RecipeType<MultiblockPreviewInfoWrapper> RECIPE_TYPE = new RecipeType<>(
            GTCEu.id("multiblock_info"),
            MultiblockPreviewInfoWrapper.class);
    private final IDrawable icon;

    public MultiblockInfoJeiCategory(IJeiHelpers helpers) {
        super(v -> new MultiblockPreviewWidget(v.definition, null,
                MultiblockPreviewWidget.RECIPE_VIEWER_WIDTH, MultiblockPreviewWidget.RECIPE_VIEWER_HEIGHT,
                lateBound -> new GTJeiRecipeViewerSlot().lateBound(lateBound),
                CandidateInteractionMode.RECIPE_VIEWER_READ_ONLY),
                v -> v.definition.getId(),
                MultiblockPreviewWidget.RECIPE_VIEWER_WIDTH,
                MultiblockPreviewWidget.RECIPE_VIEWER_HEIGHT);
        IGuiHelper guiHelper = helpers.getGuiHelper();
        this.icon = guiHelper.createDrawableItemStack(GTMultiMachines.ELECTRIC_BLAST_FURNACE.asStack());
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, GTRegistries.MACHINES.stream()
                .filter(MultiblockMachineDefinition.class::isInstance)
                .map(MultiblockMachineDefinition.class::cast)
                .filter(MultiblockMachineDefinition::isRenderXEIPreview)
                .map(MultiblockPreviewInfoWrapper::new)
                .toList());
    }

    @Override
    public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull MultiblockPreviewInfoWrapper recipe,
                                   @NotNull IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        MultiblockPreviewWidget preview = getModularScreen(recipe).getMainPanel().getChildren().stream()
                .filter(MultiblockPreviewWidget.class::isInstance)
                .map(MultiblockPreviewWidget.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing native multiblock preview widget"));
        List<IRecipeSlotDrawable> materialSlots = new ArrayList<>(builder.getRecipeSlots().getSlots());
        IJeiRuntime runtime = GTJEIPlugin.getRuntime();
        if (runtime == null) {
            throw new IllegalStateException("JEI runtime is unavailable while creating multiblock recipe extras");
        }
        builder.addSlottedWidget(new NativeSlotsWidget(preview, materialSlots, runtime.getRecipeManager(),
                runtime.getIngredientManager()), materialSlots);
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(MultiblockPreviewInfoWrapper recipe) {
        return recipe.definition.getId();
    }

    @Override
    @NotNull
    public RecipeType<MultiblockPreviewInfoWrapper> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.multiblock_info");
    }

    @NotNull
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    public record MultiblockPreviewInfoWrapper(MultiblockMachineDefinition definition) {}

    static final class NativeSlotsWidget implements ISlottedRecipeWidget {

        private static final ScreenPosition POSITION = new ScreenPosition(0, 0);

        private final Supplier<List<CandidateSlotDescriptor>> candidateSupplier;
        private final List<IRecipeSlotDrawable> materialSlots;
        private final IRecipeManager recipeManager;
        private final Function<List<ItemStack>, List<Optional<ITypedIngredient<?>>>> ingredientConverter;
        private List<CandidateSlotDescriptor> candidateDescriptors = List.of();
        private List<IRecipeSlotDrawable> candidateSlots = List.of();

        NativeSlotsWidget(MultiblockPreviewWidget preview, List<IRecipeSlotDrawable> materialSlots,
                          IRecipeManager recipeManager, IIngredientManager ingredientManager) {
            this(preview::getLateCandidateSlots, materialSlots, recipeManager, ingredientManager);
        }

        NativeSlotsWidget(Supplier<List<CandidateSlotDescriptor>> candidateSupplier,
                          List<IRecipeSlotDrawable> materialSlots, IRecipeManager recipeManager,
                          IIngredientManager ingredientManager) {
            this(candidateSupplier, materialSlots, recipeManager,
                    candidates -> toTypedIngredients(ingredientManager, candidates));
        }

        NativeSlotsWidget(Supplier<List<CandidateSlotDescriptor>> candidateSupplier,
                          List<IRecipeSlotDrawable> materialSlots, IRecipeManager recipeManager,
                          Function<List<ItemStack>, List<Optional<ITypedIngredient<?>>>> ingredientConverter) {
            this.candidateSupplier = candidateSupplier;
            this.materialSlots = List.copyOf(materialSlots);
            this.recipeManager = recipeManager;
            this.ingredientConverter = ingredientConverter;
        }

        @Override
        public ScreenPosition getPosition() {
            return POSITION;
        }

        @Override
        public void drawWidget(GuiGraphics graphics, double mouseX, double mouseY) {
            synchronizeCandidateSlots();
            drawSlots(this.materialSlots, graphics, mouseX, mouseY);
            drawSlots(this.candidateSlots, graphics, mouseX, mouseY);
        }

        @Override
        public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
            synchronizeCandidateSlots();
            Optional<RecipeSlotUnderMouse> material = findSlot(this.materialSlots, mouseX, mouseY);
            return material.isPresent() ? material : findSlot(this.candidateSlots, mouseX, mouseY);
        }

        private void synchronizeCandidateSlots() {
            List<CandidateSlotDescriptor> current = this.candidateSupplier.get();
            if (current != this.candidateDescriptors) {
                this.candidateDescriptors = current;
                List<IRecipeSlotDrawable> rebuilt = new ArrayList<>(current.size());
                for (CandidateSlotDescriptor descriptor : current) {
                    List<Optional<ITypedIngredient<?>>> ingredients =
                            this.ingredientConverter.apply(descriptor.candidates());
                    IRecipeSlotDrawable slot = this.recipeManager.createRecipeSlotDrawable(
                            RecipeIngredientRole.INPUT, ingredients, Set.of(), 0);
                    slot.addTooltipCallback((view, tooltip) -> tooltip.addAll(descriptor.tooltips()));
                    rebuilt.add(slot);
                }
                this.candidateSlots = List.copyOf(rebuilt);
            }

            for (int i = 0; i < this.candidateSlots.size(); i++) {
                Area area = this.candidateDescriptors.get(i).source().getArea();
                this.candidateSlots.get(i).setPosition(area.x(), area.y());
            }
        }

        private static List<Optional<ITypedIngredient<?>>> toTypedIngredients(
                IIngredientManager ingredientManager, List<ItemStack> candidates) {
            return candidates.stream()
                    .<Optional<ITypedIngredient<?>>>map(stack ->
                            ingredientManager.createTypedIngredient(VanillaTypes.ITEM_STACK, stack)
                            .map(typed -> (ITypedIngredient<?>) typed))
                    .toList();
        }

        private static void drawSlots(List<IRecipeSlotDrawable> slots, GuiGraphics graphics,
                                      double mouseX, double mouseY) {
            for (IRecipeSlotDrawable slot : slots) {
                slot.draw(graphics, slot.isMouseOver(mouseX, mouseY));
            }
        }

        private static Optional<RecipeSlotUnderMouse> findSlot(List<IRecipeSlotDrawable> slots,
                                                                double mouseX, double mouseY) {
            return slots.stream()
                    .filter(slot -> slot.isMouseOver(mouseX, mouseY))
                    .findFirst()
                    .map(slot -> new RecipeSlotUnderMouse(slot, 0, 0));
        }
    }
}
