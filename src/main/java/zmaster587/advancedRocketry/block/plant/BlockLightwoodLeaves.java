package zmaster587.advancedRocketry.block.plant;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zmaster587.advancedRocketry.api.AdvancedRocketryBlocks;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

public class BlockLightwoodLeaves extends BlockLeaves {

    protected static final String[] names = {"blueLeaf"};
    protected static final String[][] textures = new String[][]{{"leaves_oak"}, {"leaves_oak_opaque"}};

    public BlockLightwoodLeaves() {
        super();
        this.lightValue = 8;
        this.setDefaultState(this.getDefaultState().withProperty(DECAYABLE, true).withProperty(CHECK_DECAY, false));
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DECAYABLE, CHECK_DECAY);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(DECAYABLE) ? 1 : 0) + (state.getValue(CHECK_DECAY) ? 2 : 0);
    }

    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(DECAYABLE, (meta & 1) == 1).withProperty(CHECK_DECAY, (meta & 2) == 2);
    }

    public int quantityDropped(Random p_149745_1_) {
        return p_149745_1_.nextInt(100) == 0 ? 1 : 0;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 50;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos,
                                  EnumFacing face) {
        return 50;
    }

    @Nonnull
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(AdvancedRocketryBlocks.blockLightwoodSapling);
    }


    @Override
    @Nonnull
    public NonNullList<ItemStack> onSheared(@Nonnull ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return NonNullList.withSize(1, new ItemStack(this, 1, 0));
    }

    @Override
    @Nonnull
    public EnumType getWoodType(int meta) {
        return EnumType.OAK;
    }

    //These three methods need to be overridden
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return Blocks.LEAVES.isOpaqueCube(state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @ParametersAreNonnullByDefault
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return Blocks.LEAVES.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }
}
