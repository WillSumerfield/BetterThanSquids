package net.minecraft.world.entity;

import com.google.common.collect.ImmutableSet;
import com.inkar.betterthansquids.entity.MinecartLamp;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ModEntityType<T extends Entity> extends net.minecraftforge.registries.ForgeRegistryEntry<ModEntityType<?>> implements EntityTypeTest<Entity, T> {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String ENTITY_TAG = "EntityTag";
    private static final float MAGIC_HORSE_WIDTH = 1.3964844F;
public static final ModEntityType<MinecartLamp> LAMP_MINECART = register("lamp_minecart", ModEntityType.Builder.<MinecartTNT>of(MinecartLamp::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8));
private final ModEntityType.EntityFactory<T> factory;
    private final MobCategory category;
    private final ImmutableSet<Block> immuneTo;
    private final boolean serialize;
    private final boolean summon;
    private final boolean fireImmune;
    private final boolean canSpawnFarFromPlayer;
    private final int clientTrackingRange;
    private final int updateInterval;
    @Nullable
    private String descriptionId;
    @Nullable
    private Component description;
    @Nullable
    private ResourceLocation lootTable;
    private final EntityDimensions dimensions;

    private final java.util.function.Predicate<ModEntityType<?>> velocityUpdateSupplier;
    private final java.util.function.ToIntFunction<ModEntityType<?>> trackingRangeSupplier;
    private final java.util.function.ToIntFunction<ModEntityType<?>> updateIntervalSupplier;
    private final java.util.function.BiFunction<net.minecraftforge.fmllegacy.network.FMLPlayMessages.SpawnEntity, Level, T> customClientFactory;
    private final net.minecraftforge.common.util.ReverseTagWrapper<ModEntityType<?>> reverseTags = new net.minecraftforge.common.util.ReverseTagWrapper<>(this, net.minecraft.tags.EntityTypeTags::getAllTags);

    private static <T extends Entity> ModEntityType<T> register(String pKey, ModEntityType.Builder<T> pBuilder) {
        return Registry.register(Registry.ENTITY_TYPE, pKey, pBuilder.build(pKey));
    }

    public static ResourceLocation getKey(ModEntityType<?> pEntityType) {
        return Registry.ENTITY_TYPE.getKey(pEntityType);
    }

    /**
     * Tries to get the entity type assosiated by the key.
     */
    public static Optional<ModEntityType<?>> byString(String pKey) {
        return Registry.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(pKey));
    }

    public ModEntityType(ModEntityType.EntityFactory<T> p_20574_, MobCategory p_20575_, boolean p_20576_, boolean p_20577_, boolean p_20578_, boolean p_20579_, ImmutableSet<Block> p_20580_, EntityDimensions p_20581_, int p_20582_, int p_20583_) {
        this(p_20574_, p_20575_, p_20576_, p_20577_, p_20578_, p_20579_, p_20580_, p_20581_, p_20582_, p_20583_, ModEntityType::defaultVelocitySupplier, ModEntityType::defaultTrackingRangeSupplier, ModEntityType::defaultUpdateIntervalSupplier, null);
    }
    public ModEntityType(ModEntityType.EntityFactory<T> p_20574_, MobCategory p_20575_, boolean p_20576_, boolean p_20577_, boolean p_20578_, boolean p_20579_, ImmutableSet<Block> p_20580_, EntityDimensions p_20581_, int p_20582_, int p_20583_, final java.util.function.Predicate<ModEntityType<?>> velocityUpdateSupplier, final java.util.function.ToIntFunction<ModEntityType<?>> trackingRangeSupplier, final java.util.function.ToIntFunction<ModEntityType<?>> updateIntervalSupplier, final java.util.function.BiFunction<net.minecraftforge.fmllegacy.network.FMLPlayMessages.SpawnEntity, Level, T> customClientFactory) {
        this.factory = p_20574_;
        this.category = p_20575_;
        this.canSpawnFarFromPlayer = p_20579_;
        this.serialize = p_20576_;
        this.summon = p_20577_;
        this.fireImmune = p_20578_;
        this.immuneTo = p_20580_;
        this.dimensions = p_20581_;
        this.clientTrackingRange = p_20582_;
        this.updateInterval = p_20583_;
        this.velocityUpdateSupplier = velocityUpdateSupplier;
        this.trackingRangeSupplier = trackingRangeSupplier;
        this.updateIntervalSupplier = updateIntervalSupplier;
        this.customClientFactory = customClientFactory;
    }

    @Nullable
    public Entity spawn(ServerLevel p_20593_, @Nullable ItemStack p_20594_, @Nullable Player p_20595_, BlockPos p_20596_, MobSpawnType p_20597_, boolean p_20598_, boolean p_20599_) {
        return this.spawn(p_20593_, p_20594_ == null ? null : p_20594_.getTag(), p_20594_ != null && p_20594_.hasCustomHoverName() ? p_20594_.getHoverName() : null, p_20595_, p_20596_, p_20597_, p_20598_, p_20599_);
    }

    @Nullable
    public T spawn(ServerLevel p_20601_, @Nullable CompoundTag p_20602_, @Nullable Component p_20603_, @Nullable Player p_20604_, BlockPos p_20605_, MobSpawnType p_20606_, boolean p_20607_, boolean p_20608_) {
        T t = this.create(p_20601_, p_20602_, p_20603_, p_20604_, p_20605_, p_20606_, p_20607_, p_20608_);
        if (t != null) {
            if (t instanceof net.minecraft.world.entity.Mob && net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn((net.minecraft.world.entity.Mob) t, p_20601_, p_20605_.getX(), p_20605_.getY(), p_20605_.getZ(), null, p_20606_)) return null;
            p_20601_.addFreshEntityWithPassengers(t);
        }

        return t;
    }

    @Nullable
    public T create(ServerLevel p_20656_, @Nullable CompoundTag p_20657_, @Nullable Component p_20658_, @Nullable Player p_20659_, BlockPos p_20660_, MobSpawnType p_20661_, boolean p_20662_, boolean p_20663_) {
        T t = this.create(p_20656_);
        if (t == null) {
            return (T)null;
        } else {
            double d0;
            if (p_20662_) {
                t.setPos((double)p_20660_.getX() + 0.5D, (double)(p_20660_.getY() + 1), (double)p_20660_.getZ() + 0.5D);
                d0 = getYOffset(p_20656_, p_20660_, p_20663_, t.getBoundingBox());
            } else {
                d0 = 0.0D;
            }

            t.moveTo((double)p_20660_.getX() + 0.5D, (double)p_20660_.getY() + d0, (double)p_20660_.getZ() + 0.5D, Mth.wrapDegrees(p_20656_.random.nextFloat() * 360.0F), 0.0F);
            if (t instanceof Mob) {
                Mob mob = (Mob)t;
                mob.yHeadRot = mob.getYRot();
                mob.yBodyRot = mob.getYRot();
                mob.finalizeSpawn(p_20656_, p_20656_.getCurrentDifficultyAt(mob.blockPosition()), p_20661_, (SpawnGroupData)null, p_20657_);
                mob.playAmbientSound();
            }

            if (p_20658_ != null && t instanceof LivingEntity) {
                t.setCustomName(p_20658_);
            }

            updateCustomEntityTag(p_20656_, p_20659_, t, p_20657_);
            return t;
        }
    }

    protected static double getYOffset(LevelReader p_20626_, BlockPos p_20627_, boolean p_20628_, AABB p_20629_) {
        AABB aabb = new AABB(p_20627_);
        if (p_20628_) {
            aabb = aabb.expandTowards(0.0D, -1.0D, 0.0D);
        }

        Stream<VoxelShape> stream = p_20626_.getCollisions((Entity)null, aabb, (p_20612_) -> {
            return true;
        });
        return 1.0D + Shapes.collide(Direction.Axis.Y, p_20629_, stream, p_20628_ ? -2.0D : -1.0D);
    }

    public static void updateCustomEntityTag(Level pLevel, @Nullable Player pPlayer, @Nullable Entity pSpawnedEntity, @Nullable CompoundTag pItemNBT) {
        if (pItemNBT != null && pItemNBT.contains("EntityTag", 10)) {
            MinecraftServer minecraftserver = pLevel.getServer();
            if (minecraftserver != null && pSpawnedEntity != null) {
                if (pLevel.isClientSide || !pSpawnedEntity.onlyOpCanSetNbt() || pPlayer != null && minecraftserver.getPlayerList().isOp(pPlayer.getGameProfile())) {
                    CompoundTag compoundtag = pSpawnedEntity.saveWithoutId(new CompoundTag());
                    UUID uuid = pSpawnedEntity.getUUID();
                    compoundtag.merge(pItemNBT.getCompound("EntityTag"));
                    pSpawnedEntity.setUUID(uuid);
                    pSpawnedEntity.load(compoundtag);
                }
            }
        }
    }

    public boolean canSerialize() {
        return this.serialize;
    }

    public boolean canSummon() {
        return this.summon;
    }

    public boolean fireImmune() {
        return this.fireImmune;
    }

    public boolean canSpawnFarFromPlayer() {
        return this.canSpawnFarFromPlayer;
    }

    public MobCategory getCategory() {
        return this.category;
    }

    public String getDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("entity", Registry.ENTITY_TYPE.getKey(this));
        }

        return this.descriptionId;
    }

    public Component getDescription() {
        if (this.description == null) {
            this.description = new TranslatableComponent(this.getDescriptionId());
        }

        return this.description;
    }

    public String toString() {
        return this.getDescriptionId();
    }

    public String toShortString() {
        int i = this.getDescriptionId().lastIndexOf(46);
        return i == -1 ? this.getDescriptionId() : this.getDescriptionId().substring(i + 1);
    }

    public ResourceLocation getDefaultLootTable() {
        if (this.lootTable == null) {
            ResourceLocation resourcelocation = Registry.ENTITY_TYPE.getKey(this);
            this.lootTable = new ResourceLocation(resourcelocation.getNamespace(), "entities/" + resourcelocation.getPath());
        }

        return this.lootTable;
    }

    public float getWidth() {
        return this.dimensions.width;
    }

    public float getHeight() {
        return this.dimensions.height;
    }

    @Nullable
    public T create(Level pLevel) {
        return this.factory.create(this, pLevel);
    }

    @Nullable
    public static Entity create(int pId, Level pLevel) {
        return create(pLevel, Registry.ENTITY_TYPE.byId(pId));
    }

    public static Optional<Entity> create(CompoundTag pCompound, Level pLevel) {
        return Util.ifElse(by(pCompound).map((p_20666_) -> {
            return p_20666_.create(pLevel);
        }), (p_20641_) -> {
            p_20641_.load(pCompound);
        }, () -> {
            LOGGER.warn("Skipping Entity with id {}", (Object)pCompound.getString("id"));
        });
    }

    @Nullable
    private static Entity create(Level pLevel, @Nullable ModEntityType<?> pType) {
        return pType == null ? null : pType.create(pLevel);
    }

    public AABB getAABB(double p_20586_, double p_20587_, double p_20588_) {
        float f = this.getWidth() / 2.0F;
        return new AABB(p_20586_ - (double)f, p_20587_, p_20588_ - (double)f, p_20586_ + (double)f, p_20587_ + (double)this.getHeight(), p_20588_ + (double)f);
    }

    public boolean isBlockDangerous(BlockState p_20631_) {
        if (this.immuneTo.contains(p_20631_.getBlock())) {
            return false;
        } else if (!this.fireImmune && WalkNodeEvaluator.isBurningBlock(p_20631_)) {
            return true;
        } else {
            return p_20631_.is(Blocks.WITHER_ROSE) || p_20631_.is(Blocks.SWEET_BERRY_BUSH) || p_20631_.is(Blocks.CACTUS) || p_20631_.is(Blocks.POWDER_SNOW);
        }
    }

    public EntityDimensions getDimensions() {
        return this.dimensions;
    }

    public static Optional<ModEntityType<?>> by(CompoundTag pCompound) {
        return Registry.ENTITY_TYPE.getOptional(new ResourceLocation(pCompound.getString("id")));
    }

    @Nullable
    public static Entity loadEntityRecursive(CompoundTag p_20646_, Level p_20647_, Function<Entity, Entity> p_20648_) {
        return loadStaticEntity(p_20646_, p_20647_).map(p_20648_).map((p_20653_) -> {
            if (p_20646_.contains("Passengers", 9)) {
                ListTag listtag = p_20646_.getList("Passengers", 10);

                for(int i = 0; i < listtag.size(); ++i) {
                    Entity entity = loadEntityRecursive(listtag.getCompound(i), p_20647_, p_20648_);
                    if (entity != null) {
                        entity.startRiding(p_20653_, true);
                    }
                }
            }

            return p_20653_;
        }).orElse((Entity)null);
    }

    public static Stream<Entity> loadEntitiesRecursive(final List<? extends Tag> p_147046_, final Level p_147047_) {
        final Spliterator<? extends Tag> spliterator = p_147046_.spliterator();
        return StreamSupport.stream(new Spliterator<Entity>() {
            public boolean tryAdvance(Consumer<? super Entity> p_147066_) {
                return spliterator.tryAdvance((p_147059_) -> {
                    ModEntityType.loadEntityRecursive((CompoundTag)p_147059_, p_147047_, (p_147062_) -> {
                        p_147066_.accept(p_147062_);
                        return p_147062_;
                    });
                });
            }

            public Spliterator<Entity> trySplit() {
                return null;
            }

            public long estimateSize() {
                return (long)p_147046_.size();
            }

            public int characteristics() {
                return 1297;
            }
        }, false);
    }

    private static Optional<Entity> loadStaticEntity(CompoundTag pCompound, Level pLevel) {
        try {
            return create(pCompound, pLevel);
        } catch (RuntimeException runtimeexception) {
            LOGGER.warn("Exception loading entity: ", (Throwable)runtimeexception);
            return Optional.empty();
        }
    }

    public int clientTrackingRange() {
        return trackingRangeSupplier.applyAsInt(this);
    }
    private int defaultTrackingRangeSupplier() {
        return this.clientTrackingRange;
    }

    public int updateInterval() {
        return updateIntervalSupplier.applyAsInt(this);
    }
    private int defaultUpdateIntervalSupplier() {
        return this.updateInterval;
    }

    public boolean trackDeltas() {
        return velocityUpdateSupplier.test(this);
    }
    private boolean defaultVelocitySupplier() {
        return this != PLAYER && this != LLAMA_SPIT && this != WITHER && this != BAT && this != ITEM_FRAME && this != GLOW_ITEM_FRAME && this != LEASH_KNOT && this != PAINTING && this != END_CRYSTAL && this != EVOKER_FANGS;
    }

    /**
     * Checks if this entity type is contained in the tag
     */
    public boolean is(net.minecraft.tags.Tag<ModEntityType<?>> pTag) {
        return pTag.contains(this);
    }

    @Nullable
    public T tryCast(Entity p_147042_) {
        return (T)(p_147042_.getType() == this ? p_147042_ : null);
    }

    public Class<? extends Entity> getBaseClass() {
        return Entity.class;
    }

    public T customClientSpawn(net.minecraftforge.fmllegacy.network.FMLPlayMessages.SpawnEntity packet, Level world) {
        if (customClientFactory == null) return this.create(world);
        return customClientFactory.apply(packet, world);
    }

    /**
     * Retrieves a list of tags names this is known to be associated with.
     * This should be used in favor of TagCollection.getOwningTags, as this caches the result and automatically updates when the TagCollection changes.
     */
    public java.util.Set<ResourceLocation> getTags() {
        return reverseTags.getTagNames();
    }

    public static class Builder<T extends Entity> {
        private final ModEntityType.EntityFactory<T> factory;
        private final MobCategory category;
        private ImmutableSet<Block> immuneTo = ImmutableSet.of();
        private boolean serialize = true;
        private boolean summon = true;
        private boolean fireImmune;
        private boolean canSpawnFarFromPlayer;
        private int clientTrackingRange = 5;
        private int updateInterval = 3;
        private EntityDimensions dimensions = EntityDimensions.scalable(0.6F, 1.8F);

        private java.util.function.Predicate<ModEntityType<?>> velocityUpdateSupplier = ModEntityType::defaultVelocitySupplier;
        private java.util.function.ToIntFunction<ModEntityType<?>> trackingRangeSupplier = ModEntityType::defaultTrackingRangeSupplier;
        private java.util.function.ToIntFunction<ModEntityType<?>> updateIntervalSupplier = ModEntityType::defaultUpdateIntervalSupplier;
        private java.util.function.BiFunction<net.minecraftforge.fmllegacy.network.FMLPlayMessages.SpawnEntity, Level, T> customClientFactory;

        private Builder(ModEntityType.EntityFactory<T> p_20696_, MobCategory p_20697_) {
            this.factory = p_20696_;
            this.category = p_20697_;
            this.canSpawnFarFromPlayer = p_20697_ == MobCategory.CREATURE || p_20697_ == MobCategory.MISC;
        }

        public static <T extends Entity> ModEntityType.Builder<T> of(ModEntityType.EntityFactory<T> pFactory, MobCategory pClassification) {
            return new ModEntityType.Builder<>(pFactory, pClassification);
        }

        public static <T extends Entity> ModEntityType.Builder<T> createNothing(MobCategory pClassification) {
            return new ModEntityType.Builder<>((p_20708_, p_20709_) -> {
                return (T)null;
            }, pClassification);
        }

        public ModEntityType.Builder<T> sized(float pWidth, float pHeight) {
            this.dimensions = EntityDimensions.scalable(pWidth, pHeight);
            return this;
        }

        public ModEntityType.Builder<T> noSummon() {
            this.summon = false;
            return this;
        }

        public ModEntityType.Builder<T> noSave() {
            this.serialize = false;
            return this;
        }

        public ModEntityType.Builder<T> fireImmune() {
            this.fireImmune = true;
            return this;
        }

        public ModEntityType.Builder<T> immuneTo(Block... p_20715_) {
            this.immuneTo = ImmutableSet.copyOf(p_20715_);
            return this;
        }

        public ModEntityType.Builder<T> canSpawnFarFromPlayer() {
            this.canSpawnFarFromPlayer = true;
            return this;
        }

        public ModEntityType.Builder<T> clientTrackingRange(int pRange) {
            this.clientTrackingRange = pRange;
            return this;
        }

        public ModEntityType.Builder<T> updateInterval(int p_20718_) {
            this.updateInterval = p_20718_;
            return this;
        }

        public ModEntityType.Builder<T> setUpdateInterval(int interval) {
            this.updateIntervalSupplier = t->interval;
            return this;
        }

        public ModEntityType.Builder<T> setTrackingRange(int range) {
            this.trackingRangeSupplier = t->range;
            return this;
        }

        public ModEntityType.Builder<T> setShouldReceiveVelocityUpdates(boolean value) {
            this.velocityUpdateSupplier = t->value;
            return this;
        }

        /**
         * By default, entities are spawned clientside via {@link EntityType#create(Level)}}.
         * If you need finer control over the spawning process, use this to get read access to the spawn packet.
         */
        public ModEntityType.Builder<T> setCustomClientFactory(java.util.function.BiFunction<net.minecraftforge.fmllegacy.network.FMLPlayMessages.SpawnEntity, Level, T> customClientFactory) {
            this.customClientFactory = customClientFactory;
            return this;
        }

        public ModEntityType<T> build(String pId) {
            if (this.serialize) {
                Util.fetchChoiceType(References.ENTITY_TREE, pId);
            }

            return new ModEntityType<>(this.factory, this.category, this.serialize, this.summon, this.fireImmune, this.canSpawnFarFromPlayer, this.immuneTo, this.dimensions, this.clientTrackingRange, this.updateInterval, velocityUpdateSupplier, trackingRangeSupplier, updateIntervalSupplier, customClientFactory);
        }
    }

    public interface EntityFactory<T extends Entity> {
        T create(ModEntityType<T> p_20722_, Level p_20723_);
    }
}
