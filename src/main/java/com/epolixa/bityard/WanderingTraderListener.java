package com.epolixa.bityard;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;

public class WanderingTraderListener implements Listener {

    private final Bityard bityard;

    private final static Random random = new Random(System.currentTimeMillis());

    private ArrayList<Material> materialBlacklist;
    private ArrayList<Material> enchantableMaterials;
    private ArrayList<Material> potionMaterials;
    private ArrayList<Material> previousMaterials;
    private ArrayList<Material> expensiveMaterials;

    private final int NUM_OFFERS;
    private final int MIN_PRICE;
    private final int MAX_PRICE;
    private final int MIN_AMOUNT;
    private final int MAX_AMOUNT;
    private final int MIN_USES;
    private final int MAX_USES;
    private final int BONUS_PRICE;

    public WanderingTraderListener(Bityard bityard) {
        this.bityard = bityard;

        buildMaterialBlacklist();
        buildExpensiveMaterials();
        buildEnchantableMaterials();
        buildPotionMaterials();
        this.previousMaterials = new ArrayList<Material>();

        this.NUM_OFFERS = 6;
        this.MIN_PRICE = 1;
        this.MAX_PRICE = 6;
        this.MIN_AMOUNT = 1;
        this.MAX_AMOUNT = 8;
        this.MIN_USES = 4;
        this.MAX_USES = 16;
        this.BONUS_PRICE = 32 - this.MAX_PRICE;
    }

    // Intercept wandering trader when he spawns and build trades
    @EventHandler
    public void onWanderingTraderSpawn(CreatureSpawnEvent event)
    {
        try
        {
            if (event.getEntityType() == EntityType.WANDERING_TRADER) {
                WanderingTrader wanderingTrader = (WanderingTrader) event.getEntity();
                ArrayList<MerchantRecipe> newRecipes = new ArrayList<MerchantRecipe>(); // recipes to be added to merchant
                ArrayList<Material> pickedMaterials = new ArrayList<Material>(); // materials already added
                // Add at least one material from expensive items list first
                Material expensiveMaterial = this.expensiveMaterials.get(random.nextInt(expensiveMaterials.size()));
                pickedMaterials.add(expensiveMaterial);
                newRecipes.add(buildRecipe(expensiveMaterial));
                for (int i = 0; i < NUM_OFFERS - 1; i++) {
                    // next selected random material
                    Material randomMaterial = Material.values()[random.nextInt(Material.values().length)];
                    // check if material is illegal or has been added
                    if (this.materialBlacklist.contains(randomMaterial) || pickedMaterials.contains(randomMaterial) || this.previousMaterials.contains(randomMaterial)) {
                        // skip and try again
                        i--;
                    } else {
                        // add recipe
                        pickedMaterials.add(randomMaterial);
                        newRecipes.add(buildRecipe(randomMaterial));
                    }
                }
                wanderingTrader.setRecipes(newRecipes);
                this.previousMaterials = pickedMaterials;
                // print wandering trader offers
                bityard.log("Wandering Trader appeared at " + bityard.locationXYZ(wanderingTrader.getLocation()) + " with the following offers:");
                for (MerchantRecipe offer : wanderingTrader.getRecipes()) {
                    bityard.log("  - " + offer.getMaxUses() + " Offerings of " + offer.getResult().getAmount() + " " + offer.getResult().getType().name() + " for " + offer.getIngredients().get(0).getAmount() + " Emeralds");
                }
            }
        }
        catch (Exception e)
        {
            bityard.log("caught error: ");
            e.printStackTrace();
        }
    }

    // Prepares a recipe to be added to the list
    private MerchantRecipe buildRecipe(Material material) {
        MerchantRecipe recipe;
        ItemStack sellItem = new ItemStack(material);
        if (material == Material.ENCHANTED_BOOK || (this.enchantableMaterials.contains(material) && inRange(0,1) == 1)) {
            sellItem = addRandomEnchantment(sellItem);
        }
        if (this.potionMaterials.contains(material)) {
            sellItem = addRandomEffect(sellItem);
        }
        if (this.expensiveMaterials.contains(material)) {
            sellItem.setAmount(1);
        } else {
            sellItem.setAmount(inRange(this.MIN_AMOUNT, Math.min(sellItem.getMaxStackSize(), this.MAX_AMOUNT)));
        }
        recipe = new MerchantRecipe(sellItem, inRange(this.MIN_USES, this.expensiveMaterials.contains(material) ? this.MAX_USES / 2 : this.MAX_USES));
        recipe.addIngredient(new ItemStack(Material.EMERALD, inRange(this.MIN_PRICE, this.MAX_PRICE) + (this.expensiveMaterials.contains(material) ? this.BONUS_PRICE : 0)));
        return recipe;
    }

