package com.gregtechceu.gtceu.common.recipe.gui;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.drawable.progress.ProgressDrawable.Direction;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.value.ProgressValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.ResearchData;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeViewerCapabilityLayoutBuilder;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;
import java.util.ArrayList;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public final class JeiRecipeTypeCustomLayouts {
   public static final Function<GTRecipe, Flow> ASSEMBLY_LINE = JeiRecipeTypeCustomLayouts::assemblyLine;
   public static final Function<GTRecipe, Flow> RESEARCH_STATION = JeiRecipeTypeCustomLayouts::researchStation;
   private static final UITexture ASSEMBLY_PROGRESS = texture("gtceu:textures/gui/progress_bar/progress_bar_assembly_line.png");
   private static final UITexture ASSEMBLY_ARROW = texture("gtceu:textures/gui/progress_bar/progress_bar_assembly_line_arrow.png");
   private static final UITexture RESEARCH_BASE = texture("gtceu:textures/gui/progress_bar/progress_bar_research_station_base.png");
   private static final UITexture RESEARCH_PROGRESS_1 = texture("gtceu:textures/gui/progress_bar/progress_bar_research_station_1.png");
   private static final UITexture RESEARCH_PROGRESS_2 = texture("gtceu:textures/gui/progress_bar/progress_bar_research_station_2.png");
   private static final UITexture RESEARCH_OVERLAY = texture("gtceu:textures/gui/overlay/research_station_overlay.png");
   private static final UITexture SCANNER_OVERLAY = texture("gtceu:textures/gui/overlay/scanner_overlay.png");
   private static final UITexture DATA_ORB_OVERLAY = texture("gtceu:textures/gui/overlay/data_orb_overlay.png");

   private JeiRecipeTypeCustomLayouts() {
   }

   public static Flow assemblyLine(GTRecipe var0) {
      Flow var1 = (Flow)((Flow)((Flow)Flow.row().horizontalCenter()).size(154, 80)).name("assembly_line_custom_ui");
      ParentWidget var2 = (ParentWidget)new ParentWidget().size(154, 80);
      ProgressValue var3 = DoubleValue.simulateProgress(2000);
      var2.child(
         (IWidget)((ProgressWidget)progressWidget(
                  ASSEMBLY_PROGRESS.getSubArea(0.0F, 0.0F, 1.0F, 0.5F),
                  ASSEMBLY_PROGRESS.getSubArea(0.0F, 0.5F, 1.0F, 1.0F),
                  Direction.RIGHT,
                  var3::getDoubleValue
               )
               .pos(76, 5))
            .size(53, 69)
      );

      for (int var4 = 0; var4 < 16; var4++) {
         int var5 = 4 + var4 % 4 * 18;
         int var6 = 4 + var4 / 4 * 18;
         var2.child((IWidget)itemSlot(IO.IN, var4, IDrawable.EMPTY).pos(var5, var6));
      }

      for (int var7 = 0; var7 < 4; var7++) {
         var2.child((IWidget)fluidSlot(var7).pos(93, 4 + var7 * 18));
      }

      var2.child(
         (IWidget)((ProgressWidget)progressWidget(
                  ASSEMBLY_ARROW.getSubArea(0.0F, 0.0F, 1.0F, 0.5F), ASSEMBLY_ARROW.getSubArea(0.0F, 0.5F, 1.0F, 1.0F), Direction.UP, var3::getDoubleValue
               )
               .pos(134, 22))
            .size(10, 18)
      );
      var2.child((IWidget)itemSlot(IO.OUT, 0, IDrawable.EMPTY).pos(130, 4));
      var2.child(
         (IWidget)((RecipeViewerSlotWidget)((RecipeViewerSlotWidget)RecipeViewerSlotWidget.create()
                  .value(ItemStack.EMPTY)
                  .recipeSlotRole(RecipeSlotRole.CATALYST)
                  .background(new IDrawable[]{GuiTextures.SLOT_ITEM, DATA_ORB_OVERLAY}))
               .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, IO.IN, 16)))
            .pos(130, 41)
      );
      var1.child(var2);
      return var1;
   }

   public static Flow researchStation(GTRecipe var0) {
      Flow var1 = (Flow)((Flow)((Flow)Flow.row().horizontalCenter()).size(136, 72)).name("research_station_custom_ui");
      ParentWidget var2 = (ParentWidget)new ParentWidget().size(136, 72);
      ProgressValue var3 = DoubleValue.simulateProgress(2000);
      var2.child(
         (IWidget)((ProgressWidget)progressWidget(
                  RESEARCH_PROGRESS_1.getSubArea(0.0F, 0.0F, 1.0F, 0.5F),
                  RESEARCH_PROGRESS_1.getSubArea(0.0F, 0.5F, 1.0F, 1.0F),
                  Direction.RIGHT,
                  () -> Math.min(1.0, var3.getDoubleValue() / 0.75)
               )
               .pos(62, 28))
            .size(54, 5)
      );
      var2.child(
         (IWidget)((ProgressWidget)progressWidget(
                  RESEARCH_PROGRESS_2.getSubArea(0.0F, 0.0F, 1.0F, 0.5F),
                  RESEARCH_PROGRESS_2.getSubArea(0.0F, 0.5F, 1.0F, 1.0F),
                  Direction.DOWN,
                  () -> Math.max(0.0, (var3.getDoubleValue() - 0.75) / 0.25)
               )
               .pos(109, 31))
            .size(10, 16)
      );
      var2.child((IWidget)((Widget)RESEARCH_BASE.asWidget().pos(0, 0)).size(84, 60));
      var2.child((IWidget)itemSlot(IO.IN, 0, RESEARCH_OVERLAY).pos(87, 21));
      var2.child((IWidget)itemSlot(IO.IN, 1, SCANNER_OVERLAY).pos(33, 21));
      var2.child((IWidget)itemSlot(IO.OUT, 0, DATA_ORB_OVERLAY).pos(105, 50));
      var1.child(var2);
      return var1;
   }

   public static void attachTextCapabilities(GTRecipeViewerWidget var0, GTRecipeTypeUILayout var1) {
      RecipeViewerCapabilityLayoutBuilder.EU.createCapabilityUILayout(var1, var0, IO.IN);
      RecipeViewerCapabilityLayoutBuilder.EU.createCapabilityUILayout(var1, var0, IO.OUT);
      RecipeViewerCapabilityLayoutBuilder.COMPUTATION.createCapabilityUILayout(var1, var0, IO.IN);
      RecipeViewerCapabilityLayoutBuilder.COMPUTATION.createCapabilityUILayout(var1, var0, IO.OUT);
   }

   public static void attachResearchInfo(GTRecipe var0, GTRecipeViewerWidget var1) {
      ArrayList var2 = new ArrayList();

      for (RecipeCondition var4 : var0.conditions) {
         if (var4 instanceof ResearchCondition) {
            ResearchCondition var5 = (ResearchCondition)var4;

            for (ResearchData.ResearchEntry var7 : var5.getData()) {
               var2.add(var7.dataItem());
            }
         }
      }

      if (!var2.isEmpty()) {
         var1.textComponents.child(Text.lang("gtceu.recipe.research").asWidget());
         String var8 = GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, IO.IN, 16);
         RecipeViewerSlotWidget var9 = (RecipeViewerSlotWidget)WidgetTree.findFirstWithNameNullable(var1, var8, RecipeViewerSlotWidget.class);
         if (var9 != null) {
            ((RecipeViewerSlotWidget)var9.recipeSlotRole(RecipeSlotRole.CATALYST).background(new IDrawable[]{GuiTextures.SLOT_ITEM, DATA_ORB_OVERLAY}))
               .value(ItemStackList.of(var2));
         } else {
            Flow var10 = Flow.row();
            var10.child(
               ((RecipeViewerSlotWidget)((RecipeViewerSlotWidget)RecipeViewerSlotWidget.create().marginLeft(2))
                     .recipeSlotRole(RecipeSlotRole.CATALYST)
                     .background(new IDrawable[]{GuiTextures.SLOT_ITEM, DATA_ORB_OVERLAY}))
                  .value(ItemStackList.of(var2))
            );
            var1.child(var10.name("research_info"));
         }
      }
   }

   public static int categoryWidth(GTRecipeCategory var0) {
      return recipeWidth(var0.getRecipeType());
   }

   public static boolean usesCustomCanvas(GTRecipeType var0) {
      String var1 = var0.registryName.getPath();
      return var1.equals("assembly_line") || var1.equals("research_station");
   }

   public static int categoryHeight(GTRecipeCategory var0) {
      return recipeHeight(var0.getRecipeType());
   }

   public static int recipeWidth(GTRecipeType var0) {
      return isCustomType(var0) ? 158 : 176;
   }

   public static int recipeHeight(GTRecipeType var0) {
      String var1 = var0.registryName.getPath();

      return switch (var1) {
         case "assembly_line" -> 133;
         case "research_station" -> 125;
         default -> 166;
      };
   }

   private static boolean isCustomType(GTRecipeType var0) {
      return usesCustomCanvas(var0);
   }

   private static ProgressWidget progressWidget(IDrawable var0, IDrawable var1, Direction var2, DoubleSupplier var3) {
      return new ProgressWidget().clientValue(var3).texture(var0, var1, var2);
   }

   private static RecipeViewerSlotWidget<?> itemSlot(IO var0, int var1, IDrawable var2) {
      return (RecipeViewerSlotWidget<?>)((RecipeViewerSlotWidget)RecipeViewerSlotWidget.create()
            .value(ItemStack.EMPTY)
            .recipeSlotRole(var0 == IO.IN ? RecipeSlotRole.INPUT : RecipeSlotRole.OUTPUT)
            .background(new IDrawable[]{GuiTextures.SLOT_ITEM, var2}))
         .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, var0, var1));
   }

   private static RecipeViewerSlotWidget<?> fluidSlot(int var0) {
      return (RecipeViewerSlotWidget<?>)((RecipeViewerSlotWidget)RecipeViewerSlotWidget.create()
            .value(FluidStack.EMPTY)
            .recipeSlotRole(RecipeSlotRole.INPUT)
            .background(new IDrawable[]{GuiTextures.SLOT_FLUID}))
         .name(GTRecipeViewerWidget.capabilityWidgetName(FluidRecipeCapability.CAP, IO.IN, var0));
   }

   private static UITexture texture(String var0) {
      return UITexture.fullImage(ResourceLocation.parse(var0));
   }
}
