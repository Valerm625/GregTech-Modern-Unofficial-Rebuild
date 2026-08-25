package com.gregtechceu.gtceu.integration.recipeviewer.emi;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicLong;

public final class EmiRecipeBakeLifecycle {
   private static final String EMI_RECIPES_CLASS = "dev.emi.emi.registry.EmiRecipes";
   private static final AtomicLong GENERATION = new AtomicLong();
   private static final ThreadLocal<EmiRecipeBakeLifecycle.WorkerState> WORKER_STATE = new ThreadLocal<>();
   private static volatile Field activeWorkerField;
   private static volatile Field managerField;

   private EmiRecipeBakeLifecycle() {
   }

   public static void invalidateWorkers() {
      GENERATION.incrementAndGet();
   }

   public static void beginWorker() {
      Thread thread = Thread.currentThread();
      UncaughtExceptionHandler previousHandler = thread.getUncaughtExceptionHandler();
      WORKER_STATE.set(new EmiRecipeBakeLifecycle.WorkerState(GENERATION.get(), previousHandler));
      thread.setUncaughtExceptionHandler((failedThread, error) -> {
         if (!(error instanceof EmiRecipeBakeLifecycle.ObsoleteBakeException) && previousHandler != null) {
            previousHandler.uncaughtException(failedThread, error);
         }
      });
   }

   public static <T> List<T> cancellationAware(List<T> recipes) {
      return new EmiRecipeBakeLifecycle.CancellationAwareList<>(recipes);
   }

   public static boolean isWorkerObsolete() {
      EmiRecipeBakeLifecycle.WorkerState state = WORKER_STATE.get();
      return state != null && state.generation() != GENERATION.get();
   }

   public static boolean clearActiveWorkerIfCurrent(Object worker) {
      try {
         Class<?> recipesClass = Class.forName("dev.emi.emi.registry.EmiRecipes", false, worker.getClass().getClassLoader());
         Field field = activeWorkerField(recipesClass);
         synchronized (recipesClass) {
            if (field.get(null) == worker) {
               field.set(null, null);
            }
         }

         return true;
      } catch (LinkageError | ReflectiveOperationException var6) {
         return false;
      }
   }

   public static boolean publishManagerIfCurrent(Object worker, Object manager) {
      try {
         Class<?> recipesClass = Class.forName("dev.emi.emi.registry.EmiRecipes", false, worker.getClass().getClassLoader());
         Field active = activeWorkerField(recipesClass);
         Field managerTarget = managerField;
         if (managerTarget == null) {
            managerTarget = recipesClass.getField("manager");
            managerField = managerTarget;
         }

         synchronized (recipesClass) {
            if (active.get(null) == worker && !isWorkerObsolete()) {
               managerTarget.set(null, manager);
            }
         }

         return true;
      } catch (LinkageError | ReflectiveOperationException var8) {
         return false;
      }
   }

   public static void endWorker() {
      EmiRecipeBakeLifecycle.WorkerState state = WORKER_STATE.get();
      WORKER_STATE.remove();
      if (state != null) {
         Thread.currentThread().setUncaughtExceptionHandler(state.previousHandler());
      }
   }

   public static boolean isCancellation(Throwable error) {
      return error instanceof EmiRecipeBakeLifecycle.ObsoleteBakeException;
   }

   private static void checkCurrentGeneration() {
      if (isWorkerObsolete()) {
         throw EmiRecipeBakeLifecycle.ObsoleteBakeException.INSTANCE;
      }
   }

   private static Field activeWorkerField(Class<?> recipesClass) throws NoSuchFieldException {
      Field field = activeWorkerField;
      if (field == null) {
         field = recipesClass.getField("activeWorker");
         activeWorkerField = field;
      }

      return field;
   }

   private static final class CancellationAwareList<T> extends AbstractList<T> {
      private final List<T> delegate;

      private CancellationAwareList(List<T> delegate) {
         this.delegate = delegate;
      }

      @Override
      public T get(int index) {
         EmiRecipeBakeLifecycle.checkCurrentGeneration();
         return this.delegate.get(index);
      }

      @Override
      public int size() {
         EmiRecipeBakeLifecycle.checkCurrentGeneration();
         return this.delegate.size();
      }

      @Override
      public Iterator<T> iterator() {
         final Iterator<T> delegateIterator = this.delegate.iterator();
         return new Iterator<T>() {
            @Override
            public boolean hasNext() {
               EmiRecipeBakeLifecycle.checkCurrentGeneration();
               return delegateIterator.hasNext();
            }

            @Override
            public T next() {
               EmiRecipeBakeLifecycle.checkCurrentGeneration();
               if (!delegateIterator.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  return delegateIterator.next();
               }
            }
         };
      }

      @Override
      public Spliterator<T> spliterator() {
         return Spliterators.spliteratorUnknownSize(this.iterator(), 16);
      }
   }

   private static final class ObsoleteBakeException extends RuntimeException {
      private static final EmiRecipeBakeLifecycle.ObsoleteBakeException INSTANCE = new EmiRecipeBakeLifecycle.ObsoleteBakeException();

      private ObsoleteBakeException() {
         super("Obsolete EMI recipe bake cancelled", null, false, false);
      }
   }

   private static record WorkerState(long generation, UncaughtExceptionHandler previousHandler) {
   }
}