    private ItemStack addRandomEnchantment(ItemStack item) {
        if (item.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            Enchantment chosen = Enchantment.values()[random.nextInt(Enchantment.values().length)];
            int lvl = 1 + (int) (Math.random() * ((chosen.getMaxLevel() - 1) + 1));
            meta.addStoredEnchant(chosen, lvl, false);
            item.setItemMeta(meta);
        } else {
            List<Enchantment> possible = new ArrayList<Enchantment>();
            for (Enchantment ench : Enchantment.values()) {
                if (ench.canEnchantItem(item)) {
                    possible.add(ench);
                }
            }
            if (possible.size() >= 1) {
                Collections.shuffle(possible);
                Enchantment chosen = possible.get(0);
                int lvl = 1 + (int) (Math.random() * ((chosen.getMaxLevel() - 1) + 1));
                item.addEnchantment(chosen, lvl);
            }
        }
        return item;
    }

    private ItemStack addRandomEffect(ItemStack item) {
        List<PotionType> potions = Arrays.asList(PotionType.values());

        List<PotionType> potionBlacklist = new ArrayList<PotionType>();
        potionBlacklist.add(PotionType.AWKWARD);
        potionBlacklist.add(PotionType.MUNDANE);
        potionBlacklist.add(PotionType.THICK);
        potionBlacklist.add(PotionType.UNCRAFTABLE);

        PotionType chosen;
        do {
            Collections.shuffle(potions);
            chosen = potions.get(0);
        } while (potionBlacklist.contains(chosen));
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        boolean extended = random.nextBoolean();
        meta.setBasePotionData(new PotionData(chosen, extended, extended ? false : random.nextBoolean()));
        item.setItemMeta(meta);
        return item;
    }

