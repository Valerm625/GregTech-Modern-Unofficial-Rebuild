package com.gregtechceu.gtceu.api.recipe.chance.logic;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.core.mixins.MappedRegistryAccessor;

import net.minecraft.core.MappedRegistry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public final class ChanceLogicCompatibilityGameTest {

    private static final String FIXTURE_BINARY_NAME =
            "com.gregtechceu.gtceu.api.recipe.chance.logic.LegacyChanceLogicBinaryFixture";
    private static final String MISSING_ROLL_FIXTURE_BINARY_NAME =
            "com.gregtechceu.gtceu.api.recipe.chance.logic.MissingRollChanceLogicBinaryFixture";
    private static final IContentSerializer<Integer> INTEGER_SERIALIZER = new IContentSerializer<>() {

        @Override
        public Integer of(Object value) {
            return ((Number) value).intValue();
        }

        @Override
        public Integer defaultValue() {
            return 0;
        }

        @Override
        public Class<Integer> contentClass() {
            return Integer.class;
        }

        @Override
        public Codec<Integer> codec() {
            return Codec.INT;
        }
    };
    private static final RecipeCapability<Integer> INTEGER_CAPABILITY = new RecipeCapability<>(
            GTCEu.id("chance_logic_integer_test"), 0, false, 0, INTEGER_SERIALIZER) {

        @Override
        public Integer copyInner(Integer content) {
            return content;
        }

        @Override
        public Integer copyWithModifier(Integer content, ContentModifier modifier) {
            return modifier.apply(content);
        }
    };

    private ChanceLogicCompatibilityGameTest() {}

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void legacyBinarySubclassReceivesBoostedRolls(GameTestHelper helper)
            throws ReflectiveOperationException {
        Class<? extends ChanceLogic> fixtureClass = new FixtureClassLoader()
                .define(FIXTURE_BINARY_NAME, createFixture(FIXTURE_BINARY_NAME, true));
        helper.assertTrue(Arrays.stream(fixtureClass.getDeclaredMethods())
                .noneMatch(method -> method.getName().equals("roll") && method.getParameterCount() == 7),
                "Legacy binary fixture unexpectedly declares the boosted roll method");

        ChanceLogic fixture = instantiateFixture(fixtureClass, "legacy_chance_logic_binary_fixture");
        Content original = new Content("legacy-marker", 100, ChanceLogic.getMaxChancedValue(), 200);
        List<Content> rolled = fixture.roll(null, List.of(original),
                (entry, recipeTier, chanceTier) -> 4321, 0, 0, null, 1);

        helper.assertTrue(rolled.size() == 1, "Legacy roll bridge did not return its single entry");
        Content effective = rolled.getFirst();
        helper.assertTrue("legacy-marker".equals(effective.content()),
                "Legacy roll bridge changed the underlying recipe content");
        helper.assertTrue(effective.chance() == 4321,
                "Legacy roll bridge did not materialize the boosted chance");
        helper.assertTrue(effective.maxChance() == ChanceLogic.getMaxChancedValue(),
                "Legacy roll bridge changed the maximum chance");
        helper.assertTrue(effective.tierChanceBoost() == 0,
                "Legacy roll bridge retained a tier boost after materializing it");

        helper.assertTrue(ChanceLogic.NONE.roll(null, List.of(original), null, 1).isEmpty(),
                "Legacy entry point did not dispatch to a boosted-only implementation");
        helper.succeed();
    }

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void missingRollOverrideFailsWithoutRecursion(GameTestHelper helper)
            throws ReflectiveOperationException {
        Class<? extends ChanceLogic> fixtureClass = new FixtureClassLoader()
                .define(MISSING_ROLL_FIXTURE_BINARY_NAME,
                        createFixture(MISSING_ROLL_FIXTURE_BINARY_NAME, false));
        ChanceLogic fixture = instantiateFixture(fixtureClass, "missing_roll_chance_logic_binary_fixture");
        Content entry = new Content("missing-roll-marker", 100, ChanceLogic.getMaxChancedValue(), 200);

        assertIllegalState(helper,
                () -> fixture.roll(null, List.of(entry), (content, recipeTier, chanceTier) -> 4321,
                        0, 0, null, 1),
                "boosted entry point accepted an implementation with neither roll override");
        assertIllegalState(helper,
                () -> fixture.roll(null, List.of(entry), null, 1),
                "legacy entry point accepted an implementation with neither roll override");
        helper.succeed();
    }

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void largeParallelOrRollUsesWideArithmetic(GameTestHelper helper) {
        Content entry = new Content(1, 9999, ChanceLogic.getMaxChancedValue(), 0);
        List<Content> rolled = ChanceLogic.OR.roll(INTEGER_CAPABILITY, List.of(entry),
                (content, recipeTier, chanceTier) -> content.chance(), 0, 0, null, 300_000);

        helper.assertTrue(rolled.size() == 1,
                "Large OR roll should collapse its guaranteed output into one content entry");
        helper.assertTrue(Integer.valueOf(299_970).equals(rolled.getFirst().content()),
                "Large OR roll overflowed before calculating its guaranteed output");
        helper.succeed();
    }

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void xorHandlesZeroAndBoostedWeights(GameTestHelper helper) {
        List<Content> allZero = List.of(
                new Content(1, 0, ChanceLogic.getMaxChancedValue(), 1000),
                new Content(1, 0, ChanceLogic.getMaxChancedValue(), 1000));
        List<Content> zeroTierResult = ChanceLogic.XOR.roll(INTEGER_CAPABILITY, allZero,
                ChanceBoostFunction.OVERCLOCK, 1, 1, null, 1);
        helper.assertTrue(zeroTierResult.isEmpty(),
                "All-zero XOR weights incorrectly made the final entry guaranteed");

        List<Content> boosted = List.of(
                new Content(1, 1000, ChanceLogic.getMaxChancedValue(), 0),
                new Content(1, 1000, ChanceLogic.getMaxChancedValue(), 1000));
        List<Content> boostedResult = ChanceLogic.XOR.roll(INTEGER_CAPABILITY, boosted,
                ChanceBoostFunction.OVERCLOCK, 1, 2, null, 30_000);

        helper.assertTrue(boostedResult.size() == 2,
                "Boosted XOR should collapse its two guaranteed weighted outputs");
        int totalOutput = boostedResult.stream().mapToInt(content -> (Integer) content.content()).sum();
        helper.assertTrue(totalOutput == 30_000,
                "Boosted XOR produced more or fewer outputs than its roll count");
        helper.succeed();
    }

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void xorNormalizationMatchesBoostedViewerWeights(GameTestHelper helper) {
        List<Content> entries = List.of(
                new Content("first", 5000, ChanceLogic.getMaxChancedValue(), 750),
                new Content("second", 2500, ChanceLogic.getMaxChancedValue(), 500));
        List<Content> normalized = ChanceLogic.normalizeXorChances(
                entries, ChanceBoostFunction.OVERCLOCK, 1, 3);

        helper.assertTrue(normalized.size() == 2, "Boosted XOR normalization lost an entry");
        helper.assertTrue(normalized.get(0).chance() == 6500 && normalized.get(1).chance() == 3500,
                "Boosted XOR normalization did not produce the expected 65/35 weights");
        helper.assertTrue(normalized.stream().mapToInt(Content::chance).sum() == ChanceLogic.getMaxChancedValue(),
                "Boosted XOR normalization did not preserve the exact total weight");

        List<Content> zeroBaseBoosted = List.of(
                new Content("zero-first", 0, 20000, 1000),
                new Content("zero-second", 0, 10000, 500));
        List<Content> normalizedZeroBase = ChanceLogic.normalizeXorChances(
                zeroBaseBoosted, ChanceBoostFunction.OVERCLOCK, 1, 2);
        helper.assertTrue(normalizedZeroBase.stream().mapToInt(Content::chance).sum() ==
                        ChanceLogic.getMaxChancedValue(),
                "Tier-only XOR weights were incorrectly treated as an all-zero list");
        helper.assertTrue(ChanceLogic.normalizeXorChances(zeroBaseBoosted,
                        ChanceBoostFunction.OVERCLOCK, 1, 1).isEmpty(),
                "Truly zero XOR weights incorrectly received a normalization remainder");
        helper.succeed();
    }

    private static void assertIllegalState(GameTestHelper helper, Runnable action, String failureMessage) {
        boolean failedSafely = false;
        try {
            action.run();
        } catch (IllegalStateException expected) {
            failedSafely = true;
        }
        helper.assertTrue(failedSafely, failureMessage);
    }

    private static ChanceLogic instantiateFixture(Class<? extends ChanceLogic> fixtureClass, String id)
            throws ReflectiveOperationException {
        @SuppressWarnings("unchecked")
        MappedRegistry<ChanceLogic> registry = (MappedRegistry<ChanceLogic>) GTRegistries.CHANCE_LOGICS;
        boolean wasFrozen = ((MappedRegistryAccessor) (Object) registry).gtceu$isFrozen();
        if (wasFrozen) registry.unfreeze();
        try {
            return fixtureClass.getConstructor(ResourceLocation.class).newInstance(GTCEu.id(id));
        } finally {
            if (wasFrozen) registry.freeze();
        }
    }

    private static byte[] createFixture(String binaryName, boolean includeLegacyRoll) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        String chanceLogic = Type.getInternalName(ChanceLogic.class);
        String internalName = binaryName.replace('.', '/');
        writer.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER,
                internalName, null, chanceLogic, null);

        MethodVisitor constructor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ResourceLocation.class)), null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitVarInsn(Opcodes.ALOAD, 1);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, chanceLogic, "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ResourceLocation.class)), false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(0, 0);
        constructor.visitEnd();

        if (includeLegacyRoll) {
            MethodVisitor legacyRoll = writer.visitMethod(Opcodes.ACC_PUBLIC, "roll",
                    Type.getMethodDescriptor(Type.getType(List.class),
                            Type.getType(RecipeCapability.class), Type.getType(List.class),
                            Type.getType(Object2IntMap.class), Type.INT_TYPE),
                    null, null);
            legacyRoll.visitCode();
            legacyRoll.visitVarInsn(Opcodes.ALOAD, 2);
            legacyRoll.visitInsn(Opcodes.ARETURN);
            legacyRoll.visitMaxs(0, 0);
            legacyRoll.visitEnd();
        }

        MethodVisitor translation = writer.visitMethod(Opcodes.ACC_PUBLIC, "getTranslation",
                Type.getMethodDescriptor(Type.getType(Component.class)), null, null);
        translation.visitCode();
        translation.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Component.class), "empty",
                Type.getMethodDescriptor(Type.getType(Component.class)), Component.class.isInterface());
        translation.visitInsn(Opcodes.ARETURN);
        translation.visitMaxs(0, 0);
        translation.visitEnd();

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static final class FixtureClassLoader extends ClassLoader {

        private FixtureClassLoader() {
            super(ChanceLogicCompatibilityGameTest.class.getClassLoader());
        }

        private Class<? extends ChanceLogic> define(String binaryName, byte[] bytes) {
            return defineClass(binaryName, bytes, 0, bytes.length).asSubclass(ChanceLogic.class);
        }
    }
}
