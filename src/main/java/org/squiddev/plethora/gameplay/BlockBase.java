package org.squiddev.plethora.gameplay;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Base class for all blocks
 */
public abstract class BlockBase<T extends TileBase> extends BlockContainer {
	public final String name;
	public final Class<T> klass;

	public BlockBase(String blockName, Material material, Class<T> klass) {
		super(material);

		this.klass = klass;
		name = blockName;
		setRegistryName(new ResourceLocation(Plethora.RESOURCE_DOMAIN, blockName));

		setHardness(2);
		setTranslationKey(Plethora.RESOURCE_DOMAIN + "." + blockName);
		setCreativeTab(Plethora.getCreativeTab());
	}

	public BlockBase(String name, Class<T> klass) {
		this(name, Material.ROCK, klass);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public T getTile(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		return klass.isInstance(tile) ? (T) tile : null;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	@Nonnull
	@Override
	@Deprecated
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, Direction side, float hitX, float hitY, float hitZ) {
		TileBase tile = getTile(world, pos);
		return tile != null && tile.onActivated(player, hand, side, new Vec3d(hitX, hitY, hitZ));
	}

	@Override
	@Deprecated
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, neighborBlock, fromPos);

		if (world.isRemote) return;

		TileBase tile = getTile(world, pos);
		if (tile != null) tile.onNeighborChanged();
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(world, pos, neighbor);

		if (world instanceof World && ((World) world).isRemote) return;

		TileBase tile = getTile(world, pos);
		if (tile != null) tile.onNeighborChanged();
	}

	@Override
	public void breakBlock(World world, @Nonnull BlockPos block, @Nonnull IBlockState state) {
		if (!world.isRemote) {
			T tile = getTile(world, block);
			if (tile != null) tile.broken();
		}

		super.breakBlock(world, block, state);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> out, ITooltipFlag advanced) {
		super.addInformation(stack, world, out, advanced);
		out.add(Helpers.translateToLocal(getTranslationKey(stack.getItemDamage()) + ".desc"));
	}

	public String getTranslationKey(int meta) {
		return getTranslationKey();
	}
}
