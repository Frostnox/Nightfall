package frostnox.nightfall.client;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.Attack;
import frostnox.nightfall.block.IMicroGrid;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.Tree;
import frostnox.nightfall.capability.*;
import frostnox.nightfall.client.gui.CategoryToast;
import frostnox.nightfall.client.gui.EntryToast;
import frostnox.nightfall.client.gui.screen.AttributeSelectionScreen;
import frostnox.nightfall.client.gui.screen.LimitedDebugScreen;
import frostnox.nightfall.client.gui.screen.encyclopedia.EncyclopediaCategory;
import frostnox.nightfall.client.gui.screen.encyclopedia.EncyclopediaScreen;
import frostnox.nightfall.client.gui.screen.encyclopedia.EntryClient;
import frostnox.nightfall.client.gui.screen.inventory.PlayerInventoryScreen;
import frostnox.nightfall.client.gui.screen.item.ModifiableItemScreen;
import frostnox.nightfall.client.model.AnimatedItemModel;
import frostnox.nightfall.client.render.BlockEntityAsItemRenderer;
import frostnox.nightfall.client.render.entity.PlayerRendererNF;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.data.recipe.ToolIngredientRecipe;
import frostnox.nightfall.encyclopedia.Entry;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.item.Armament;
import frostnox.nightfall.item.TieredArmorMaterial;
import frostnox.nightfall.item.TieredItemMaterial;
import frostnox.nightfall.item.client.IHeldClientTick;
import frostnox.nightfall.item.client.IClientSwapBehavior;
import frostnox.nightfall.item.item.ToolItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.capability.EntryNotificationToServer;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.EntriesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.data.SingleSortedSet;
import frostnox.nightfall.util.data.Vec3f;
import frostnox.nightfall.world.Season;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import frostnox.nightfall.world.generation.structure.ExplorerRuinsPiece;
import frostnox.nightfall.world.generation.structure.SlayerRuinsPiece;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.LegacyStuffWrapper;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.compress.utils.Lists;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class ClientEngine {
    private static final ClientEngine INSTANCE = new ClientEngine();
    private int tickCount;

    //Keys
    public final IKeyConflictContext movementKeyConflict = new IKeyConflictContext() {
        @Override
        public boolean isActive() {
            Screen screen = Minecraft.getInstance().screen;
            return screen == null || (screen instanceof ModifiableItemScreen modifiableItemScreen && modifiableItemScreen.allowMovementInputs());
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == this || other == KeyConflictContext.IN_GAME;
        }
    };
    public final KeyMapping keyDash = new KeyMapping(Nightfall.MODID + ".key.dash", GLFW_KEY_V, "key.categories.movement");
    public final KeyMapping keyOffhand = new KeyMapping(Nightfall.MODID + ".key.offhand", GLFW_KEY_R, "key.categories.gameplay");
    public final KeyMapping keyEncyclopedia = new KeyMapping(Nightfall.MODID + ".key.encyclopedia", GLFW_KEY_C, "key.categories.gameplay");
    public final KeyMapping keyModify = new KeyMapping(Nightfall.MODID + ".key.modify", GLFW_KEY_Z, "key.categories.gameplay");
    public final List<KeyMapping> movementConflictKeys = Lists.newArrayList(); //List of all keys with movement conflict type for convenience

    //Caches
    public int atlasWidth, atlasHeight;
    private boolean isDevVersion;
    private double normalizedFov;

    //Color maps
    private int[] grassCache = new int[65536];
    private int[] mossCache = new int[65536];
    private int[] forestCache = new int[256];
    private int[] lichenCache = new int[256];
    private int[] oakLeavesCache = new int[256];
    private int[] birchLeavesCache = new int[256];
    private int[] jungleLeavesCache = new int[256];
    private int[] larchLeavesCache = new int[256];
    private int[] mapleLeavesCache = new int[256];
    private int[] willowLeavesCache = new int[256];
    private int[] acaciaLeavesCache = new int[256];
    private int[] caedtarLeavesCache = new int[256];

    //Shaders
    private final ResourceLocation waterPostLocation = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "shaders/post/water.json");
    private PostChain waterPost;
    private RenderTarget translucentTarget;
    public float tempFogStart;
    private final ResourceLocation seasonPostLocation = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "shaders/post/season.json");
    private PostChain seasonPost;

    //Entity renderers
    private PlayerRendererNF defaultPlayerRenderer;
    private PlayerRendererNF slimPlayerRenderer;
    private float partialTick;
    private float lastPartialTick;
    private boolean renderersCreated = false;
    private final Minecraft mc = Minecraft.getInstance();

    //Hand renderer
    public ItemStack mainHandItem = ItemStack.EMPTY;
    public ItemStack offHandItem = ItemStack.EMPTY;
    public float mainHandHeight;
    public float oMainHandHeight;
    public float offHandHeight;
    public float oOffHandHeight;
    public int mainHandLowerTime, lastMainHandLowerTime;
    public int offHandLowerTime, lastOffHandLowerTime;

    //Screens
    private DebugScreenOverlay debugScreen;
    private PlayerInventoryScreen inventoryScreen;
    private boolean dirtyScreen;

    //Custom block entity item renderer
    private BlockEntityAsItemRenderer beiRenderer;
    private boolean beiRendererCreated = false;

    //Optional items for use with modifiable items
    private ItemStack optionalMainItem = ItemStack.EMPTY, optionalOffItem = ItemStack.EMPTY;
    private int modifiableIndexMain = -1, modifiableIndexOff = -1;
    public boolean canUseModifiableMain, canUseModifiableOff;

    //Last
    public int lastDashTick = -100;
    private double lastStamina;
    private float lastRenderYRot;
    private float lastRenderXRot;
    private double lastX;
    private double lastY;
    private double lastZ;
    private float lastXRot;
    private float lastYRot;
    private int lastPlayerTickCount;
    private long lastSeasonTime;
    private boolean firstRender = true, firstTick = true;

    //World
    public Vec3i microHitResult; //Position that look vector intersected with a gridded micro cube, local to grid
    public AABB microHitBox; //Box for microHitResult
    public BlockPos microBlockEntityPos; //Position of BlockEntity associated with microHitResult

    //Music
    public final Music MENU_MUSIC, SPRING_MUSIC, SUMMER_MUSIC, FALL_MUSIC, WINTER_MUSIC, NO_MUSIC;

    //Encyclopedia
    public final EncyclopediaCategory WYLDERY, METALLURGY;
    public @Nullable EntryClient openEntry;
    private final HashMap<EncyclopediaCategory, HashMap<ResourceLocation, EntryClient>> categories = new HashMap<>(); //Contains all categories, each with unique client entries
    private final List<EncyclopediaCategory> orderedCategories = new ObjectArrayList<>(5);
    private final Set<SoundEvent> soundsPlayedThisTick = new ObjectArraySet<>(4); //Sounds played this tick

    private ClientEngine() {
        MENU_MUSIC = new Music(SoundsNF.MUSIC_MENU.get(), 20, 20 * 20, true);
        SPRING_MUSIC = new Music(SoundsNF.MUSIC_SPRING.get(), 20 * 60 * 5, 20 * 60 * 10, false);
        SUMMER_MUSIC = new Music(SoundsNF.MUSIC_SUMMER.get(), 20 * 60 * 5, 20 * 60 * 10, false);
        FALL_MUSIC = new Music(SoundsNF.MUSIC_FALL.get(), 20 * 60 * 5, 20 * 60 * 10, false);
        WINTER_MUSIC = new Music(SoundsNF.MUSIC_WINTER.get(), 20 * 60 * 5, 20 * 60 * 10, false);
        NO_MUSIC = new Music(SoundsNF.SILENT.get(), 20 * 60 * 5, 20 * 60 * 10, false);
        //Setup encyclopedia
        WYLDERY = new EncyclopediaCategory("nightfall.category.wyldery", modLoc("textures/gui/icon/wyldery.png"),
                modLoc("textures/gui/encyclopedia/background/wyldery.png"), EntriesNF.TOOLS.getId(),
                SoundsNF.EXPERIMENT_SURVIVAL_FAIL, SoundsNF.EXPERIMENT_SURVIVAL_SUCCESS);
        registerCategory(WYLDERY);
        registerEntry(WYLDERY, EntriesNF.TOOLS, 0, 0, new ItemStack(ItemsNF.FLINT_AXE.get()));
        registerEntry(WYLDERY, EntriesNF.SLING, 2, 0, new ItemStack(ItemsNF.SLING.get()));
        registerEntry(WYLDERY, EntriesNF.TAMING, 4, 0, new ItemStack(ItemsNF.ROPE.get()), null,
                null, image(122, 23, imageLoc("breeding")), false);
        registerEntry(WYLDERY, EntriesNF.WOODCARVING, -2, -1, new ItemStack(ItemsNF.WOODEN_BOWL.get()));
        registerEntry(WYLDERY, EntriesNF.WOODWORKING, -4, -1, new ItemStack(ItemsNF.PLANKS.get(Tree.OAK).get()));
        registerEntry(WYLDERY, EntriesNF.ADVANCED_WOODWORKING, -5, -3, new ItemStack(ItemsNF.CHESTS.get(Tree.OAK).get()));
        registerEntry(WYLDERY, EntriesNF.WOODEN_SHIELD, -3, -3, new ItemStack(ItemsNF.IRONWOOD_SHIELD.get()));
        registerEntry(WYLDERY, EntriesNF.TANNING, -6, -1, new ItemStack(ItemsNF.LEATHER.get()), null,
                image(58, 12, imageLoc("item_in_water")), null, false);
        registerEntry(WYLDERY, EntriesNF.CAMPFIRE, 0, -2, new ItemStack(ItemsNF.CAMPFIRE.get()));
        registerEntry(WYLDERY, EntriesNF.POTTERY, 0, -4, new ItemStack(ItemsNF.POT.get()), null,
                null, image(36, 84, imageLoc("kiln")), false);
        registerEntry(WYLDERY, EntriesNF.FIRESTARTER, -2, -4, new ItemStack(ItemsNF.FIRESTARTER.get()));
        registerEntry(WYLDERY, EntriesNF.COOKING, 0, -6, new ItemStack(ItemsNF.HEARTY_STEW.get()), null,
                null, image(31, 31, imageLoc("stew")), false);
        registerEntry(WYLDERY, EntriesNF.WEAVING, -2, 1, new ItemStack(ItemsNF.LINEN.get()));
        registerEntry(WYLDERY, EntriesNF.MEDICINAL_BANDAGE, -2, 3, new ItemStack(ItemsNF.MEDICINAL_BANDAGE.get()));
        registerEntry(WYLDERY, EntriesNF.BOW_AND_ARROW, -4, 1, new ItemStack(ItemsNF.BOWS.get(Tree.OAK).get()));
        registerEntry(WYLDERY, EntriesNF.WARDING_CHARM, 0, 2, new ItemStack(ItemsNF.WARDING_CHARM.get()));
        registerEntry(WYLDERY, EntriesNF.WARDING_EFFIGY, 0, 4, new ItemStack(ItemsNF.WARDING_EFFIGY.get()), null,
                null, image(90, 90, imageLoc("warding_effigy")), false);

        METALLURGY = new EncyclopediaCategory("nightfall.category.metallurgy", modLoc("textures/gui/icon/metallurgy.png"),
                modLoc("textures/gui/encyclopedia/background/metallurgy.png"), EntriesNF.CASTING.getId(),
                SoundsNF.EXPERIMENT_METALLURGY_FAIL, SoundsNF.EXPERIMENT_METALLURGY_SUCCESS);
        registerCategory(METALLURGY);
        registerEntry(METALLURGY, EntriesNF.CASTING, 0, 0, new ItemStack(ItemsNF.CRUCIBLE.get()), null,
                null, image(67, 25, imageLoc("alloying")), false);
        registerEntry(METALLURGY, EntriesNF.SMITHING, -2, -1, new ItemStack(ItemsNF.ANVILS_METAL.get(Metal.COPPER).get()), null,
                null, image(100, 12, imageLoc("smithing")), false);
        registerEntry(METALLURGY, EntriesNF.SMELTING, -2, 1, new ItemStack(ItemsNF.INGOTS.get(Metal.COPPER).get()), null,
                null, image(120, 58, imageLoc("furnace")), false);
        registerEntry(METALLURGY, EntriesNF.IRONWORKING, -4, 0, new ItemStack(ItemsNF.INGOTS.get(Metal.IRON).get()));
        registerEntry(METALLURGY, EntriesNF.SABRE, 3, -5, new ItemStack(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.COPPER).get(Armament.SABRE).get()), true);
        registerEntry(METALLURGY, EntriesNF.SICKLE, 5, -5, new ItemStack(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.COPPER).get(Armament.SICKLE).get()), true);
        registerEntry(METALLURGY, EntriesNF.BUCKET, -2, -5, new ItemStack(ItemsNF.BRONZE_BUCKET.get()));
        registerEntry(METALLURGY, EntriesNF.PLATE_ARMOR, -1, -3, new ItemStack(ItemsNF.PLATES.get(Metal.COPPER).get()));
        registerEntry(METALLURGY, EntriesNF.CHAINMAIL_ARMOR, 1, -3, new ItemStack(ItemsNF.CHAINMAIL.get(Metal.COPPER).get()));
        registerEntry(METALLURGY, EntriesNF.SCALE_ARMOR, -3, -3, new ItemStack(ItemsNF.SCALES.get(Metal.COPPER).get()));
        registerEntry(METALLURGY, EntriesNF.MACE, -3, -4, new ItemStack(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.COPPER).get(Armament.MACE).get()));
        registerEntry(METALLURGY, EntriesNF.SHIELD, -1, -4, new ItemStack(ItemsNF.METAL_SHIELDS.get(Metal.COPPER).get()));
        registerEntry(METALLURGY, EntriesNF.SLAYER_PLATE, 3, -4, dyedItem(new ItemStack(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.IRON_PLATE_SLAYER).get()), SlayerRuinsPiece.ITEM_COLOR), true);
        registerEntry(METALLURGY, EntriesNF.SLAYER_CHAINMAIL, 3, -3, dyedItem(new ItemStack(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.IRON_CHAINMAIL_SLAYER).get()), SlayerRuinsPiece.ITEM_COLOR), true);
        registerEntry(METALLURGY, EntriesNF.SLAYER_SCALE, 3, -2, dyedItem(new ItemStack(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.IRON_SCALE_SLAYER).get()), SlayerRuinsPiece.ITEM_COLOR), true);
        registerEntry(METALLURGY, EntriesNF.EXPLORER_PLATE, 5, -4, dyedItem(new ItemStack(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.IRON_PLATE_EXPLORER).get()), ExplorerRuinsPiece.ITEM_COLOR), true);
        registerEntry(METALLURGY, EntriesNF.EXPLORER_CHAINMAIL, 5, -3, dyedItem(new ItemStack(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.IRON_CHAINMAIL_EXPLORER).get()), ExplorerRuinsPiece.ITEM_COLOR), true);
        registerEntry(METALLURGY, EntriesNF.EXPLORER_SCALE, 5, -2, dyedItem(new ItemStack(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.IRON_SCALE_EXPLORER).get()), ExplorerRuinsPiece.ITEM_COLOR), true);
    }

    public static ClientEngine get() {
        return INSTANCE;
    }

    private static ItemStack dyedItem(ItemStack item, int color) {
        DyeableLeatherItem dyeableItem = (DyeableLeatherItem) item.getItem();
        dyeableItem.setColor(item, color);
        return item;
    }

    public RenderTarget getTranslucentTarget() {
        return translucentTarget;
    }

    private void setupShaders() {
        if(translucentTarget != null) {
            translucentTarget.destroyBuffers();
            translucentTarget = null;
        }
        waterPost = getShader(waterPost, waterPostLocation);
        translucentTarget = waterPost.getTempTarget("translucent");
        seasonPost = getShader(seasonPost, seasonPostLocation);
    }

    private PostChain getShader(PostChain oldShader, ResourceLocation location) {
        if(oldShader != null) oldShader.close();
        PostChain shader = null;
        try {
            shader = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), location);
            shader.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        }
        catch(IOException exception) {
            Nightfall.LOGGER.error("Failed to load shader: {}", location, exception);
        }
        catch(JsonSyntaxException exception) {
            Nightfall.LOGGER.error("Failed to parse shader: {}", location, exception);
        }
        return shader;
    }

    public void resize(int width, int height) {
        if(waterPost != null) waterPost.resize(width, height);
        if(seasonPost != null) seasonPost.resize(width, height);
    }

    public void applyWaterShader(Matrix4f projMat, float partialTick) {
        //Water effect is applied via core shaders in vanilla; these aren't used in Sodium so a post effect is applied instead
        if(!Nightfall.isRubidiumLoaded || Minecraft.useShaderTransparency()) return;

        EffectInstance pass0 = waterPost.passes.get(0).getEffect();
        //InvMat = inverse(ProjectionMatrix * CameraMatrix)
        Matrix4f invMat = projMat.copy();
        invMat.multiply(Vector3f.XP.rotationDegrees(mc.gameRenderer.getMainCamera().getXRot()));
        invMat.invert();
        pass0.safeGetUniform("InvMat").set(invMat);
        pass0.safeGetUniform("DepthValue").set(Math.min(12F, RenderSystem.getShaderFogEnd() / 16F / 2F));
        pass0.safeGetUniform("FogColor").set(RenderSystem.getShaderFogColor());
        //Use cached value since fog was disabled for translucent state
        pass0.safeGetUniform("FogStart").set(tempFogStart);
        pass0.safeGetUniform("FogEnd").set(RenderSystem.getShaderFogEnd());
        pass0.safeGetUniform("FogShape").set(RenderSystem.getShaderFogShape().getIndex());

        waterPost.process(partialTick);
        mc.getMainRenderTarget().copyDepthFrom(translucentTarget);
    }

    public void applySeasonShader(float partialTick) {
        if(LevelData.isPresent(mc.level)) {
            float y = mc.player != null ? (float) mc.player.getEyePosition(partialTick).y : Float.MAX_VALUE;
            if(y > ContinentalChunkGenerator.SEA_LEVEL - 64) {
                float time = Season.getNormalizedProgress(LevelData.get(mc.level).getSeasonTime());
                if((time > 0.2F && time < 0.55F) || (time > 0.7F || time < 0.05F)) {
                    float scalar = 0.05F;
                    float intensity = y > (ContinentalChunkGenerator.SEA_LEVEL - 32) ? 1F : ((y - (ContinentalChunkGenerator.SEA_LEVEL - 64)) / 32F);
                    float r, b;
                    if(time < 0.05F) {
                        r = -scalar * (0.5F - time / 0.05F);
                        b = -r;
                    }
                    else if(time < 0.3F) {
                        r = scalar * ((time - 0.2F) / 0.1F);
                        b = -r;
                    }
                    else if(time < 0.45F) {
                        r = scalar;
                        b = -scalar;
                    }
                    else if(time < 0.55F) {
                        r = scalar * (1F - (time - 0.45F) / 0.1F);
                        b = -r;
                    }
                    else if(time < 0.8F) {
                        r = -scalar * ((time - 0.7F) / 0.1F);
                        b = -r;
                    }
                    else if(time < 0.95F) {
                        r = -scalar;
                        b = scalar;
                    }
                    else {
                        r = -scalar * (1F - (time - 0.95F) / 0.1F);
                        b = -r;
                    }
                    for(PostPass pass : seasonPost.passes) pass.getEffect().safeGetUniform("Color").set(1 + r * intensity, 1, 1 + b * intensity);
                    seasonPost.process(partialTick);
                }
            }
        }
    }

    //Avoid doing stuff that isn't available during data gen
    public void dataExcludedInit() {
        //KeyMapping conflict contexts
        setMovementConflictKeys(mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft, mc.options.keyRight, mc.options.keyJump, mc.options.keyShift,
                mc.options.keySprint, mc.options.keyTogglePerspective, keyDash);
        mc.getTutorial().setStep(TutorialSteps.NONE);
        mc.options.joinedFirstServer = true;
        isDevVersion = mc.getLaunchedVersion().equals("MOD_DEV");
    }

    public void tickRenderStart() {
        if(mc.isPaused()) partialTick = lastPartialTick;
        else partialTick = mc.getFrameTime();
        Camera camera = mc.gameRenderer.getMainCamera();
        normalizedFov = mc.gameRenderer.getFov(camera, partialTick, true) / 85.556D;
        if(mc.player != null && mc.player.isAlive() && !mc.isPaused()) {
            IActionTracker capA = ActionTracker.get(mc.player);
            Action action = capA.getAction();
            if(action == null) return;
            if(capA.getState() == action.getChargeState() && !capA.isStunnedOrHitPaused()) capA.setChargePartial(partialTick);
            Player player = mc.player;
            if(firstRender) {
                lastX = player.getX();
                lastY = player.getY();
                lastZ = player.getZ();
                lastXRot = player.getXRot();
                lastYRot = player.getYRot();
                lastRenderYRot = player.getYRot();
                lastPlayerTickCount = player.tickCount;
                firstRender = false;
            }
            if(action instanceof Attack attack && !ActionsNF.isEmpty(capA.getActionID())) {
                float limitY = Math.max(0, ((player.tickCount + partialTick) - (lastPlayerTickCount + lastPartialTick)) * attack.getMaxYRot(capA.getState()));
                float limitX = Math.max(0, ((player.tickCount + partialTick) - (lastPlayerTickCount + lastPartialTick)) * attack.getMaxXRot(capA.getState()));
                player.setYRot(Mth.clamp(player.getYRot(), lastRenderYRot - limitY, lastRenderYRot + limitY));
                player.setXRot(Mth.clamp(player.getXRot(), lastRenderXRot - limitX, lastRenderXRot + limitX));
            }
            /*if(capA.isDamaging()) {
                Attack attack = (Attack) Actions.getAction(capA.getActionID());
                float limitY = ((player.tickCount + partialTick) - (lastPlayerTickCount + lastPartialTick)) / capA.getDuration() * attack.getMaxYRot();
                float limitX = ((player.tickCount + partialTick) - (lastPlayerTickCount + lastPartialTick)) / capA.getDuration() * attack.getMaxXRot();
                player.setYRot(Mth.clamp(player.getYRot(), lastYRot - limitY, lastYRot + limitY));
                player.setXRot(Mth.clamp(player.getXRot(), lastXRot - limitX, lastXRot + limitX));
            }*/
            /*else if(capA.getState() == action.getChainState() && action instanceof Attack attack) {
                //Assume a chain state always follows a damage state
                float rotAmount = (float) action.getDurationArray()[action.getChainState()] / (float) action.getDurationArray()[action.getChainState() - 1];
                float limitY = ((player.tickCount + partialTick) - (lastPlayerTickCount + lastPartialTick)) / capA.getDuration() * attack.getMaxYRot() * rotAmount;
                float limitX = ((player.tickCount + partialTick) - (lastPlayerTickCount + lastPartialTick)) / capA.getDuration() * attack.getMaxXRot() * rotAmount;
                player.setYRot(Mth.clamp(player.getYRot(), lastYRot - limitY, lastYRot + limitY));
                player.setXRot(Mth.clamp(player.getXRot(), lastXRot - limitX, lastXRot + limitX));
            }*/
            lastRenderYRot = player.getYRot();
            lastRenderXRot = player.getXRot();
            lastPlayerTickCount = player.tickCount;
        }
        lastPartialTick = partialTick;
    }

    public void tickRenderEnd() {
        if(!mc.isPaused()) {
            Player player = mc.player;
            microHitResult = null;
            microHitBox = null;
            microBlockEntityPos = null;
            if((player.getPose() == Pose.STANDING || player.getPose() == Pose.CROUCHING) && (player.getMainHandItem().is(TagsNF.GRID_INTERACTABLE) || player.getOffhandItem().is(TagsNF.GRID_INTERACTABLE))) {
                int searchReach = 3; //Search reach could be higher to find BlockEntities that are further away than their grids
                int playerReach = 2;
                Camera camera = mc.gameRenderer.getMainCamera();
                Vector3f look = new Vector3f(player.getViewVector(partialTick));
                Vec3 startVec = camera.isDetached() ? player.getEyePosition(partialTick) : camera.getPosition();
                //Adjust for bob transformations
                if(mc.options.bobView) {
                    float f = player.walkDist - player.walkDistO;
                    float f1 = -(player.walkDist + f * getPartialTick());
                    float f2 = Mth.lerp(getPartialTick(), player.oBob, player.bob);
                    float xBob = Mth.sin(f1 * (float) Math.PI) * f2 * 0.5F;
                    float yaw = MathUtil.toRadians(Mth.wrapDegrees(camera.getYRot() + 90F));
                    startVec = startVec.subtract(xBob * Mth.sin(-yaw), -Math.abs(Mth.cos(f1 * (float) Math.PI) * f2), xBob * Mth.cos(-yaw));
                    Quaternion bob = Vector3f.ZP.rotationDegrees(Mth.sin(f1 * (float) Math.PI) * f2 * 3.0F);
                    bob.mul(Vector3f.XP.rotationDegrees(Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F));
                    look.transform(bob);
                }
                look.mul(playerReach);
                Vec3 endVec = startVec.add(look.x(), look.y(), look.z());
                BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
                for(int x = -searchReach; x <= searchReach; x++) {
                    for(int y = -searchReach; y <= searchReach; y++) {
                        for(int z = -searchReach; z <= searchReach; z++) {
                            blockPos.set(startVec.x + x, startVec.y + y, startVec.z + z);
                            BlockEntity blockEntity = player.level.getBlockEntity(blockPos);
                            if(blockEntity instanceof IMicroGrid gridEntity) {
                                float rot = -gridEntity.getRotationDegrees();
                                Vec3 gridPos = gridEntity.getWorldPos(blockPos, 0, 0, 0);
                                Vector3f gridSize = MathUtil.rotatePointByYaw(new Vector3f(gridEntity.getGridXSize()/16F, gridEntity.getGridYSize()/16F, gridEntity.getGridZSize()/16F), rot);
                                //Check first that anywhere on the grid is being looked at and LoS is available
                                Optional<Vec3> prelimHitVec = new AABB(gridPos, gridPos.add(gridSize.x(), gridSize.y(), gridSize.z())).clip(startVec, endVec);
                                if(prelimHitVec.isEmpty()) continue;
                                else if(player.level.clip(new ClipContext(startVec, prelimHitVec.get(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS) continue;
                                double closestDistSqr = Double.MAX_VALUE;
                                for(int gridX = 0; gridX < gridEntity.getGridXSize(); gridX++) {
                                    for(int gridY = 0; gridY < gridEntity.getGridYSize(); gridY++) {
                                        for(int gridZ = 0; gridZ < gridEntity.getGridZSize(); gridZ++) {
                                            Vector3f selectPos = MathUtil.rotatePointByYaw(new Vector3f(gridX/16F, gridY/16F, gridZ/16F), rot);
                                            Vector3f selectSize = MathUtil.rotatePointByYaw(new Vector3f(1/16F, 1/16F, 1/16F), rot);
                                            Vec3 finalPos = gridPos.add(selectPos.x(), selectPos.y(), selectPos.z());
                                            //player.level.addParticle(ParticleTypes.FLAME, finalPos.x, finalPos.y, finalPos.z, 0, 0, 0);
                                            Optional<Vec3> hitVec = new AABB(finalPos, finalPos.add(selectSize.x(), selectSize.y(), selectSize.z())).clip(startVec, endVec);
                                            if(gridEntity.getGrid()[gridX][gridY][gridZ] && hitVec.isPresent()) {
                                                double distSqr = startVec.distanceToSqr(hitVec.get());
                                                if(distSqr < closestDistSqr) {
                                                    microHitResult = new Vec3i(gridX, gridY, gridZ);
                                                    microHitBox = new AABB(gridX/16D - 0.001, gridY/16D - 0.001, gridZ/16D - 0.001, (gridX + 1)/16D + 0.001, (gridY + 1)/16D + 0.001, (gridZ + 1)/16D + 0.001);
                                                    microBlockEntityPos = new BlockPos(blockPos);
                                                    closestDistSqr = distSqr;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void tickStart() {
        tickCount++;
        soundsPlayedThisTick.clear();
        LocalPlayer player = mc.player;
        if(player != null && player.isAlive()) {
            IPlayerData capP = PlayerData.get(player);
            if(firstTick) {
                capP.setLastMainItem();
                capP.setLastOffItem();
                lastStamina = capP.getStamina();
                if(LevelData.isPresent(player.level)) {
                    lastSeasonTime = LevelData.get(player.level).getSeasonTime();
                }
            }

            this.oMainHandHeight = this.mainHandHeight;
            this.oOffHandHeight = this.offHandHeight;

            ItemStack mainItem = player.getMainHandItem();
            ItemStack offItem = player.getOffhandItem();
            if(ItemStack.matches(this.mainHandItem, mainItem)) this.mainHandItem = mainItem;
            if(ItemStack.matches(this.offHandItem, offItem)) this.offHandItem = offItem;

            if(player.isHandsBusy()) {
                this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.15F, 0.0F, 1.0F);
                this.offHandHeight = Mth.clamp(this.offHandHeight - 0.15F, 0.0F, 1.0F);
            }
            else {
                float limitMain = Minecraft.getInstance().getItemRenderer().getModel(mainHandItem, player.level, player, 0) instanceof AnimatedItemModel.Model model ? (float) model.swapSpeed * 0.31F : 0.31F;
                float limitOff = Minecraft.getInstance().getItemRenderer().getModel(offHandItem, player.level, player, 0) instanceof AnimatedItemModel.Model model ? (float) model.swapSpeed * 0.31F : 0.31F;
                boolean requipM = net.minecraftforge.client.ForgeHooksClient.shouldCauseReequipAnimation(this.mainHandItem, mainItem, player.getInventory().selected);
                boolean requipO = net.minecraftforge.client.ForgeHooksClient.shouldCauseReequipAnimation(this.offHandItem, offItem, -1);
                if(!requipM && this.mainHandItem != mainItem) this.mainHandItem = mainItem;
                if(!requipO && this.offHandItem != offItem) this.offHandItem = offItem;
                this.mainHandHeight += Mth.clamp((!requipM ? 1 : 0) - this.mainHandHeight, -limitMain, limitMain);
                this.offHandHeight += Mth.clamp((!requipO ? 1 : 0) - this.offHandHeight, -limitOff, limitOff);
            }

            if(this.mainHandHeight < 0.05F) this.mainHandItem = mainItem;
            if(this.offHandHeight < 0.05F) this.offHandItem = offItem;

            if(!capP.getLastMainItem().sameItemStackIgnoreDurability(mainItem) || firstTick) {
                optionalMainItem = ItemStack.EMPTY;
                canUseModifiableMain = true;
                if(!mainItem.isEmpty() && mainItem.getItem() instanceof IClientSwapBehavior swapItem) swapItem.swapClient(mc, mainItem, player, true);
                capP.setLastMainItem();
            }
            if(!capP.getLastOffItem().sameItemStackIgnoreDurability(offItem) || firstTick) {
                optionalOffItem = ItemStack.EMPTY;
                canUseModifiableOff = true;
                if(!offItem.isEmpty() && offItem.getItem() instanceof IClientSwapBehavior swapItem) swapItem.swapClient(mc, offItem, player, false);
                capP.setLastOffItem();
            }
            if(mainItem.getItem() instanceof IHeldClientTick tickItem) tickItem.onHeldTickClient(mc, mainItem, player, true);
            if(offItem.getItem() instanceof IHeldClientTick tickItem) tickItem.onHeldTickClient(mc, offItem, player, false);

            lastMainHandLowerTime = mainHandLowerTime;
            if(canUseModifiableMain || optionalMainItem.isEmpty() || getPlayer().getAbilities().instabuild) {
                if(mainHandLowerTime > 0) mainHandLowerTime--;
            }
            else if(mainHandLowerTime < 4) mainHandLowerTime++;
            lastOffHandLowerTime = offHandLowerTime;
            if(canUseModifiableOff || optionalOffItem.isEmpty() || getPlayer().getAbilities().instabuild) {
                if(offHandLowerTime > 0) offHandLowerTime--;
            }
            else if(offHandLowerTime < 4) offHandLowerTime++;

            lastX = player.getX();
            lastY = player.getY();
            lastZ = player.getZ();
            lastXRot = player.getXRot();
            lastYRot = player.getYRot();
            lastStamina = capP.getStamina();
            firstTick = false;
        }
    }

    public void tickEnd() {
        if(mc.level != null && LevelData.isPresent(mc.level)) {
            ILevelData cap = LevelData.get(mc.level);
            cap.tick();
            if(Math.abs(cap.getSeasonTime() - lastSeasonTime) > 1200) Minecraft.getInstance().levelRenderer.allChanged(); //Catches big time changes from commands
            lastSeasonTime = cap.getSeasonTime();
        }
    }

    public int getTickCount() {
        return tickCount;
    }

    public double getLastStamina() {
        return lastStamina;
    }

    public float getLastRenderYRot() {
        return lastRenderYRot;
    }

    public float getLastRenderXRot() {
        return lastRenderXRot;
    }

    public double getLastX() {
        return lastX;
    }

    public double getLastY() {
        return lastY;
    }

    public double getLastZ() {
        return lastZ;
    }

    public float getLastXRot() {
        return lastXRot;
    }

    public float getLastYRot() {
        return lastYRot;
    }

    public int getLastPlayerTickCount() {
        return lastPlayerTickCount;
    }

    public ItemStack getOptionalMainItem() {
        return optionalMainItem;
    }

    public ItemStack getOptionalOffItem() {
        return optionalOffItem;
    }

    /**
     * @return main camera's FOV scaled to a range of 0 to 1 where 1 has FOV = 85.556
     */
    public double getNormalizedFov() {
        return normalizedFov;
    }

    private void fillGrassCache(int[] colors) {
        grassCache = colors;
    }

    public int getGrassColor(float temperature, float humidity) {
        int i = (int)(temperature * 255.0D) | ((int)(humidity * 255.0D) << 8);
        if(i < 0) return grassCache[0];
        if(i > 65535) return grassCache[65535];
        return grassCache[i];
    }

    private void fillMossCache(int[] colors) {
        mossCache = colors;
    }

    public int getMossColor(float temperature, float humidity) {
        int i = (int)(temperature * 255.0D) | ((int)(humidity * 255.0D) << 8);
        if(i < 0) return mossCache[0];
        if(i > 65535) return mossCache[65535];
        return mossCache[i];
    }

    private void fillForestCache(int[] colors) {
        forestCache = colors;
    }

    public int getForestColor(float humidity) {
        int i = (int) (humidity * 255F);
        if(i >= 256) return forestCache[255];
        return forestCache[i];
    }

    private void fillLichenCache(int[] colors) {
        lichenCache = colors;
    }

    public int getLichenColor(float humidity) {
        int i = (int) (humidity * 255F);
        if(i >= 256) return lichenCache[255];
        return lichenCache[i];
    }

    public int getLeavesColor(Tree type, float season) {
        return switch(type) {
            case BIRCH -> getBirchLeavesColor(season);
            case JUNGLE -> getJungleLeavesColor(season);
            case LARCH -> getLarchLeavesColor(season);
            case OAK -> getOakLeavesColor(season);
            case MAPLE -> getMapleLeavesColor(season);
            case WILLOW -> getWillowLeavesColor(season);
            case ACACIA -> getAcaciaLeavesColor(season);
            case CAEDTAR -> getCaedtarLeavesColor(season);
            default -> 0xFFFFFF;
        };
    }

    private void fillOakLeavesCache(int[] colors) {
        oakLeavesCache = colors;
    }

    private int getOakLeavesColor(float season) {
        if(season < 0F || season > 1F) return oakLeavesCache[0];
        return oakLeavesCache[(int) (season * 255D)];
    }

    private void fillBirchLeavesCache(int[] colors) {
        birchLeavesCache = colors;
    }

    private int getBirchLeavesColor(float season) {
        if(season < 0F || season > 1F) return birchLeavesCache[0];
        return birchLeavesCache[(int) (season * 255D)];
    }

    private void fillJungleLeavesCache(int[] colors) {
        jungleLeavesCache = colors;
    }

    private int getJungleLeavesColor(float season) {
        if(season < 0F || season > 1F) return jungleLeavesCache[0];
        return jungleLeavesCache[(int) (season * 255D)];
    }

    private void fillLarchLeavesCache(int[] colors) {
        larchLeavesCache = colors;
    }

    private int getLarchLeavesColor(float season) {
        if(season < 0F || season > 1F) return larchLeavesCache[0];
        return larchLeavesCache[(int) (season * 255D)];
    }

    private void fillMapleLeavesCache(int[] colors) {
        mapleLeavesCache = colors;
    }

    private int getMapleLeavesColor(float season) {
        if(season < 0F || season > 1F) return mapleLeavesCache[0];
        return mapleLeavesCache[(int) (season * 255D)];
    }

    private void fillWillowLeavesCache(int[] colors) {
        willowLeavesCache = colors;
    }

    private int getWillowLeavesColor(float season) {
        if(season < 0F || season > 1F) return willowLeavesCache[0];
        return willowLeavesCache[(int) (season * 255D)];
    }

    private void fillAcaciaLeavesCache(int[] colors) {
        acaciaLeavesCache = colors;
    }

    private int getAcaciaLeavesColor(float season) {
        if(season < 0F || season > 1F) return acaciaLeavesCache[0];
        return acaciaLeavesCache[(int) (season * 255D)];
    }

    private void fillCaedtarLeavesCache(int[] colors) {
        caedtarLeavesCache = colors;
    }

    private int getCaedtarLeavesColor(float season) {
        if(season < 0F || season > 1F) return caedtarLeavesCache[0];
        return caedtarLeavesCache[(int) (season * 255D)];
    }

    public Player getPlayer() {
        return mc.player;
    }

    public List<Entity> getPlayerToPush(ActionableEntity pusher) {
        if(mc.player != null && pusher.getPushForce() >= LevelUtil.PLAYER_PUSH && pusher.getBoundingBox().intersects(mc.player.getBoundingBox())
                && EntitySelector.pushableBy(pusher).test(mc.player)) {
            return List.of(mc.player);
        }
        return List.of();
    }

    public Vec3 getPlayerPosition(float partialTick) {
        if(mc.player != null) {
            double x = Mth.lerp(partialTick, lastX, mc.player.getX());
            double y = Mth.lerp(partialTick, lastY, mc.player.getY());
            double z = Mth.lerp(partialTick, lastZ, mc.player.getZ());
            return new Vec3(x, y, z);
        }
        return Vec3.ZERO;
    }

    public PlayerRendererNF getPlayerCombatRenderer(PlayerRenderer renderer) {
        if(!renderersCreated) createPlayerRenderers();
        return mc.getEntityRenderDispatcher().getSkinMap().get("default").equals(renderer) ? defaultPlayerRenderer : slimPlayerRenderer;
    }

    public PlayerRendererNF getDefaultPlayerCombatRenderer() {
        if(!renderersCreated) createPlayerRenderers();
        return defaultPlayerRenderer;
    }

    public PlayerRendererNF getSlimPlayerCombatRenderer() {
        if(!renderersCreated) createPlayerRenderers();
        return slimPlayerRenderer;
    }

    private void createPlayerRenderers() {
        EntityRendererProvider.Context renderer = new EntityRendererProvider.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getResourceManager(), mc.getEntityModels(), mc.font);
        defaultPlayerRenderer = new PlayerRendererNF(renderer, false);
        slimPlayerRenderer = new PlayerRendererNF(renderer, true, ImmutableMap.of("arm", new Vec3f(0.75F, 1F, 1F)));
        renderersCreated = true;
    }

    public BlockEntityAsItemRenderer getBlockEntityAsItemRenderer() {
        if(!beiRendererCreated) createBlockEntityAsItemRenderer();
        return beiRenderer;
    }

    private void createBlockEntityAsItemRenderer() {
        beiRenderer = new BlockEntityAsItemRenderer();
        beiRendererCreated = true;
    }

    public void setMovementConflictKey(KeyMapping key) {
        key.setKeyConflictContext(movementKeyConflict);
        movementConflictKeys.add(key);
    }

    public void setMovementConflictKeys(KeyMapping... keys) {
        for(KeyMapping key : keys) setMovementConflictKey(key);
    }

    public boolean isDevVersion() {
        return isDevVersion;
    }

    public boolean isShiftHeld() {
        return InputConstants.isKeyDown(mc.getWindow().getWindow(), InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(mc.getWindow().getWindow(), InputConstants.KEY_RSHIFT);
    }

    public boolean isCtrlHeld() {
        return Screen.hasControlDown();
    }

    public Component getAttackKeyName() {
        return mc.options.keyAttack.getKey().getDisplayName();
    }

    public Component getUseKeyName() {
        return mc.options.keyUse.getKey().getDisplayName();
    }

    public DebugScreenOverlay getLimitedDebugScreen() {
        if(debugScreen == null) debugScreen = new LimitedDebugScreen(mc);
        return debugScreen;
    }

    public PlayerInventoryScreen getInventoryScreen(Player player) {
        if(inventoryScreen == null || dirtyScreen) {
            inventoryScreen = new PlayerInventoryScreen(player);
            dirtyScreen = false;
        }
        return inventoryScreen;
    }

    public void dirtyScreen() {
        dirtyScreen = true;
    }

    public void updateRecipeSearchItems() {
        if(mc.screen != null && mc.screen == inventoryScreen) {
            inventoryScreen.recipeSearch.updateItems();
        }
    }

    public boolean isRecipeSearchOpen() {
        if(mc.screen != null && mc.screen == inventoryScreen) {
            return inventoryScreen.selectedComponent == inventoryScreen.recipeSearch;
        }
        else return false;
    }

    public void openAttributeSelectionScreen() {
        if(!(mc.screen instanceof AttributeSelectionScreen) && mc.player != null && mc.player.isAlive()) mc.setScreen(new AttributeSelectionScreen());
    }

    public void updateToolItemRecipeSelection(ToolItem tool, boolean mainHand) {
        List<ToolIngredientRecipe> recipes = tool.getRecipes(mc.level, mc.player, mc.player.getItemInHand(mainHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
        List<ItemStack> items = new ObjectArrayList<>(recipes.size());
        for(int i = 0; i < recipes.size(); i++) items.add(i, recipes.get(i).getResultItem());
        ModifiableItemScreen.initSelection(mc, items, tool, mainHand);
    }

    public float getPartialTick() {
        return partialTick;
    }

    public float getLastPartialTick() {
        return lastPartialTick;
    }

    public void setModifiableIndex(boolean main, ItemStack item, int index) {
        if(main) {
            optionalMainItem = item;
            modifiableIndexMain = index;
        }
        else {
            optionalOffItem = item;
            modifiableIndexOff = index;
        }
    }

    public int getModifiableIndexMain() {
        return modifiableIndexMain;
    }

    public int getModifiableIndexOff() {
        return modifiableIndexOff;
    }

    private static ResourceLocation modLoc(String name) {
        return ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, name);
    }

    private static ResourceLocation itemLoc(RegistryObject<? extends Item> item) {
        return ResourceLocation.fromNamespaceAndPath(item.getId().getNamespace(), "textures/item/" + item.getId().getPath() + ".png");
    }

    private static ResourceLocation invItemLoc(RegistryObject<? extends Item> item) {
        return ResourceLocation.fromNamespaceAndPath(item.getId().getNamespace(), "textures/item/" + item.getId().getPath() + "_inventory.png");
    }

    private static ResourceLocation imageLoc(String name) {
        return ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/encyclopedia/image/" + name + ".png");
    }

    private static EntryClient.Image image(int width, int height, ResourceLocation texture) {
        return new EntryClient.Image(width, height, texture);
    }

    public void registerCategory(EncyclopediaCategory category) {
        if(categories.containsKey(category)) Nightfall.LOGGER.error("Category '" + category.name() + "' is already in use.");
        else {
            categories.put(category, new HashMap<>());
            orderedCategories.add(category);
            orderedCategories.sort((c1, c2) -> {
                if(c1 == WYLDERY) return -1;
                else if(c2 == WYLDERY) return 1;
                else if(c1.name().contains(Nightfall.MODID)) {
                    if(!c2.name().contains(Nightfall.MODID)) return -1;
                }
                else if(c2.name().contains(Nightfall.MODID)) {
                    if(!c1.name().contains(Nightfall.MODID)) return 1;
                }
                return c1.name().compareTo(c2.name());
            });
        }
    }

    public void registerEntry(EncyclopediaCategory category, RegistryObject<? extends Entry> entry, int x, int y, ItemStack icon) {
        registerEntry(category, entry, x, y, icon, null, null, null, false);
    }

    public void registerEntry(EncyclopediaCategory category, RegistryObject<? extends Entry> entry, int x, int y, ItemStack icon, boolean separated) {
        registerEntry(category, entry, x, y, icon, null, null, null, separated);
    }

    public void registerEntry(EncyclopediaCategory category, RegistryObject<? extends Entry> entry, int x, int y, ResourceLocation icon, boolean separated) {
        registerEntry(category, entry, x, y, ItemStack.EMPTY, icon, null, null, separated);
    }

    public void registerEntry(EncyclopediaCategory category, RegistryObject<? extends Entry> entry, int x, int y, ItemStack itemIcon, ResourceLocation icon, EntryClient.Image puzzleImage, EntryClient.Image completedImage, boolean separated) {
        ResourceLocation entryId = entry.getId();
        if(!categories.containsKey(category)) Nightfall.LOGGER.error("Category '" + category.name() + "' is not registered.");
        else if(categories.get(category).containsKey(entryId)) Nightfall.LOGGER.error("EntryClient ID " + entryId + " is already in use.");
        else {
            List<RegistryObject<? extends Entry>> addenda = new ObjectArrayList<>(4);
            for(RegistryObject<Entry> e : EntriesNF.ENTRIES.getEntries()) {
                if(e.get().isAddendum && e.get().parents.get(0).getId().equals(entryId)) addenda.add(e);
            }
            categories.get(category).put(entryId, new EntryClient(category, entry, x, y, itemIcon, icon, puzzleImage, completedImage, addenda, separated));
        }
    }

    public List<EntryClient> getEntries(EncyclopediaCategory category) {
        return categories.get(category).values().stream().toList();
    }

    public List<EncyclopediaCategory> getCategories() {
        return orderedCategories;
    }

    public EncyclopediaCategory getCategory(ResourceLocation id) {
        for(EncyclopediaCategory category : categories.keySet()) {
            HashMap<ResourceLocation, EntryClient> map = categories.get(category);
            EntryClient entry = map.get(id);
            if(entry != null) return category;
        }
        Nightfall.LOGGER.error("Could not match entry " + id + " to a category.");
        return null;
    }

    public @Nullable EntryClient getEntry(ResourceLocation id) {
        for(HashMap<ResourceLocation, EntryClient> map : categories.values()) {
            EntryClient entry = map.get(id);
            if(entry != null) return entry;
        }
        return null;
    }

    public @Nullable EntryClient getEntry(EncyclopediaCategory category, ResourceLocation id) {
        if(!categories.containsKey(category)) throw new IllegalArgumentException("Category '" + category.name() + "' not found.");
        return categories.get(category).get(id);
    }

    public void playExperimentSound(boolean success) {
        if(openEntry != null) {
            playGuiSound(success ? openEntry.category.experimentSuccessSound().get() : openEntry.category.experimentFailSound().get(),
                    1F, 1F);
        }
    }

    public void playGuiSound(SoundEvent sound, float pitch, float volume) {
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    public void playToastSound(SoundEvent sound, float pitch, float volume) {
        if(!soundsPlayedThisTick.contains(sound)) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
            soundsPlayedThisTick.add(sound);
        }
    }

    public void tryCategoryNotification(ResourceLocation entryId) {
        for(EncyclopediaCategory category : getCategories()) {
            if(category == WYLDERY) continue;
            if(category.unlockEntryId().equals(entryId)) {
                PlayerData.get(mc.player).addEntryNotification(entryId);
                NetworkHandler.toServer(new EntryNotificationToServer(entryId, false));
                mc.getToasts().addToast(new CategoryToast(category));
                if(mc.screen instanceof EncyclopediaScreen encyclopediaScreen) encyclopediaScreen.dirtyTabs = true;
                break;
            }
        }
    }

    public void doEntryNotification(ResourceLocation id) {
        if(EntriesNF.contains(id)) {
            IPlayerData capP = PlayerData.get(mc.player);
            Entry entry = EntriesNF.get(id);
            ResourceLocation notifId = entry.isAddendum ? entry.parents.get(0).getId() : entry.getRegistryName();
            if(capP.hasEntry(getCategory(notifId).unlockEntryId())) {
                capP.addEntryNotification(notifId);
                NetworkHandler.toServer(new EntryNotificationToServer(notifId, false));
                mc.getToasts().addToast(new EntryToast(entry));
            }
        }
    }

    public void visuallyDestroyBlock(BlockPos pos, int progress) {
        if(progress >= 0 && progress < 10) {
            BlockDestructionProgress data = new BlockDestructionProgress(0, pos);
            data.updateTick(Integer.MAX_VALUE);
            data.setProgress(progress);
            mc.levelRenderer.destructionProgress.put(pos.asLong(), new SingleSortedSet<>(data));
        }
        else mc.levelRenderer.destructionProgress.remove(pos.asLong());
    }

    public void playEntitySound(Entity pEntity, SoundEvent pEvent, SoundSource pCategory, float pVolume, float pPitch) {
        pEntity.level.playSound(mc.player, pEntity, pEvent, pCategory, pVolume, pPitch);
    }

    public void playUniqueEntitySound(Entity pEntity, SoundEvent sound, SoundSource pCategory, float pVolume, float pPitch) {
        if(!soundsPlayedThisTick.contains(sound) && mc.player != null && mc.player.distanceToSqr(pEntity) <= pVolume * 16 * pVolume * 16) {
            pEntity.level.playSound(mc.player, pEntity, sound, pCategory, pVolume, pPitch);
            soundsPlayedThisTick.add(sound);
        }
    }

    /**
     * Static function to register listeners that use ClientEngine before it is created
     */
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        //Independent ClientEngine listener
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
            ClientEngine.get().setupShaders();
            //Hacky way to save the block atlas dimensions for later use
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(Fluids.WATER.getAttributes().getStillTexture());
            ClientEngine.get().atlasWidth = (int) ((sprite.getX() + sprite.getWidth()) / sprite.getU1());
            ClientEngine.get().atlasHeight = (int) ((sprite.getY() + sprite.getHeight()) / sprite.getV1());
        });
        //Color map listeners
        event.registerReloadListener(new SimplePreparableReloadListener<int[]>() {
            private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/colormap/grass.png");
            @Override
            protected int[] prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                try { return LegacyStuffWrapper.getPixels(pResourceManager, LOCATION); }
                catch (IOException ioexception) { throw new IllegalStateException("Failed to load grass color texture", ioexception); }
            }
            @Override
            protected void apply(int[] pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                ClientEngine.get().fillGrassCache(pObject);
            }
        });
        event.registerReloadListener(new SimplePreparableReloadListener<int[]>() {
            private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/colormap/moss.png");
            @Override
            protected int[] prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                try { return LegacyStuffWrapper.getPixels(pResourceManager, LOCATION); }
                catch (IOException ioexception) { throw new IllegalStateException("Failed to load moss color texture", ioexception); }
            }
            @Override
            protected void apply(int[] pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                ClientEngine.get().fillMossCache(pObject);
            }
        });
        event.registerReloadListener(new SimplePreparableReloadListener<int[]>() {
            private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/colormap/forest.png");
            @Override
            protected int[] prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                try { return LegacyStuffWrapper.getPixels(pResourceManager, LOCATION); }
                catch (IOException ioexception) { throw new IllegalStateException("Failed to load forest color texture", ioexception); }
            }
            @Override
            protected void apply(int[] pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                ClientEngine.get().fillForestCache(pObject);
            }
        });
        event.registerReloadListener(new SimplePreparableReloadListener<int[]>() {
            private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/colormap/lichen.png");
            @Override
            protected int[] prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                try { return LegacyStuffWrapper.getPixels(pResourceManager, LOCATION); }
                catch (IOException ioexception) { throw new IllegalStateException("Failed to load lichen color texture", ioexception); }
            }
            @Override
            protected void apply(int[] pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                ClientEngine.get().fillLichenCache(pObject);
            }
        });
        event.registerReloadListener(new SimplePreparableReloadListener<int[]>() {
            private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/colormap/oak_leaves.png");
            @Override
            protected int[] prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                try { return LegacyStuffWrapper.getPixels(pResourceManager, LOCATION); }
                catch (IOException ioexception) { throw new IllegalStateException("Failed to load oak leaves color texture", ioexception);}
            }
            @Override
            protected void apply(int[] pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                ClientEngine.get().fillOakLeavesCache(pObject);
            }
        });
        event.registerReloadListener(new SimplePreparableReloadListener<int[]>() {
            private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/colormap/birch_leaves.png");
            @Override
            protected int[] prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                try { return LegacyStuffWrapper.getPixels(pResourceManager, LOCATION); }
                catch (IOException ioexception) { throw new IllegalStateException("Failed to load birch leaves color texture", ioexception);}
            }
            @Override
            protected void apply(int[] pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                ClientEngine.get().fillBirchLeavesCache(pObject);
            }
        });
        event.registerReloadListener(new SimplePreparableReloadListener<int[]>() {
            private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/colormap/jungle_leaves.png");
            @Override
            protected int[] prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                try { return LegacyStuffWrapper.getPixels(pResourceManager, LOCATION); }
                catch (IOException ioexception) { throw new IllegalStateException("Failed to load jungle leaves color texture", ioexception);}
            }
            @Override
            protected void apply(int[] pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                ClientEngine.get().fillJungleLeavesCache(pObject);
            }
        });
        event.registerReloadListener(new SimplePreparableReloadListener<int[]>() {
            private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/colormap/larch_leaves.png");
            @Override
            protected int[] prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                try { return LegacyStuffWrapper.getPixels(pResourceManager, LOCATION); }
                catch (IOException ioexception) { throw new IllegalStateException("Failed to load larch leaves color texture", ioexception);}
            }
            @Override
            protected void apply(int[] pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                ClientEngine.get().fillLarchLeavesCache(pObject);
            }
        });
        event.registerReloadListener(new SimplePreparableReloadListener<int[]>() {
            private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/colormap/maple_leaves.png");
            @Override
            protected int[] prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                try { return LegacyStuffWrapper.getPixels(pResourceManager, LOCATION); }
                catch (IOException ioexception) { throw new IllegalStateException("Failed to load maple leaves color texture", ioexception);}
            }
            @Override
            protected void apply(int[] pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                ClientEngine.get().fillMapleLeavesCache(pObject);
            }
        });
        event.registerReloadListener(new SimplePreparableReloadListener<int[]>() {
            private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/colormap/willow_leaves.png");
            @Override
            protected int[] prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                try { return LegacyStuffWrapper.getPixels(pResourceManager, LOCATION); }
                catch (IOException ioexception) { throw new IllegalStateException("Failed to load willow leaves color texture", ioexception);}
            }
            @Override
            protected void apply(int[] pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                ClientEngine.get().fillWillowLeavesCache(pObject);
            }
        });
        event.registerReloadListener(new SimplePreparableReloadListener<int[]>() {
            private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/colormap/acacia_leaves.png");
            @Override
            protected int[] prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                try { return LegacyStuffWrapper.getPixels(pResourceManager, LOCATION); }
                catch (IOException ioexception) { throw new IllegalStateException("Failed to load acacia leaves color texture", ioexception);}
            }
            @Override
            protected void apply(int[] pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                ClientEngine.get().fillAcaciaLeavesCache(pObject);
            }
        });
        event.registerReloadListener(new SimplePreparableReloadListener<int[]>() {
            private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/colormap/caedtar_leaves.png");
            @Override
            protected int[] prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                try { return LegacyStuffWrapper.getPixels(pResourceManager, LOCATION); }
                catch (IOException ioexception) { throw new IllegalStateException("Failed to load caedtar leaves color texture", ioexception);}
            }
            @Override
            protected void apply(int[] pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
                ClientEngine.get().fillCaedtarLeavesCache(pObject);
            }
        });
    }
}
