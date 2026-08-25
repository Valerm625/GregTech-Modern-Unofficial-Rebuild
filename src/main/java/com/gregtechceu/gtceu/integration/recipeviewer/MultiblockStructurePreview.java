package com.gregtechceu.gtceu.integration.recipeviewer;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public interface MultiblockStructurePreview {
   String IMPLEMENTATION = "com.gregtechceu.gtceu.integration.recipeviewer.jei.multiblock.GTMultiblockStructureJeiPlugin";

   static MultiblockStructurePreview create(MultiblockMachineDefinition definition) {
      try {
         Class<?> implementation = Class.forName(
            "com.gregtechceu.gtceu.integration.recipeviewer.jei.multiblock.GTMultiblockStructureJeiPlugin",
            true,
            MultiblockStructurePreview.class.getClassLoader()
         );
         Method factory = implementation.getMethod("createSharedPreview", MultiblockMachineDefinition.class);
         Object result = factory.invoke(null, definition);
         if (result instanceof MultiblockStructurePreview) {
            return (MultiblockStructurePreview)result;
         } else {
            throw new IllegalStateException("The multiblock preview factory returned an incompatible implementation");
         }
      } catch (LinkageError | ReflectiveOperationException var5) {
         Object var10000;
         label21: {
            if (var5 instanceof InvocationTargetException invocation && invocation.getCause() != null) {
               var10000 = invocation.getCause();
               break label21;
            }

            var10000 = var5;
         }

         Throwable cause = (Throwable)var10000;
         throw new IllegalStateException("Could not load the optional multiblock preview implementation", cause);
      }
   }

   int width();

   int height();

   void draw(GuiGraphics var1, double var2, double var4);

   List<Component> tooltipAt(double var1, double var3);

   boolean handleMouseClicked(double var1, double var3, int var5);

   boolean handleMouseReleased(double var1, double var3, int var5);

   boolean handleKeyPressed(double var1, double var3, int var5, int var6, int var7);

   boolean handleMouseScrolled(double var1, double var3, double var5, double var7);

   boolean handleMouseDragged(double var1, double var3, int var5, double var6, double var8);
}