    // return a random value between two values
    private int inRange(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    private void buildEnchantableMaterials() {
        this.enchantableMaterials = new ArrayList<Material>();

        enchantableMaterials.add(Material.ENCHANTED_BOOK);
        enchantableMaterials.add(Material.BOW);
        enchantableMaterials.add(Material.LEATHER_BOOTS);
        enchantableMaterials.add(Material.LEATHER_CHESTPLATE);
        enchantableMaterials.add(Material.LEATHER_HELMET);
        enchantableMaterials.add(Material.LEATHER_LEGGINGS);
        enchantableMaterials.add(Material.WOODEN_AXE);
        enchantableMaterials.add(Material.WOODEN_HOE);
        enchantableMaterials.add(Material.WOODEN_PICKAXE);
        enchantableMaterials.add(Material.WOODEN_SHOVEL);
        enchantableMaterials.add(Material.WOODEN_SWORD);
        enchantableMaterials.add(Material.IRON_BOOTS);
        enchantableMaterials.add(Material.IRON_CHESTPLATE);
        enchantableMaterials.add(Material.IRON_HELMET);
        enchantableMaterials.add(Material.IRON_LEGGINGS);
        enchantableMaterials.add(Material.IRON_AXE);
        enchantableMaterials.add(Material.IRON_HOE);
        enchantableMaterials.add(Material.IRON_PICKAXE);
        enchantableMaterials.add(Material.IRON_SHOVEL);
        enchantableMaterials.add(Material.IRON_SWORD);
        enchantableMaterials.add(Material.CHAINMAIL_BOOTS);
        enchantableMaterials.add(Material.CHAINMAIL_CHESTPLATE);
        enchantableMaterials.add(Material.CHAINMAIL_HELMET);
        enchantableMaterials.add(Material.CHAINMAIL_LEGGINGS);
        enchantableMaterials.add(Material.GOLDEN_BOOTS);
        enchantableMaterials.add(Material.GOLDEN_CHESTPLATE);
        enchantableMaterials.add(Material.GOLDEN_HELMET);
        enchantableMaterials.add(Material.GOLDEN_LEGGINGS);
        enchantableMaterials.add(Material.GOLDEN_AXE);
        enchantableMaterials.add(Material.GOLDEN_HOE);
        enchantableMaterials.add(Material.GOLDEN_PICKAXE);
        enchantableMaterials.add(Material.GOLDEN_SHOVEL);
        enchantableMaterials.add(Material.GOLDEN_SWORD);
        enchantableMaterials.add(Material.DIAMOND_BOOTS);
        enchantableMaterials.add(Material.DIAMOND_CHESTPLATE);
        enchantableMaterials.add(Material.DIAMOND_HELMET);
        enchantableMaterials.add(Material.DIAMOND_LEGGINGS);
        enchantableMaterials.add(Material.DIAMOND_AXE);
        enchantableMaterials.add(Material.DIAMOND_HOE);
        enchantableMaterials.add(Material.DIAMOND_PICKAXE);
        enchantableMaterials.add(Material.DIAMOND_SHOVEL);
        enchantableMaterials.add(Material.DIAMOND_SWORD);
        enchantableMaterials.add(Material.SHIELD);
        enchantableMaterials.add(Material.TRIDENT);
        enchantableMaterials.add(Material.CROSSBOW);
        enchantableMaterials.add(Material.FISHING_ROD);
    }

    private void buildPotionMaterials() {
        this.potionMaterials = new ArrayList<Material>();

        potionMaterials.add(Material.POTION);
        potionMaterials.add(Material.LINGERING_POTION);
        potionMaterials.add(Material.SPLASH_POTION);
        potionMaterials.add(Material.TIPPED_ARROW);
    }

    private void buildExpensiveMaterials() {
        this.expensiveMaterials = new ArrayList<Material>();

        // DIAMOND GEAR
        expensiveMaterials.add(Material.DIAMOND_SWORD);
        expensiveMaterials.add(Material.DIAMOND_PICKAXE);
        expensiveMaterials.add(Material.DIAMOND_AXE);
        expensiveMaterials.add(Material.DIAMOND_SHOVEL);
        expensiveMaterials.add(Material.DIAMOND_HOE);
        expensiveMaterials.add(Material.DIAMOND_LEGGINGS);
        expensiveMaterials.add(Material.DIAMOND_BOOTS);
        expensiveMaterials.add(Material.DIAMOND_HELMET);
        expensiveMaterials.add(Material.DIAMOND_CHESTPLATE);
        expensiveMaterials.add(Material.DIAMOND_HORSE_ARMOR);

        // MISC GEAR
        expensiveMaterials.add(Material.BOW);
        expensiveMaterials.add(Material.FISHING_ROD);
        expensiveMaterials.add(Material.TRIDENT);
        expensiveMaterials.add(Material.CROSSBOW);
        expensiveMaterials.add(Material.SHIELD);
        expensiveMaterials.add(Material.ENCHANTED_BOOK);
        expensiveMaterials.add(Material.SPECTRAL_ARROW);

        // BANNER PATTERNS
        expensiveMaterials.add(Material.MOJANG_BANNER_PATTERN);
        expensiveMaterials.add(Material.CREEPER_BANNER_PATTERN);
        expensiveMaterials.add(Material.FLOWER_BANNER_PATTERN);
        expensiveMaterials.add(Material.GLOBE_BANNER_PATTERN);
        expensiveMaterials.add(Material.SKULL_BANNER_PATTERN);
        expensiveMaterials.add(Material.PIGLIN_BANNER_PATTERN);

        // POTIONS
        expensiveMaterials.add(Material.POTION);
        expensiveMaterials.add(Material.LINGERING_POTION);
        expensiveMaterials.add(Material.SPLASH_POTION);
        expensiveMaterials.add(Material.TIPPED_ARROW);

        // SPAWN EGGS
        expensiveMaterials.add(Material.ZOMBIE_SPAWN_EGG);
        expensiveMaterials.add(Material.SKELETON_SPAWN_EGG);
        expensiveMaterials.add(Material.SPIDER_SPAWN_EGG);
        expensiveMaterials.add(Material.CAVE_SPIDER_SPAWN_EGG);
        expensiveMaterials.add(Material.SILVERFISH_SPAWN_EGG);
        expensiveMaterials.add(Material.BLAZE_SPAWN_EGG);
        expensiveMaterials.add(Material.MAGMA_CUBE_SPAWN_EGG);

        // HEADS
        expensiveMaterials.add(Material.ZOMBIE_HEAD);
        expensiveMaterials.add(Material.SKELETON_SKULL);
        expensiveMaterials.add(Material.CREEPER_HEAD);

        // RESOURCES
        expensiveMaterials.add(Material.SHULKER_SHELL);
        expensiveMaterials.add(Material.NAUTILUS_SHELL);
        expensiveMaterials.add(Material.DRAGON_BREATH);
        expensiveMaterials.add(Material.SCUTE);
        expensiveMaterials.add(Material.ENDER_EYE);
        expensiveMaterials.add(Material.EXPERIENCE_BOTTLE);
        expensiveMaterials.add(Material.IRON_BLOCK);
        expensiveMaterials.add(Material.GOLD_BLOCK);
        expensiveMaterials.add(Material.REDSTONE_BLOCK);
        expensiveMaterials.add(Material.COAL_BLOCK);
        expensiveMaterials.add(Material.DIAMOND_BLOCK);
        expensiveMaterials.add(Material.ENCHANTED_GOLDEN_APPLE);
        expensiveMaterials.add(Material.TURTLE_EGG);

        // 1.16 NETHERITE SCRAPS, ETC
        expensiveMaterials.add(Material.ANCIENT_DEBRIS);
        expensiveMaterials.add(Material.NETHERITE_SCRAP);
        expensiveMaterials.add(Material.CRYING_OBSIDIAN);

    }

    private void buildMaterialBlacklist() {
        this.materialBlacklist = new ArrayList<Material>();

        // VERY ILLEGAL - Game-breaking/technical
        materialBlacklist.add(Material.AIR);
        materialBlacklist.add(Material.BARRIER);
        materialBlacklist.add(Material.BEDROCK);
        materialBlacklist.add(Material.BUBBLE_COLUMN);
        materialBlacklist.add(Material.CAVE_AIR);
        materialBlacklist.add(Material.CHAIN_COMMAND_BLOCK);
        materialBlacklist.add(Material.CHIPPED_ANVIL);
        materialBlacklist.add(Material.COMMAND_BLOCK);
        materialBlacklist.add(Material.COMMAND_BLOCK_MINECART);
        materialBlacklist.add(Material.DAMAGED_ANVIL);
        materialBlacklist.add(Material.DEBUG_STICK);
        materialBlacklist.add(Material.END_GATEWAY);
        materialBlacklist.add(Material.END_PORTAL);
        materialBlacklist.add(Material.END_PORTAL_FRAME);
        materialBlacklist.add(Material.FILLED_MAP);
        materialBlacklist.add(Material.FIRE);
        materialBlacklist.add(Material.SOUL_FIRE);
        materialBlacklist.add(Material.FIREWORK_STAR);
        materialBlacklist.add(Material.FROSTED_ICE);
        materialBlacklist.add(Material.INFESTED_CHISELED_STONE_BRICKS);
        materialBlacklist.add(Material.INFESTED_COBBLESTONE);
        materialBlacklist.add(Material.INFESTED_CRACKED_STONE_BRICKS);
        materialBlacklist.add(Material.INFESTED_MOSSY_STONE_BRICKS);
        materialBlacklist.add(Material.INFESTED_STONE);
        materialBlacklist.add(Material.INFESTED_STONE_BRICKS);
        materialBlacklist.add(Material.JIGSAW);
        materialBlacklist.add(Material.KNOWLEDGE_BOOK);
        materialBlacklist.add(Material.LAVA);
        materialBlacklist.add(Material.MOVING_PISTON);
        materialBlacklist.add(Material.NETHER_PORTAL);
        materialBlacklist.add(Material.PISTON_HEAD);
        materialBlacklist.add(Material.REDSTONE_WIRE);
        materialBlacklist.add(Material.REPEATING_COMMAND_BLOCK);
        materialBlacklist.add(Material.SNOW);
        materialBlacklist.add(Material.SPAWNER);
        materialBlacklist.add(Material.STRUCTURE_BLOCK);
        materialBlacklist.add(Material.STRUCTURE_VOID);
        materialBlacklist.add(Material.VOID_AIR);
        materialBlacklist.add(Material.WATER);
        materialBlacklist.add(Material.WET_SPONGE);
        materialBlacklist.add(Material.TRIPWIRE);
        materialBlacklist.add(Material.WRITTEN_BOOK);
        materialBlacklist.add(Material.SUSPICIOUS_STEW);
        materialBlacklist.add(Material.EMERALD);
        materialBlacklist.add(Material.EMERALD_BLOCK);
        materialBlacklist.add(Material.EMERALD_ORE);
        materialBlacklist.add(Material.PLAYER_HEAD);

        // Too OP, valuable, rare, collectibles
        materialBlacklist.add(Material.BEACON);
        materialBlacklist.add(Material.SHULKER_BOX);
        materialBlacklist.add(Material.BLACK_SHULKER_BOX);
        materialBlacklist.add(Material.BLUE_SHULKER_BOX);
        materialBlacklist.add(Material.BROWN_SHULKER_BOX);
        materialBlacklist.add(Material.CYAN_SHULKER_BOX);
        materialBlacklist.add(Material.GRAY_SHULKER_BOX);
        materialBlacklist.add(Material.GREEN_SHULKER_BOX);
        materialBlacklist.add(Material.LIGHT_BLUE_SHULKER_BOX);
        materialBlacklist.add(Material.LIGHT_GRAY_SHULKER_BOX);
        materialBlacklist.add(Material.LIME_SHULKER_BOX);
        materialBlacklist.add(Material.MAGENTA_SHULKER_BOX);
        materialBlacklist.add(Material.ORANGE_SHULKER_BOX);
        materialBlacklist.add(Material.PINK_SHULKER_BOX);
        materialBlacklist.add(Material.PURPLE_SHULKER_BOX);
        materialBlacklist.add(Material.RED_SHULKER_BOX);
        materialBlacklist.add(Material.WHITE_SHULKER_BOX);
        materialBlacklist.add(Material.YELLOW_SHULKER_BOX);
        materialBlacklist.add(Material.CONDUIT);
        materialBlacklist.add(Material.DRAGON_EGG);
        materialBlacklist.add(Material.DRAGON_HEAD);
        materialBlacklist.add(Material.WITHER_SKELETON_SKULL);
        materialBlacklist.add(Material.ELYTRA);
        materialBlacklist.add(Material.END_CRYSTAL);
        materialBlacklist.add(Material.HEART_OF_THE_SEA);
        materialBlacklist.add(Material.MUSIC_DISC_11);
        materialBlacklist.add(Material.MUSIC_DISC_13);
        materialBlacklist.add(Material.MUSIC_DISC_BLOCKS);
        materialBlacklist.add(Material.MUSIC_DISC_CAT);
        materialBlacklist.add(Material.MUSIC_DISC_CHIRP);
        materialBlacklist.add(Material.MUSIC_DISC_FAR);
        materialBlacklist.add(Material.MUSIC_DISC_MALL);
        materialBlacklist.add(Material.MUSIC_DISC_MELLOHI);
        materialBlacklist.add(Material.MUSIC_DISC_STAL);
        materialBlacklist.add(Material.MUSIC_DISC_STRAD);
        materialBlacklist.add(Material.MUSIC_DISC_WAIT);
        materialBlacklist.add(Material.MUSIC_DISC_WARD);
        materialBlacklist.add(Material.MUSIC_DISC_PIGSTEP);
        materialBlacklist.add(Material.NETHER_STAR);
        materialBlacklist.add(Material.TOTEM_OF_UNDYING);
        materialBlacklist.add(Material.TURTLE_HELMET);
        materialBlacklist.add(Material.WITHER_ROSE);
        materialBlacklist.add(Material.LODESTONE);
        materialBlacklist.add(Material.RESPAWN_ANCHOR);
        materialBlacklist.add(Material.NETHERITE_INGOT);
        materialBlacklist.add(Material.NETHERITE_BLOCK);
        materialBlacklist.add(Material.NETHERITE_BOOTS);
        materialBlacklist.add(Material.NETHERITE_LEGGINGS);
        materialBlacklist.add(Material.NETHERITE_CHESTPLATE);
        materialBlacklist.add(Material.NETHERITE_HELMET);
        materialBlacklist.add(Material.NETHERITE_SWORD);
        materialBlacklist.add(Material.NETHERITE_PICKAXE);
        materialBlacklist.add(Material.NETHERITE_AXE);
        materialBlacklist.add(Material.NETHERITE_SHOVEL);
        materialBlacklist.add(Material.NETHERITE_HOE);

        // Banned Spawn Eggs
        materialBlacklist.add(Material.BAT_SPAWN_EGG);
        materialBlacklist.add(Material.BEE_SPAWN_EGG);
        materialBlacklist.add(Material.CAT_SPAWN_EGG);
        materialBlacklist.add(Material.CHICKEN_SPAWN_EGG);
        materialBlacklist.add(Material.COD_SPAWN_EGG);
        materialBlacklist.add(Material.COW_SPAWN_EGG);
        materialBlacklist.add(Material.CREEPER_SPAWN_EGG);
        materialBlacklist.add(Material.DOLPHIN_SPAWN_EGG);
        materialBlacklist.add(Material.DONKEY_SPAWN_EGG);
        materialBlacklist.add(Material.DROWNED_SPAWN_EGG);
        materialBlacklist.add(Material.ELDER_GUARDIAN_SPAWN_EGG);
        materialBlacklist.add(Material.ENDERMAN_SPAWN_EGG);
        materialBlacklist.add(Material.ENDERMITE_SPAWN_EGG);
        materialBlacklist.add(Material.EVOKER_SPAWN_EGG);
        materialBlacklist.add(Material.FOX_SPAWN_EGG);
        materialBlacklist.add(Material.GHAST_SPAWN_EGG);
        materialBlacklist.add(Material.GUARDIAN_SPAWN_EGG);
        materialBlacklist.add(Material.HORSE_SPAWN_EGG);
        materialBlacklist.add(Material.HUSK_SPAWN_EGG);
        materialBlacklist.add(Material.LLAMA_SPAWN_EGG);
        materialBlacklist.add(Material.MOOSHROOM_SPAWN_EGG);
        materialBlacklist.add(Material.MULE_SPAWN_EGG);
        materialBlacklist.add(Material.OCELOT_SPAWN_EGG);
        materialBlacklist.add(Material.PANDA_SPAWN_EGG);
        materialBlacklist.add(Material.PARROT_SPAWN_EGG);
        materialBlacklist.add(Material.PHANTOM_SPAWN_EGG);
        materialBlacklist.add(Material.PIG_SPAWN_EGG);
        materialBlacklist.add(Material.PILLAGER_SPAWN_EGG);
        materialBlacklist.add(Material.POLAR_BEAR_SPAWN_EGG);
        materialBlacklist.add(Material.PUFFERFISH_SPAWN_EGG);
        materialBlacklist.add(Material.RABBIT_SPAWN_EGG);
        materialBlacklist.add(Material.RAVAGER_SPAWN_EGG);
        materialBlacklist.add(Material.SALMON_SPAWN_EGG);
        materialBlacklist.add(Material.SHEEP_SPAWN_EGG);
        materialBlacklist.add(Material.SHULKER_SPAWN_EGG);
        materialBlacklist.add(Material.SKELETON_HORSE_SPAWN_EGG);
        materialBlacklist.add(Material.SLIME_SPAWN_EGG);
        materialBlacklist.add(Material.SQUID_SPAWN_EGG);
        materialBlacklist.add(Material.STRAY_SPAWN_EGG);
        materialBlacklist.add(Material.TRADER_LLAMA_SPAWN_EGG);
        materialBlacklist.add(Material.TROPICAL_FISH_SPAWN_EGG);
        materialBlacklist.add(Material.TURTLE_SPAWN_EGG);
        materialBlacklist.add(Material.VEX_SPAWN_EGG);
        materialBlacklist.add(Material.VILLAGER_SPAWN_EGG);
        materialBlacklist.add(Material.VINDICATOR_SPAWN_EGG);
        materialBlacklist.add(Material.WANDERING_TRADER_SPAWN_EGG);
        materialBlacklist.add(Material.WITCH_SPAWN_EGG);
        materialBlacklist.add(Material.WITHER_SKELETON_SPAWN_EGG);
        materialBlacklist.add(Material.WOLF_SPAWN_EGG);
        materialBlacklist.add(Material.ZOMBIE_HORSE_SPAWN_EGG);
        materialBlacklist.add(Material.ZOMBIE_VILLAGER_SPAWN_EGG);
        materialBlacklist.add(Material.PIGLIN_SPAWN_EGG);
        materialBlacklist.add(Material.ZOMBIFIED_PIGLIN_SPAWN_EGG);
        materialBlacklist.add(Material.HOGLIN_SPAWN_EGG);
        materialBlacklist.add(Material.ZOGLIN_SPAWN_EGG);
        materialBlacklist.add(Material.STRIDER_SPAWN_EGG);

        // Dead coral. what's the point?
        materialBlacklist.add(Material.DEAD_BRAIN_CORAL);
        materialBlacklist.add(Material.DEAD_BRAIN_CORAL_BLOCK);
        materialBlacklist.add(Material.DEAD_BRAIN_CORAL_FAN);
        materialBlacklist.add(Material.DEAD_BRAIN_CORAL_WALL_FAN);
        materialBlacklist.add(Material.BRAIN_CORAL_WALL_FAN);
        materialBlacklist.add(Material.DEAD_BUBBLE_CORAL);
        materialBlacklist.add(Material.DEAD_BUBBLE_CORAL_BLOCK);
        materialBlacklist.add(Material.DEAD_BUBBLE_CORAL_FAN);
        materialBlacklist.add(Material.DEAD_BUBBLE_CORAL_WALL_FAN);
        materialBlacklist.add(Material.BUBBLE_CORAL_WALL_FAN);
        materialBlacklist.add(Material.DEAD_FIRE_CORAL);
        materialBlacklist.add(Material.DEAD_FIRE_CORAL_BLOCK);
        materialBlacklist.add(Material.DEAD_FIRE_CORAL_FAN);
        materialBlacklist.add(Material.DEAD_FIRE_CORAL_WALL_FAN);
        materialBlacklist.add(Material.FIRE_CORAL_WALL_FAN);
        materialBlacklist.add(Material.DEAD_HORN_CORAL);
        materialBlacklist.add(Material.DEAD_HORN_CORAL_BLOCK);
        materialBlacklist.add(Material.DEAD_HORN_CORAL_FAN);
        materialBlacklist.add(Material.DEAD_HORN_CORAL_WALL_FAN);
        materialBlacklist.add(Material.HORN_CORAL_WALL_FAN);
        materialBlacklist.add(Material.DEAD_TUBE_CORAL);
        materialBlacklist.add(Material.DEAD_TUBE_CORAL_BLOCK);
        materialBlacklist.add(Material.DEAD_TUBE_CORAL_FAN);
        materialBlacklist.add(Material.DEAD_TUBE_CORAL_WALL_FAN);
        materialBlacklist.add(Material.TUBE_CORAL_WALL_FAN);

        // Crop parts
        materialBlacklist.add(Material.ATTACHED_MELON_STEM);
        materialBlacklist.add(Material.ATTACHED_PUMPKIN_STEM);
        materialBlacklist.add(Material.BAMBOO_SAPLING);
        materialBlacklist.add(Material.BEETROOTS);
        materialBlacklist.add(Material.CHORUS_PLANT);
        materialBlacklist.add(Material.COCOA);
        materialBlacklist.add(Material.FARMLAND);
        materialBlacklist.add(Material.KELP_PLANT);
        materialBlacklist.add(Material.LARGE_FERN);
        materialBlacklist.add(Material.TALL_GRASS);
        materialBlacklist.add(Material.TALL_SEAGRASS);
        materialBlacklist.add(Material.MELON_STEM);
        materialBlacklist.add(Material.PUMPKIN_STEM);
        materialBlacklist.add(Material.POTATOES);
        materialBlacklist.add(Material.POTTED_ACACIA_SAPLING);
        materialBlacklist.add(Material.POTTED_ALLIUM);
        materialBlacklist.add(Material.POTTED_AZURE_BLUET);
        materialBlacklist.add(Material.POTTED_BAMBOO);
        materialBlacklist.add(Material.POTTED_BIRCH_SAPLING);
        materialBlacklist.add(Material.POTTED_BLUE_ORCHID);
        materialBlacklist.add(Material.POTTED_BROWN_MUSHROOM);
        materialBlacklist.add(Material.POTTED_CACTUS);
        materialBlacklist.add(Material.POTTED_CORNFLOWER);
        materialBlacklist.add(Material.POTTED_DANDELION);
        materialBlacklist.add(Material.POTTED_DARK_OAK_SAPLING);
        materialBlacklist.add(Material.POTTED_DEAD_BUSH);
        materialBlacklist.add(Material.POTTED_FERN);
        materialBlacklist.add(Material.POTTED_JUNGLE_SAPLING);
        materialBlacklist.add(Material.POTTED_LILY_OF_THE_VALLEY);
        materialBlacklist.add(Material.POTTED_OAK_SAPLING);
        materialBlacklist.add(Material.POTTED_ORANGE_TULIP);
        materialBlacklist.add(Material.POTTED_OXEYE_DAISY);
        materialBlacklist.add(Material.POTTED_PINK_TULIP);
        materialBlacklist.add(Material.POTTED_POPPY);
        materialBlacklist.add(Material.POTTED_RED_MUSHROOM);
        materialBlacklist.add(Material.POTTED_RED_TULIP);
        materialBlacklist.add(Material.POTTED_SPRUCE_SAPLING);
        materialBlacklist.add(Material.POTTED_WHITE_TULIP);
        materialBlacklist.add(Material.POTTED_WITHER_ROSE);
        materialBlacklist.add(Material.POTTED_CRIMSON_FUNGUS);
        materialBlacklist.add(Material.POTTED_CRIMSON_ROOTS);
        materialBlacklist.add(Material.POTTED_WARPED_FUNGUS);
        materialBlacklist.add(Material.POTTED_WARPED_ROOTS);
        materialBlacklist.add(Material.SWEET_BERRY_BUSH);
        materialBlacklist.add(Material.CARROTS);
        materialBlacklist.add(Material.TWISTING_VINES_PLANT);
        materialBlacklist.add(Material.WEEPING_VINES_PLANT);

        // Wall blocks
        materialBlacklist.add(Material.OAK_WALL_SIGN);
        materialBlacklist.add(Material.SPRUCE_WALL_SIGN);
        materialBlacklist.add(Material.ACACIA_WALL_SIGN);
        materialBlacklist.add(Material.BIRCH_WALL_SIGN);
        materialBlacklist.add(Material.JUNGLE_WALL_SIGN);
        materialBlacklist.add(Material.DARK_OAK_WALL_SIGN);
        materialBlacklist.add(Material.CRIMSON_WALL_SIGN);
        materialBlacklist.add(Material.WARPED_WALL_SIGN);
        materialBlacklist.add(Material.BLACK_WALL_BANNER);
        materialBlacklist.add(Material.BLUE_WALL_BANNER);
        materialBlacklist.add(Material.BROWN_WALL_BANNER);
        materialBlacklist.add(Material.CYAN_WALL_BANNER);
        materialBlacklist.add(Material.GRAY_WALL_BANNER);
        materialBlacklist.add(Material.GREEN_WALL_BANNER);
        materialBlacklist.add(Material.LIGHT_BLUE_WALL_BANNER);
        materialBlacklist.add(Material.LIGHT_GRAY_WALL_BANNER);
        materialBlacklist.add(Material.LIME_WALL_BANNER);
        materialBlacklist.add(Material.MAGENTA_WALL_BANNER);
        materialBlacklist.add(Material.ORANGE_WALL_BANNER);
        materialBlacklist.add(Material.PINK_WALL_BANNER);
        materialBlacklist.add(Material.PURPLE_WALL_BANNER);
        materialBlacklist.add(Material.RED_WALL_BANNER);
        materialBlacklist.add(Material.WHITE_WALL_BANNER);
        materialBlacklist.add(Material.YELLOW_WALL_BANNER);
        materialBlacklist.add(Material.DRAGON_WALL_HEAD);
        materialBlacklist.add(Material.CREEPER_WALL_HEAD);
        materialBlacklist.add(Material.PLAYER_WALL_HEAD);
        materialBlacklist.add(Material.ZOMBIE_WALL_HEAD);
        materialBlacklist.add(Material.SKELETON_WALL_SKULL);
        materialBlacklist.add(Material.WITHER_SKELETON_WALL_SKULL);
        materialBlacklist.add(Material.WALL_TORCH);
        materialBlacklist.add(Material.REDSTONE_WALL_TORCH);
        materialBlacklist.add(Material.SOUL_WALL_TORCH);
    }
}
