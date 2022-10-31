package com.telepathicgrunt.the_bumblezone.client.bakedmodel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.telepathicgrunt.the_bumblezone.Bumblezone;
import com.telepathicgrunt.the_bumblezone.modinit.BzBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelDataManager;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PorousHoneycombBlockModel implements IDynamicBakedModel {
    public static final ModelProperty<List<Direction>> DIRECTION_OF_HONEY_MERGERS = new ModelProperty<>();
    protected final BakedModel mainModel;
    private final Map<List<Direction>, BakedModel> cache = Maps.newHashMap();

    private PorousHoneycombBlockModel(BakedModel mainModel) {
        this.mainModel = mainModel;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
        List<BakedQuad> quads = Lists.newArrayList();
        quads.addAll(mainModel.getQuads(state, side, rand, extraData, renderType));
        if (state != null && side != null && extraData.has(DIRECTION_OF_HONEY_MERGERS) && extraData.get(DIRECTION_OF_HONEY_MERGERS) != null) {
            List<Direction> directionsOfHoney = extraData.get(DIRECTION_OF_HONEY_MERGERS);

            // For sides for face to connect texture to
            for (Direction honeyDirection : directionsOfHoney) {
                if (side == honeyDirection || side.getOpposite() == honeyDirection) {
                    continue;
                }

                TextureAtlasSprite textureAtlasSprite = Minecraft.getInstance().getModelManager()
                        .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                        .getSprite(new ResourceLocation(Bumblezone.MODID, "textures/block/porous_honeycomb_block/porous_honeycomb_block_1d.png"));

                // draw connecting texture on top of previous quad?
                BakedQuad newBakedQuad = new BakedQuad(
                        quads.get(0).getVertices(),
                        0,
                        side,
                        textureAtlasSprite,
                        true
                    );

                quads.set(0, newBakedQuad);
            }
        }

        return quads;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        ModelData currentData = modelData;
        if (state != null) {
            if (!currentData.has(DIRECTION_OF_HONEY_MERGERS) || currentData.get(DIRECTION_OF_HONEY_MERGERS) == null) {
                currentData = ModelData.builder()
                        .with(DIRECTION_OF_HONEY_MERGERS, new ArrayList<>())
                        .build();
            }

            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            currentData.get(DIRECTION_OF_HONEY_MERGERS).clear();

            for (Direction direction : Direction.values()) {
                mutableBlockPos.set(pos).move(direction);
                BlockState neighborState = level.getBlockState(mutableBlockPos);
                if (neighborState.is(BzBlocks.FILLED_POROUS_HONEYCOMB.get()) || neighborState.is(BzBlocks.HONEYCOMB_BROOD.get())) {
                    currentData.get(DIRECTION_OF_HONEY_MERGERS).add(direction);
                }
            }
        }

        return currentData;
    }

    @Override
    public boolean usesBlockLight() {
        return mainModel.usesBlockLight();
    }

    @Override
    public boolean useAmbientOcclusion () {
        return mainModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d () {
        return mainModel.isGui3d();
    }

    @Override
    public boolean isCustomRenderer () {
        return mainModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon () {
        return mainModel.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides () {
        return mainModel.getOverrides();
    }

    public static void onModelBake(ModelEvent.BakingCompleted event) {
        override(event, BzBlocks.POROUS_HONEYCOMB.get(), PorousHoneycombBlockModel::new);
    }

    private static void override(ModelEvent.BakingCompleted event, Block block, Function<BakedModel, PorousHoneycombBlockModel> f) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            ModelResourceLocation loc = BlockModelShaper.stateToModelLocation(state);
            BakedModel model = event.getModels().get(loc);
            if (model != null) {
                event.getModels().put(loc, f.apply(model));
            }
        }
    }
}