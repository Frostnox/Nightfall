package frostnox.nightfall.registry.forge;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.entity.entity.ambient.JellyfishEntity;
import frostnox.nightfall.entity.entity.animal.DeerEntity;
import frostnox.nightfall.entity.entity.animal.DrakefowlEntity;
import frostnox.nightfall.entity.entity.animal.MerborEntity;
import frostnox.nightfall.entity.entity.animal.RabbitEntity;
import frostnox.nightfall.entity.entity.monster.CockatriceEntity;
import frostnox.nightfall.entity.entity.monster.SpiderEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class DataSerializersNF {
    public static final DeferredRegister<DataSerializerEntry> SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.DATA_SERIALIZERS, Nightfall.MODID);
    public static final EntityDataSerializer<Sex> SEX = new EntityDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf pBuffer, Sex pValue) {
            pBuffer.writeEnum(pValue);
        }

        @Override
        public Sex read(FriendlyByteBuf pBuffer) {
            return pBuffer.readEnum(Sex.class);
        }

        @Override
        public Sex copy(Sex pValue) {
            return pValue;
        }
    };
    public static final EntityDataSerializer<RabbitEntity.Type> RABBIT_TYPE = new EntityDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf pBuffer, RabbitEntity.Type pValue) {
            pBuffer.writeEnum(pValue);
        }

        @Override
        public RabbitEntity.Type read(FriendlyByteBuf pBuffer) {
            return pBuffer.readEnum(RabbitEntity.Type.class);
        }

        @Override
        public RabbitEntity.Type copy(RabbitEntity.Type pValue) {
            return pValue;
        }
    };
    public static final EntityDataSerializer<DeerEntity.Type> DEER_TYPE = new EntityDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf pBuffer, DeerEntity.Type pValue) {
            pBuffer.writeEnum(pValue);
        }

        @Override
        public DeerEntity.Type read(FriendlyByteBuf pBuffer) {
            return pBuffer.readEnum(DeerEntity.Type.class);
        }

        @Override
        public DeerEntity.Type copy(DeerEntity.Type pValue) {
            return pValue;
        }
    };
    public static final EntityDataSerializer<DrakefowlEntity.Type> DRAKEFOWL_TYPE = new EntityDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf pBuffer, DrakefowlEntity.Type pValue) {
            pBuffer.writeEnum(pValue);
        }

        @Override
        public DrakefowlEntity.Type read(FriendlyByteBuf pBuffer) {
            return pBuffer.readEnum(DrakefowlEntity.Type.class);
        }

        @Override
        public DrakefowlEntity.Type copy(DrakefowlEntity.Type pValue) {
            return pValue;
        }
    };
    public static final EntityDataSerializer<MerborEntity.Type> MERBOR_TYPE = new EntityDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf pBuffer, MerborEntity.Type pValue) {
            pBuffer.writeEnum(pValue);
        }

        @Override
        public MerborEntity.Type read(FriendlyByteBuf pBuffer) {
            return pBuffer.readEnum(MerborEntity.Type.class);
        }

        @Override
        public MerborEntity.Type copy(MerborEntity.Type pValue) {
            return pValue;
        }
    };
    public static final EntityDataSerializer<CockatriceEntity.Type> COCKATRICE_TYPE = new EntityDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf pBuffer, CockatriceEntity.Type pValue) {
            pBuffer.writeEnum(pValue);
        }

        @Override
        public CockatriceEntity.Type read(FriendlyByteBuf pBuffer) {
            return pBuffer.readEnum(CockatriceEntity.Type.class);
        }

        @Override
        public CockatriceEntity.Type copy(CockatriceEntity.Type pValue) {
            return pValue;
        }
    };
    public static final EntityDataSerializer<SpiderEntity.Type> SPIDER_TYPE = new EntityDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf pBuffer, SpiderEntity.Type pValue) {
            pBuffer.writeEnum(pValue);
        }

        @Override
        public SpiderEntity.Type read(FriendlyByteBuf pBuffer) {
            return pBuffer.readEnum(SpiderEntity.Type.class);
        }

        @Override
        public SpiderEntity.Type copy(SpiderEntity.Type pValue) {
            return pValue;
        }
    };
    public static final EntityDataSerializer<JellyfishEntity.Type> JELLYFISH_TYPE = new EntityDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf pBuffer, JellyfishEntity.Type pValue) {
            pBuffer.writeEnum(pValue);
        }

        @Override
        public JellyfishEntity.Type read(FriendlyByteBuf pBuffer) {
            return pBuffer.readEnum(JellyfishEntity.Type.class);
        }

        @Override
        public JellyfishEntity.Type copy(JellyfishEntity.Type pValue) {
            return pValue;
        }
    };

    static {
        SERIALIZERS.register("sex", () -> new DataSerializerEntry(SEX));
        SERIALIZERS.register("rabbit_type", () -> new DataSerializerEntry(RABBIT_TYPE));
        SERIALIZERS.register("deer_type", () -> new DataSerializerEntry(DEER_TYPE));
        SERIALIZERS.register("drakefowl_type", () -> new DataSerializerEntry(DRAKEFOWL_TYPE));
        SERIALIZERS.register("merbor_type", () -> new DataSerializerEntry(MERBOR_TYPE));
        SERIALIZERS.register("cockatrice_type", () -> new DataSerializerEntry(COCKATRICE_TYPE));
        SERIALIZERS.register("spider_type", () -> new DataSerializerEntry(SPIDER_TYPE));
        SERIALIZERS.register("jellyfish_type", () -> new DataSerializerEntry(JELLYFISH_TYPE));
    }

    public static void register() {
        SERIALIZERS.register(Nightfall.MOD_EVENT_BUS);
    }
}
