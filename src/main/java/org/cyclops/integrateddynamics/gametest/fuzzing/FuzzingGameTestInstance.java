package org.cyclops.integrateddynamics.gametest.fuzzing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.gametest.GameTestsFuzzing;

/**
 * @author rubensworks
 */
public class FuzzingGameTestInstance extends GameTestInstance {

    public static final MapCodec<? extends GameTestInstance> CODEC = RecordCodecBuilder.<FuzzingGameTestInstance>mapCodec(instance -> instance.group(
            TestData.CODEC.forGetter(FuzzingGameTestInstance::info),
            Codec.INT.fieldOf("iteration").forGetter(FuzzingGameTestInstance::getIteration)
    ).apply(instance, FuzzingGameTestInstance::new));

    private final int iteration;

    public FuzzingGameTestInstance(TestData<Holder<TestEnvironmentDefinition<?>>> info, int iteration) {
        super(info);
        this.iteration = iteration;
    }

    public int getIteration() {
        return iteration;
    }

    @Override
    public void run(GameTestHelper gameTestHelper) {
        GameTestsFuzzing.runFuzzingIteration(gameTestHelper, this.iteration);
    }

    @Override
    public MapCodec<? extends GameTestInstance> codec() {
        return CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.literal("Fuzzing test iteration " + this.iteration);
    }

    public Identifier getId() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "fuzzing_" + this.iteration);
    }
}
