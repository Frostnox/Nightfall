package frostnox.nightfall.client.particle;

import frostnox.nightfall.block.block.tree.TreeLeavesBlock;
import frostnox.nightfall.client.ClientEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nullable;

public class LeafParticle extends TextureSheetParticle {
    private final BlockPos pos;
    private final float rotSpeed;

    protected LeafParticle(ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprite, BlockState state, boolean color) {
        super(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
        this.pos = new BlockPos(x, y, z);
        this.rotSpeed = ((float)Math.random() * 0.3F + 0.7F) * 0.02F;
        this.roll = (float)Math.random() * ((float)Math.PI * 2F);
        this.pickSprite(sprite);
        this.quadSize *= 0.85F + this.random.nextFloat(0.3F);
        this.gravity = 0.05F;
        this.lifetime = 400;
        this.xd *= 0.5D;
        this.yd = 0D;
        this.zd *= 0.5D;
        if(color) {
            int i = Minecraft.getInstance().getBlockColors().getColor(state, worldIn, pos, 0);
            this.rCol *= (float) (i >> 16 & 255) / 255.0F;
            this.gCol *= (float) (i >> 8 & 255) / 255.0F;
            this.bCol *= (float) (i & 255) / 255.0F;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float pPartialTick) {
        int i = super.getLightColor(pPartialTick);
        return i == 0 && this.level.hasChunkAt(this.pos) ? LevelRenderer.getLightColor(this.level, this.pos) : i;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if(this.age++ >= this.lifetime) this.remove();
        else {
            FluidState fluid = level.getBlockState(new BlockPos(x, y, z)).getFluidState();
            if(!fluid.isEmpty()) {
                if(fluid.is(FluidTags.LAVA)) remove();
                else {
                    this.oRoll = this.roll;
                    move(0, Mth.sin(age * 0.08F) * 0.0013, 0);
                    xd = 0;
                    yd = 0;
                    zd = 0;
                }
            }
            else if(!this.onGround) {
                this.oRoll = this.roll;
                this.roll += (float)Math.PI * this.rotSpeed * 2.0F;
                this.move(this.xd, this.yd, this.zd);
            }
            else {
                this.oRoll = roll;
                if(Minecraft.useFancyGraphics()) {
                    stoppedByCollision = false;
                    move(0, yd, 0);
                }
            }

            this.yd -= 0.003D;
            this.yd = Math.max(this.yd, -0.14D);
        }
        int fadeTime = this.lifetime * 4 / 5;
        if(this.age >= fadeTime) {
            this.setAlpha(Mth.clamp(1F - ((float) this.age + ClientEngine.get().getPartialTick() - fadeTime) / (this.lifetime - fadeTime), 0F, 1F));
        }
    }

    public static class Provider implements ParticleProvider<BlockParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(BlockParticleOption type, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            BlockState state = type.getState();
            return state.getBlock() instanceof TreeLeavesBlock leavesBlock ? new LeafParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite, state, leavesBlock.type.isDeciduous()) : null;
        }
    }
}
