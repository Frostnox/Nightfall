package frostnox.nightfall.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;

public abstract class ConstantCollidingParticle extends TextureSheetParticle {
    protected ConstantCollidingParticle(ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if(this.age++ >= this.lifetime) this.remove();
        else {
            if(!this.onGround) {
                this.yd -= 0.04D * gravity;
                this.move(this.xd, this.yd, this.zd);
            }
            else {
                if(Minecraft.useFancyGraphics()) {
                    this.yd = -0.04D * gravity;
                    stoppedByCollision = false;
                    move(0, yd, 0);
                }
            }
            if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
                this.xd *= 1.1D;
                this.zd *= 1.1D;
            }
            this.xd *= (double)this.friction;
            this.yd *= (double)this.friction;
            this.zd *= (double)this.friction;
            if (this.onGround) {
                this.xd *= (double)0.7F;
                this.zd *= (double)0.7F;
            }
        }
    }
}
