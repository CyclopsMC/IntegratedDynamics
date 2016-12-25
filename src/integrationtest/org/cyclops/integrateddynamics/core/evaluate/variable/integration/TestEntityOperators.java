package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.http.util.Asserts;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.test.IntegrationBefore;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;
import org.cyclops.integrateddynamics.core.test.TestHelpers;

/**
 * Test the different logical operators.
 * @author rubensworks
 */
public class TestEntityOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableEntity eZombie;
    private DummyVariableEntity eZombieBurning;
    private DummyVariableEntity eZombieWet;
    private DummyVariableEntity eZombieSneaking;
    private DummyVariableEntity eZombieEating;
    private DummyVariableEntity eChicken;
    private DummyVariableEntity eItem;
    private DummyVariableEntity eItemFrame;
    private DummyVariableEntity ePlayer;

    @IntegrationBefore
    public void before() {
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
        eZombie = new DummyVariableEntity(ValueObjectTypeEntity.ValueEntity.of(new EntityZombie(world)));
        EntityZombie zombieBurning = new EntityZombie(world);
        zombieBurning.setFire(10);
        eZombieBurning = new DummyVariableEntity(ValueObjectTypeEntity.ValueEntity.of(zombieBurning));
        EntityZombie zombieWet = new EntityZombie(world) {
            @Override
            protected void entityInit() {
                super.entityInit();
                this.inWater = true;
            }
        };
        eZombieWet = new DummyVariableEntity(ValueObjectTypeEntity.ValueEntity.of(zombieWet));
        EntityZombie zombieSneaking = new EntityZombie(world);
        zombieSneaking.setSneaking(true);
        eZombieSneaking = new DummyVariableEntity(ValueObjectTypeEntity.ValueEntity.of(zombieSneaking));
        EntityZombie zombieEating = new EntityZombie(world) {
            @Override
            public int getItemInUseCount() {
                return 1;
            }
        };
        eZombieEating = new DummyVariableEntity(ValueObjectTypeEntity.ValueEntity.of(zombieEating));
        eChicken = new DummyVariableEntity(ValueObjectTypeEntity.ValueEntity.of(new EntityChicken(world)));
        eItem = new DummyVariableEntity(ValueObjectTypeEntity.ValueEntity.of(new EntityItem(world)));
        eItemFrame = new DummyVariableEntity(ValueObjectTypeEntity.ValueEntity.of(new EntityItemFrame(world)));
        ePlayer = new DummyVariableEntity(ValueObjectTypeEntity.ValueEntity.of(world.playerEntities.get(0)));
    }

    /**
     * ----------------------------------- ISMOB -----------------------------------
     */

    @IntegrationTest
    public void testBlockIsMob() throws EvaluationException {
        IValue res1 = Operators.OBJECT_ENTITY_ISMOB.evaluate(new IVariable[]{eZombie});
        Asserts.check(res1 instanceof ValueTypeBoolean.ValueBoolean, "result is a boolean");
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), true, "isismob(zombie) = true");

        IValue res2 = Operators.OBJECT_ENTITY_ISMOB.evaluate(new IVariable[]{eChicken});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), false, "isismob(chicken) = false");

        IValue res3 = Operators.OBJECT_ENTITY_ISMOB.evaluate(new IVariable[]{eItem});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), false, "isismob(item) = false");

        IValue res4 = Operators.OBJECT_ENTITY_ISMOB.evaluate(new IVariable[]{eItemFrame});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), false, "isismob(itemframe) = false");

        IValue res5 = Operators.OBJECT_ENTITY_ISMOB.evaluate(new IVariable[]{ePlayer});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res5).getRawValue(), false, "isismob(player) = false");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsMobLarge() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISMOB.evaluate(new IVariable[]{eZombie, eZombie});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsMobSmall() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISMOB.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeIsMob() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISMOB.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ISANIMAL -----------------------------------
     */

    @IntegrationTest
    public void testBlockIsAnimal() throws EvaluationException {
        IValue res1 = Operators.OBJECT_ENTITY_ISANIMAL.evaluate(new IVariable[]{eZombie});
        Asserts.check(res1 instanceof ValueTypeBoolean.ValueBoolean, "result is a boolean");
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), false, "isisanimal(zombie) = false");

        IValue res2 = Operators.OBJECT_ENTITY_ISANIMAL.evaluate(new IVariable[]{eChicken});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), true, "isisanimal(chicken) = true");

        IValue res3 = Operators.OBJECT_ENTITY_ISANIMAL.evaluate(new IVariable[]{eItem});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), false, "isisanimal(item) = false");

        IValue res4 = Operators.OBJECT_ENTITY_ISANIMAL.evaluate(new IVariable[]{eItemFrame});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), false, "isisanimal(itemframe) = false");

        IValue res5 = Operators.OBJECT_ENTITY_ISANIMAL.evaluate(new IVariable[]{ePlayer});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res5).getRawValue(), false, "isismob(player) = false");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsAnimalLarge() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISANIMAL.evaluate(new IVariable[]{eZombie, eZombie});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsAnimalSmall() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISANIMAL.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeIsAnimal() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISANIMAL.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ISITEM -----------------------------------
     */

    @IntegrationTest
    public void testBlockIsItem() throws EvaluationException {
        IValue res1 = Operators.OBJECT_ENTITY_ISITEM.evaluate(new IVariable[]{eZombie});
        Asserts.check(res1 instanceof ValueTypeBoolean.ValueBoolean, "result is a boolean");
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), false, "isisitem(zombie) = false");

        IValue res2 = Operators.OBJECT_ENTITY_ISITEM.evaluate(new IVariable[]{eChicken});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), false, "isisitem(chicken) = false");

        IValue res3 = Operators.OBJECT_ENTITY_ISITEM.evaluate(new IVariable[]{eItem});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), true, "isisitem(item) = true");

        IValue res4 = Operators.OBJECT_ENTITY_ISITEM.evaluate(new IVariable[]{eItemFrame});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), false, "isisitem(itemframe) = false");

        IValue res5 = Operators.OBJECT_ENTITY_ISITEM.evaluate(new IVariable[]{ePlayer});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res5).getRawValue(), false, "isismob(player) = false");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsItemLarge() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISITEM.evaluate(new IVariable[]{eZombie, eZombie});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsItemSmall() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISITEM.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeIsItem() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISITEM.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ISPLAYER -----------------------------------
     */

    @IntegrationTest
    public void testBlockIsPlayer() throws EvaluationException {
        IValue res1 = Operators.OBJECT_ENTITY_ISPLAYER.evaluate(new IVariable[]{eZombie});
        Asserts.check(res1 instanceof ValueTypeBoolean.ValueBoolean, "result is a boolean");
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), false, "isisplayer(zombie) = false");

        IValue res2 = Operators.OBJECT_ENTITY_ISPLAYER.evaluate(new IVariable[]{eChicken});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), false, "isisplayer(chicken) = false");

        IValue res3 = Operators.OBJECT_ENTITY_ISPLAYER.evaluate(new IVariable[]{eItem});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res3).getRawValue(), false, "isisplayer(item) = false");

        IValue res4 = Operators.OBJECT_ENTITY_ISPLAYER.evaluate(new IVariable[]{eItemFrame});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res4).getRawValue(), false, "isisplayer(itemframe) = false");

        IValue res5 = Operators.OBJECT_ENTITY_ISPLAYER.evaluate(new IVariable[]{ePlayer});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res5).getRawValue(), true, "isisplayer(player) = true");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsPlayerLarge() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISPLAYER.evaluate(new IVariable[]{eZombie, eZombie});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsPlayerSmall() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISPLAYER.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeIsPlayer() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISPLAYER.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ITEMSTACK -----------------------------------
     */

    @IntegrationTest
    public void testBlockItemStack() throws EvaluationException {
        IValue res1 = Operators.OBJECT_ENTITY_ITEMSTACK.evaluate(new IVariable[]{eZombie});
        Asserts.check(res1 instanceof ValueObjectTypeItemStack.ValueItemStack, "result is an itemstack");
        TestHelpers.assertEqual(!((ValueObjectTypeItemStack.ValueItemStack) res1).getRawValue().isEmpty(), false, "itemstack(zombie) = null");

        IValue res2 = Operators.OBJECT_ENTITY_ITEMSTACK.evaluate(new IVariable[]{eItem});
        TestHelpers.assertEqual(((ValueObjectTypeItemStack.ValueItemStack) res2).getRawValue().isEmpty(), true, "itemstack(null) = null");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeItemStackLarge() throws EvaluationException {
        Operators.OBJECT_ENTITY_ITEMSTACK.evaluate(new IVariable[]{eZombie, eZombie});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeItemStackSmall() throws EvaluationException {
        Operators.OBJECT_ENTITY_ITEMSTACK.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeItemStack() throws EvaluationException {
        Operators.OBJECT_ENTITY_ITEMSTACK.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- HEALTH -----------------------------------
     */

    @IntegrationTest
    public void testBlockHealth() throws EvaluationException {
        IValue res1 = Operators.OBJECT_ENTITY_HEALTH.evaluate(new IVariable[]{eZombie});
        Asserts.check(res1 instanceof ValueTypeDouble.ValueDouble, "result is a double");
        TestHelpers.assertEqual(((ValueTypeDouble.ValueDouble) res1).getRawValue(), 20.0D, "health(zombie) = 10");

        IValue res2 = Operators.OBJECT_ENTITY_HEALTH.evaluate(new IVariable[]{eItem});
        TestHelpers.assertEqual(((ValueTypeDouble.ValueDouble) res2).getRawValue(), 0D, "health(item) = 0");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeHealthLarge() throws EvaluationException {
        Operators.OBJECT_ENTITY_HEALTH.evaluate(new IVariable[]{eZombie, eZombie});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeHealthSmall() throws EvaluationException {
        Operators.OBJECT_ENTITY_HEALTH.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeHealth() throws EvaluationException {
        Operators.OBJECT_ENTITY_HEALTH.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WIDTH -----------------------------------
     */

    @IntegrationTest
    public void testBlockWidth() throws EvaluationException {
        IValue res1 = Operators.OBJECT_ENTITY_WIDTH.evaluate(new IVariable[]{eZombie});
        Asserts.check(res1 instanceof ValueTypeDouble.ValueDouble, "result is a double");
        TestHelpers.assertEqual(((ValueTypeDouble.ValueDouble) res1).getRawValue(), 0.6D, "width(zombie) = 0.6");

        IValue res2 = Operators.OBJECT_ENTITY_WIDTH.evaluate(new IVariable[]{eItem});
        TestHelpers.assertEqual(((ValueTypeDouble.ValueDouble) res2).getRawValue(), 0.25D, "width(item) = 0.25");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeWidthLarge() throws EvaluationException {
        Operators.OBJECT_ENTITY_WIDTH.evaluate(new IVariable[]{eZombie, eZombie});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeWidthSmall() throws EvaluationException {
        Operators.OBJECT_ENTITY_WIDTH.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeWidth() throws EvaluationException {
        Operators.OBJECT_ENTITY_WIDTH.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- HEIGHT -----------------------------------
     */

    @IntegrationTest
    public void testBlockHeight() throws EvaluationException {
        IValue res1 = Operators.OBJECT_ENTITY_HEIGHT.evaluate(new IVariable[]{eZombie});
        Asserts.check(res1 instanceof ValueTypeDouble.ValueDouble, "result is a double");
        TestHelpers.assertEqual(((ValueTypeDouble.ValueDouble) res1).getRawValue(), 2D, "height(zombie) = 2");

        IValue res2 = Operators.OBJECT_ENTITY_HEIGHT.evaluate(new IVariable[]{eItem});
        TestHelpers.assertEqual(((ValueTypeDouble.ValueDouble) res2).getRawValue(), 0.25D, "height(item) = 0.25");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeHeightLarge() throws EvaluationException {
        Operators.OBJECT_ENTITY_HEIGHT.evaluate(new IVariable[]{eZombie, eZombie});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeHeightSmall() throws EvaluationException {
        Operators.OBJECT_ENTITY_HEIGHT.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeHeight() throws EvaluationException {
        Operators.OBJECT_ENTITY_HEIGHT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ISBURNING -----------------------------------
     */

    @IntegrationTest
    public void testBlockIsBurning() throws EvaluationException {
        IValue res1 = Operators.OBJECT_ENTITY_ISBURNING.evaluate(new IVariable[]{eZombie});
        Asserts.check(res1 instanceof ValueTypeBoolean.ValueBoolean, "result is a boolean");
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), false, "isburning(zombie) = false");

        IValue res2 = Operators.OBJECT_ENTITY_ISBURNING.evaluate(new IVariable[]{eZombieBurning});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), true, "isburning(zombie:burning) = true");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsBurningLarge() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISBURNING.evaluate(new IVariable[]{eZombie, eZombie});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsBurningSmall() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISBURNING.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeIsBurning() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISBURNING.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ISWET -----------------------------------
     */

    @IntegrationTest
    public void testBlockIsWet() throws EvaluationException {
        IValue res1 = Operators.OBJECT_ENTITY_ISWET.evaluate(new IVariable[]{eZombie});
        Asserts.check(res1 instanceof ValueTypeBoolean.ValueBoolean, "result is a boolean");
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), false, "iswet(zombie) = false");

        IValue res2 = Operators.OBJECT_ENTITY_ISWET.evaluate(new IVariable[]{eZombieWet});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), true, "iswet(zombie:wet) = true");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsWetLarge() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISWET.evaluate(new IVariable[]{eZombie, eZombie});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsWetSmall() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISWET.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeIsWet() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISWET.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ISSNEAKING -----------------------------------
     */

    @IntegrationTest
    public void testBlockIsSneaking() throws EvaluationException {
        IValue res1 = Operators.OBJECT_ENTITY_ISSNEAKING.evaluate(new IVariable[]{eZombie});
        Asserts.check(res1 instanceof ValueTypeBoolean.ValueBoolean, "result is a boolean");
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), false, "issneaking(zombie) = false");

        IValue res2 = Operators.OBJECT_ENTITY_ISSNEAKING.evaluate(new IVariable[]{eZombieSneaking});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), true, "issneaking(zombie:sneaking) = true");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsSneakingLarge() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISSNEAKING.evaluate(new IVariable[]{eZombie, eZombie});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsSneakingSmall() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISSNEAKING.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeIsSneaking() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISSNEAKING.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ISEATING -----------------------------------
     */

    @IntegrationTest
    public void testBlockIsEating() throws EvaluationException {
        IValue res1 = Operators.OBJECT_ENTITY_ISEATING.evaluate(new IVariable[]{eZombie});
        Asserts.check(res1 instanceof ValueTypeBoolean.ValueBoolean, "result is a boolean");
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res1).getRawValue(), false, "iseating(zombie) = false");

        IValue res2 = Operators.OBJECT_ENTITY_ISEATING.evaluate(new IVariable[]{eZombieEating});
        TestHelpers.assertEqual(((ValueTypeBoolean.ValueBoolean) res2).getRawValue(), true, "iseating(zombie:eating) = true");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsEatingLarge() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISEATING.evaluate(new IVariable[]{eZombie, eZombie});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeIsEatingSmall() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISEATING.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeIsEating() throws EvaluationException {
        Operators.OBJECT_ENTITY_ISEATING.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- MODNAME -----------------------------------
     */

    @IntegrationTest
    public void testEntityModName() throws EvaluationException {
        IValue res1 = Operators.OBJECT_ENTITY_MODNAME.evaluate(new IVariable[]{eZombie});
        Asserts.check(res1 instanceof ValueTypeString.ValueString, "result is a string");
        TestHelpers.assertEqual(((ValueTypeString.ValueString) res1).getRawValue(), "Minecraft", "modname(zombie) = Minecraft");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeModNameLarge() throws EvaluationException {
        Operators.OBJECT_ENTITY_MODNAME.evaluate(new IVariable[]{eZombie, eZombie});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputSizeModNameSmall() throws EvaluationException {
        Operators.OBJECT_ENTITY_MODNAME.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInvalidInputTypeModName() throws EvaluationException {
        Operators.OBJECT_ENTITY_MODNAME.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

}
