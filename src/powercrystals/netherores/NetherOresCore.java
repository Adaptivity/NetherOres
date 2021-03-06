package powercrystals.netherores;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

//import static cofh.core.CoFHProps.VERSION;

//import cofh.mod.BaseMod;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
//import cpw.mods.fml.common.registry.TickRegistry;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;

import powercrystals.netherores.entity.EntityArmedOre;
import powercrystals.netherores.entity.EntityHellfish;
import powercrystals.netherores.net.INetherOresProxy;
import powercrystals.netherores.ores.BlockNetherOres;
import powercrystals.netherores.ores.BlockNetherOverrideOre;
import powercrystals.netherores.ores.ItemBlockNetherOre;
import powercrystals.netherores.ores.Ores;
import powercrystals.netherores.world.BlockHellfish;
import powercrystals.netherores.world.NetherOresWorldGenHandler;

@Mod(modid = NetherOresCore.modId, name = NetherOresCore.modName, version = NetherOresCore.version,
dependencies = "required-after:CoFHCore@[2.0.1.0,);before:ThermalExpansion")
public class NetherOresCore// extends BaseMod ^
{
	public static final String modId = "NetherOres";
	public static final String version = "1.7.2R2.3.0B1";
	public static final String modName = "Nether Ores";
	
	public static final String mobTextureFolder = "netherores:textures/mob/";

	public static Block[] blockNetherOres = new Block[(Ores.values().length + 15) / 16];
	public static Block blockHellfish;

	public static Property explosionPower;
	public static Property explosionProbability;
	public static Property enableExplosions;
	public static Property enableExplosionChainReactions;
	public static Property enableAngryPigmen;
	public static Property enableHellfish;
	public static Property enableStandardFurnaceRecipes;
	public static Property enableMaceratorRecipes;
	public static Property enablePulverizerRecipes;
	public static Property enableInductionSmelterRecipes;
	public static Property enableGrinderRecipes;
	public static Property forceOreSpawn;
	public static Property worldGenAllDimensions;
	public static Property enableWorldGen;
	public static Property enableHellQuartz;
	public static Property silkyStopsPigmen;
	public static Property hellFishPerChunk;
	public static Property hellFishPerGroup;
	public static Property hellFishMinY;
	public static Property hellFishMaxY;
	public static Property enableMobsAngerPigmen;
	
	@SidedProxy(clientSide = "powercrystals.netherores.net.ClientProxy", serverSide="powercrystals.netherores.net.ServerProxy")
	public static INetherOresProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		//setConfigFolderBase(evt.getModConfigurationDirectory());
		
		//loadConfig(getCommonConfig());
		
