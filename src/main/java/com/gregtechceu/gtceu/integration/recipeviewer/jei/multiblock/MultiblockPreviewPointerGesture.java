package com.gregtechceu.gtceu.integration.recipeviewer.jei.multiblock;

final class MultiblockPreviewPointerGesture {
   static final double DEFAULT_DRAG_THRESHOLD_SQUARED = 9.0;
   private final double dragThresholdSquared;
   private boolean active;
   private boolean rotationArmed;
   private boolean dragged;
   private double pressX;
   private double pressY;
   private double lastX;
   private double lastY;

   MultiblockPreviewPointerGesture(double dragThresholdSquared) {
      this.dragThresholdSquared = dragThresholdSquared;
   }

   boolean begin(double mouseX, double mouseY, boolean insideModel, boolean hasClickAction) {
      this.clear();
      if (!insideModel && !hasClickAction) {
         return false;
      } else {
         this.active = true;
         this.rotationArmed = insideModel;
         this.pressX = mouseX;
         this.pressY = mouseY;
         this.lastX = mouseX;
         this.lastY = mouseY;
         return true;
      }
   }

   MultiblockPreviewPointerGesture.Drag drag(double mouseX, double mouseY) {
      if (!this.active) {
         return MultiblockPreviewPointerGesture.Drag.NONE;
      } else {
         double totalX = mouseX - this.pressX;
         double totalY = mouseY - this.pressY;
         if (!this.dragged && totalX * totalX + totalY * totalY >= this.dragThresholdSquared) {
            this.dragged = true;
         }

         if (this.rotationArmed && this.dragged) {
            MultiblockPreviewPointerGesture.Drag drag = new MultiblockPreviewPointerGesture.Drag(true, mouseX - this.lastX, mouseY - this.lastY);
            this.lastX = mouseX;
            this.lastY = mouseY;
            return drag;
         } else {
            return MultiblockPreviewPointerGesture.Drag.NONE;
         }
      }
   }

   MultiblockPreviewPointerGesture.Finish complete(double mouseX, double mouseY) {
      if (!this.active) {
         return new MultiblockPreviewPointerGesture.Finish(MultiblockPreviewPointerGesture.Completion.NONE, MultiblockPreviewPointerGesture.Drag.NONE);
      } else {
         MultiblockPreviewPointerGesture.Drag finalDrag = this.drag(mouseX, mouseY);
         MultiblockPreviewPointerGesture.Completion completion = this.dragged
            ? MultiblockPreviewPointerGesture.Completion.DRAG
            : MultiblockPreviewPointerGesture.Completion.CLICK;
         this.clear();
         return new MultiblockPreviewPointerGesture.Finish(completion, finalDrag);
      }
   }

   boolean isActive() {
      return this.active;
   }

   void clear() {
      this.active = false;
      this.rotationArmed = false;
      this.dragged = false;
   }

   static enum Completion {
      NONE,
      CLICK,
      DRAG;
   }

   static record Drag(boolean rotate, double deltaX, double deltaY) {
      private static final MultiblockPreviewPointerGesture.Drag NONE = new MultiblockPreviewPointerGesture.Drag(false, 0.0, 0.0);
   }

   static record Finish(MultiblockPreviewPointerGesture.Completion completion, MultiblockPreviewPointerGesture.Drag drag) {
   }
}
