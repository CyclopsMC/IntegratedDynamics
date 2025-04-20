package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeEntity;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Objects;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.testReadAspect;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.testReadAspectPredicate;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsAspectsReadEntity {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadEntityItemFrameRotationInvalid(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.ENTITY_READER, Aspects.Read.Entity.INTEGER_ITEMFRAMEROTATION, ValueTypeInteger.ValueInteger.of(0));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadEntityItemFrameRotationValid(GameTestHelper helper) {
        ItemFrame itemFrame = helper.spawn(EntityType.ITEM_FRAME, POS.west());
        itemFrame.setRotation(3);
        itemFrame.setYRot(Direction.WEST.toYRot());
        testReadAspect(POS, helper, PartTypes.ENTITY_READER, Aspects.Read.Entity.INTEGER_ITEMFRAMEROTATION, ValueTypeInteger.ValueInteger.of(3));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadEntityItemFrameContentsInvalid(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.ENTITY_READER, Aspects.Read.Entity.ITEMSTACK_ITEMFRAMECONTENTS, ValueObjectTypeItemStack.ValueItemStack.of(ItemStack.EMPTY));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadEntityItemFrameContentsValid(GameTestHelper helper) {
        ItemFrame itemFrame = helper.spawn(EntityType.ITEM_FRAME, POS.west());
        itemFrame.setItem(new ItemStack(Items.COAL));
        itemFrame.setYRot(Direction.WEST.toYRot());
        testReadAspect(POS, helper, PartTypes.ENTITY_READER, Aspects.Read.Entity.ITEMSTACK_ITEMFRAMECONTENTS, ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.COAL)));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadEntityEntities(GameTestHelper helper) {
        Cow cow = helper.spawn(EntityType.COW, POS.west());
        Chicken chicken = helper.spawn(EntityType.CHICKEN, POS.west());
        testReadAspect(POS, helper, PartTypes.ENTITY_READER, Aspects.Read.Entity.LIST_ENTITIES, ValueTypeList.ValueList.ofAll(
                ValueObjectTypeEntity.ValueEntity.of(cow),
                ValueObjectTypeEntity.ValueEntity.of(chicken)
        ));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadEntityEntity(GameTestHelper helper) {
        Cow cow = helper.spawn(EntityType.COW, POS.west());
        testReadAspect(POS, helper, PartTypes.ENTITY_READER, Aspects.Read.Entity.ENTITY, ValueObjectTypeEntity.ValueEntity.of(cow));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadEntityPlayers(GameTestHelper helper) {
        testReadAspectPredicate(POS, helper, PartTypes.ENTITY_READER, Aspects.Read.Entity.LIST_PLAYERS, Objects::nonNull);
    }

}