		//extractLang(new String[] {  "en_US", "es_AR", "es_ES", "es_MX", "es_UY", "es_VE", "de_DE",
		//							"ru_RU", "sv_SE" });
		//loadLang();
	}

	@EventHandler
	public void load(FMLInitializationEvent evt)
	{
		for (int i = 0, e = blockNetherOres.length; i < e; ++i)
		{
			Block b = blockNetherOres[i] = new BlockNetherOres(i);
			GameRegistry.registerBlock(b, ItemBlockNetherOre.class, b.getUnlocalizedName());
		}
		blockHellfish = new BlockHellfish();
		GameRegistry.registerBlock(blockHellfish, ItemBlock.class, "netherOresBlockHellfish", "Minecraft");
		GameRegistry.registerCustomItemStack("netherOresBlockHellfish", new ItemStack(blockHellfish));
		GameRegistry.registerWorldGenerator(new NetherOresWorldGenHandler(), 10);
		if (enableHellQuartz.getBoolean(true))
		{
			Block quartz = new BlockNetherOverrideOre(Blocks.quartz_ore);
			Blocks.quartz_ore = quartz;
			Block.blockRegistry.addObject(153, "quartz_ore", quartz);
		}
		
		for(Ores o : Ores.values())
		{
			o.load();
		}
		
		EntityRegistry.registerModEntity(EntityArmedOre.class, "netherOresArmedOre", 0, this, 80, 5, false);
		EntityRegistry.registerModEntity(EntityHellfish.class, "netherOresHellfish", 1, this, 160, 5, true);
		
		proxy.load();

		//TickRegistry.registerScheduledTickHandler(new UpdateManager(this), Side.CLIENT);
	}
	
	@EventHandler
	public void postInit(FMLInterModComms.IMCEvent e)
	{
		Ores.Coal    .registerSmelting(new ItemStack(Blocks.coal_ore));
		Ores.Gold    .registerSmelting(new ItemStack(Blocks.gold_ore));
		Ores.Iron    .registerSmelting(new ItemStack(Blocks.iron_ore));
		Ores.Lapis   .registerSmelting(new ItemStack(Blocks.lapis_ore));
		Ores.Diamond .registerSmelting(new ItemStack(Blocks.diamond_ore));
		Ores.Emerald .registerSmelting(new ItemStack(Blocks.emerald_ore));
		Ores.Redstone.registerSmelting(new ItemStack(Blocks.redstone_ore));
		
		Ores.Coal    .registerMacerator(new ItemStack(Items.coal));
		Ores.Diamond .registerMacerator(new ItemStack(Items.diamond));
		Ores.Emerald .registerMacerator(new ItemStack(Items.emerald));
		Ores.Redstone.registerMacerator(new ItemStack(Items.redstone));
		Ores.Lapis   .registerMacerator(new ItemStack(Items.dye, 1, 4));
		
		for(Ores ore : Ores.values())
		{
			String oreName;
			oreName = ore.getOreName();   // Ore
			if (OreDictionary.getOres(oreName).size() > 0)
				registerOreDictSmelt(ore, oreName, OreDictionary.getOres(oreName).get(0));
			oreName = ore.getDustName(); // Dust
			if (OreDictionary.getOres(oreName).size() > 0)
				registerOreDictDust(ore, oreName, OreDictionary.getOres(oreName).get(0));
			oreName = ore.getAltName(); // Gem
			if (OreDictionary.getOres(oreName).size() > 0)
				registerOreDictGem(ore, oreName, OreDictionary.getOres(oreName).get(0));
		}
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static Block getOreBlock(int index)
	{
		if (index >= 0 && index < blockNetherOres.length)
			return blockNetherOres[index];
		return null;
	}

	private void loadConfig(File f)
	{
		Configuration c = new Configuration(f);
		c.load();
		
		explosionPower = c.get(CATEGORY_GENERAL, "ExplosionPower", 2);
		explosionPower.comment = "How powerful an explosion will be. Creepers are 3, TNT is 4, electrified creepers are 6. This affects both the ability of the explosion to punch through blocks as well as the blast radius.";
		explosionProbability = c.get(CATEGORY_GENERAL, "ExplosionProbability", 75);
		explosionProbability.comment = "The likelyhood an adjacent netherore will turn into an armed ore when one is mined. Percent chance out of 1000 (lower is less likely).";
		enableExplosions = c.get(CATEGORY_GENERAL, "ExplosionEnable", true);
		enableExplosions.comment = "NetherOres have a chance to explode when mined if true.";
		enableExplosionChainReactions = c.get(CATEGORY_GENERAL, "ExplosionChainReactEnable", true);
		enableExplosionChainReactions.comment = "NetherOre explosions can trigger more explosions if true. Does nothing if ExplosionEnable is false.";
		enableAngryPigmen = c.get(CATEGORY_GENERAL, "AngryPigmenEnable", true);
		enableAngryPigmen.comment = "If true, when NetherOres are mined, nearby pigmen become angry to the player.";
		silkyStopsPigmen = c.get(CATEGORY_GENERAL, "SilkyAngryPigmenEnable", false);
		silkyStopsPigmen.comment = "If true, when NetherOres are mined with Silk Touch, nearby pigmen become angry to the player.";
		enableMobsAngerPigmen = c.get(CATEGORY_GENERAL, "MobsAngerPigmen", true);
		enableMobsAngerPigmen.comment = "If true, any entity not a player exploding a NetherOre will anger nearby pigmen. This only accounts for exploding, entities breaking the blocks normally will still anger pigmen.";
		
		enableStandardFurnaceRecipes = c.get("Processing.Enable", "StandardFurnaceRecipes", true);
		enableStandardFurnaceRecipes.comment = "Set this to false to remove the standard furnace recipes (ie, nether iron ore -> normal iron ore).";
		enableMaceratorRecipes = c.get("Processing.Enable", "MaceratorRecipes", true);
		enableMaceratorRecipes.comment = "Set this to false to remove the IC2 Macerator recipes (ie, nether iron ore -> 4x iron dust).";
		enablePulverizerRecipes = c.get("Processing.Enable", "PulverizerRecipes", true);
		enablePulverizerRecipes.comment = "Set this to false to remove the TE Pulvierzer recipes (ie, nether iron ore -> 4x iron dust).";
		enableInductionSmelterRecipes = c.get("Processing.Enable", "InductionSmelterRecipes", true);
		enableInductionSmelterRecipes.comment = "Set this to false to remove the TE Induction Smelter recipes (ie, nether iron ore -> 2x normal iron ore).";
		enableGrinderRecipes = c.get("Processing.Enable", "GrinderRecipes", true);
		enableGrinderRecipes.comment = "Set this to false to remove the AE Grind Stone recipes (ie, nether iron ore -> 4x iron dust).";
		
		forceOreSpawn = c.get("WorldGen.Enable", "ForceOreSpawn", false);
		forceOreSpawn.comment = "If true, will spawn nether ores regardless of if a furnace or macerator recipe was found. If false, at least one of those two must be found to spawn the ore.";
		worldGenAllDimensions = c.get("WorldGen.Enable", "AllDimensionWorldGen", false);
		worldGenAllDimensions.comment = "If true, Nether Ores worldgen will run in all dimensions instead of just the Nether. It will still require netherrack to place ores.";
		enableWorldGen = c.get("WorldGen.Enable", "OreGen", true);
		enableWorldGen.comment = "If true, Nether Ores oregen will run and places ores in the world where appropriate. Only disable this if you intend to use the ores with a custom ore generator. (overrides per-ore forcing; hellfish still generate if enabled)";
		enableHellQuartz = c.get("WorldGen.Enable", "OverrideNetherQuartz", true);
		enableHellQuartz.comment = "If true, Nether Quartz ore will be a NetherOre and will follow the same rules as all other NetherOres.";
		
		hellFishPerChunk = c.get("WorldGen.HellFish", "GroupsPerChunk", 9);
		hellFishPerChunk.comment = "The maximum number of hellfish veins per chunk.";
		hellFishPerGroup = c.get("WorldGen.HellFish", "BlocksPerGroup", 12);
		hellFishPerGroup.comment = "The maximum number of hellfish blocks per vein.";
		enableHellfish = c.get("WorldGen.HellFish", "Enable", true);
		enableHellfish.comment = "If true, Hellfish will spawn in the Nether. Note that setting this false will not kill active Hellfish mobs.";
		hellFishMinY = c.get("WorldGen.HellFish", "MinY", 1);
		hellFishMaxY = c.get("WorldGen.HellFish", "MaxY", 127);

		for(Ores o : Ores.values())
		{
			o.loadConfig(c);
		}
		
		c.save();
	}

	@SubscribeEvent
	public void registerOreEvent(OreRegisterEvent event)
	{
		registerOreDictionaryEntry(event.Name, event.Ore);
	}
	
	private void registerOreDictionaryEntry(String oreName, ItemStack stack)
	{
		for(Ores ore : Ores.values())
		{
			registerOreDictSmelt(ore, oreName, stack);
			registerOreDictDust(ore, oreName, stack);
			registerOreDictGem(ore, oreName, stack);
		}
	}
	
	private void registerOreDictSmelt(Ores ore, String oreName, ItemStack stack)
	{
		if (!ore.isRegisteredSmelting() && ore.getOreName().equals(oreName))
			ore.registerSmelting(stack);
	}
	
	private void registerOreDictDust(Ores ore, String oreName, ItemStack stack)
	{
		if (!ore.isRegisteredMacerator() && ore.getDustName().equals(oreName))
			ore.registerMacerator(stack);
	}
	
	private void registerOreDictGem(Ores ore, String oreName, ItemStack stack)
	{
		if (!ore.isRegisteredMacerator() && ore.getAltName().equals(oreName))
			ore.registerMacerator(stack);
	}

	//@Override
	public String getModId()
	{
		return modId;
	}

	//@Override
	public String getModName()
	{
		return modName;
	}

	//@Override
	public String getModVersion()
	{
		return version;
	}
}
