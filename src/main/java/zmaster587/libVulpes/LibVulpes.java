package zmaster587.libVulpes;


import com.google.common.collect.Lists;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.GameData;
import org.apache.logging.log4j.LogManager;
import zmaster587.libVulpes.api.LibVulpesBlocks;
import zmaster587.libVulpes.api.LibVulpesItems;
import zmaster587.libVulpes.api.material.AllowedProducts;
import zmaster587.libVulpes.api.material.MaterialRegistry;
import zmaster587.libVulpes.block.*;
import zmaster587.libVulpes.block.multiblock.BlockHatch;
import zmaster587.libVulpes.block.multiblock.BlockMultiMachineBattery;
import zmaster587.libVulpes.block.multiblock.BlockMultiblockPlaceHolder;
import zmaster587.libVulpes.common.CommonProxy;
import zmaster587.libVulpes.common.block.LibVulpesMetaBlocks;
import zmaster587.libVulpes.event.BucketHandler;
import zmaster587.libVulpes.interfaces.IRecipe;
import zmaster587.libVulpes.inventory.GuiHandler;
import zmaster587.libVulpes.items.ItemBlockMeta;
import zmaster587.libVulpes.items.ItemIngredient;
import zmaster587.libVulpes.items.ItemLinker;
import zmaster587.libVulpes.items.ItemProjector;
import zmaster587.libVulpes.network.PacketChangeKeyState;
import zmaster587.libVulpes.network.PacketEntity;
import zmaster587.libVulpes.network.PacketHandler;
import zmaster587.libVulpes.network.PacketMachine;
import zmaster587.libVulpes.recipe.RecipesMachine;
import zmaster587.libVulpes.tile.TileInventoriedPointer;
import zmaster587.libVulpes.tile.TilePointer;
import zmaster587.libVulpes.tile.TileSchematic;
import zmaster587.libVulpes.tile.energy.*;
import zmaster587.libVulpes.tile.multiblock.TileMultiBlock;
import zmaster587.libVulpes.tile.multiblock.TilePlaceholder;
import zmaster587.libVulpes.tile.multiblock.hatch.TileFluidHatch;
import zmaster587.libVulpes.tile.multiblock.hatch.TileInputHatch;
import zmaster587.libVulpes.tile.multiblock.hatch.TileOutputHatch;
import zmaster587.libVulpes.util.TeslaCapabilityProvider;
import zmaster587.libVulpes.util.XMLRecipeLoader;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Mod(modid="libvulpes", name="Vulpes library", version="0.5.0", useMetadata=true, dependencies="after:ic2;after:cofhcore;after:buildcraft|core;after:immersiveengineering")

public class LibVulpes {
	public static org.apache.logging.log4j.Logger logger = LogManager.getLogger("libVulpes");
	public static int time = 0;
	private static HashMap<Class, String> userModifiableRecipes = new HashMap<>();

	@Instance(value = "libvulpes")
	public static LibVulpes instance;

	@SidedProxy(clientSide="zmaster587.libVulpes.client.ClientProxy", serverSide="zmaster587.libVulpes.common.CommonProxy")
	public static CommonProxy proxy;

	public static CreativeTabs tabMultiblock = new CreativeTabs("multiBlock") {
		@Nonnull
		public ItemStack createIcon() {
			return new ItemStack(LibVulpesItems.itemLinker);
		}
	};

	public static CreativeTabs tabLibVulpesOres = new CreativeTabs("advancedRocketryOres") {
		@Nonnull
		public ItemStack createIcon() {
			return OreDictUnifier.get(OrePrefix.ore, Materials.Copper);
		}
	};

	public static MaterialRegistry materialRegistry = new MaterialRegistry();

	public static void registerRecipeHandler(Class clazz, String fileName) {
		userModifiableRecipes.put(clazz, fileName);
	}
	
	public LibVulpes()
    {
        MinecraftForge.EVENT_BUS.register(this);

        //Initialize Blocks
        LibVulpesBlocks.blockPhantom = new BlockPhantom(Material.CIRCUITS).setRegistryName("blockPhantom");
        LibVulpesBlocks.blockHatch = new BlockHatch(Material.IRON).setRegistryName("hatch").setCreativeTab(tabMultiblock).setHardness(3f);
        LibVulpesBlocks.blockPlaceHolder = new BlockMultiblockPlaceHolder().setRegistryName("placeHolder").setHardness(1f);
        LibVulpesBlocks.blockAdvStructureBlock = new BlockAlphaTexture(Material.IRON).setRegistryName("advstructuremachine").setCreativeTab(tabMultiblock).setHardness(3f);
        LibVulpesBlocks.blockStructureBlock = new BlockAlphaTexture(Material.IRON).setRegistryName("structuremachine").setCreativeTab(tabMultiblock).setHardness(3f);
        LibVulpesBlocks.blockCreativeInputPlug = new BlockMultiMachineBattery(Material.IRON, TileCreativePowerInput.class, GuiHandler.guiId.MODULAR.ordinal()).setRegistryName("creativePowerBattery").setCreativeTab(tabMultiblock).setHardness(3f);
        LibVulpesBlocks.blockForgeInputPlug = new BlockMultiMachineBattery(Material.IRON, TileForgePowerInput.class, GuiHandler.guiId.MODULAR.ordinal()).setRegistryName("forgePowerInput").setCreativeTab(tabMultiblock).setHardness(3f);
        LibVulpesBlocks.blockForgeOutputPlug = new BlockMultiMachineBattery(Material.IRON, TileForgePowerOutput.class, GuiHandler.guiId.MODULAR.ordinal()).setRegistryName("forgePowerOutput").setCreativeTab(tabMultiblock).setHardness(3f);
        LibVulpesBlocks.blockCoalGenerator = new BlockTileComparatorOverride(TileCoalGenerator.class, GuiHandler.guiId.MODULAR.ordinal()).setRegistryName("coalGenerator").setCreativeTab(tabMultiblock).setHardness(3f);

        //Initialize Items
        LibVulpesItems.itemLinker = new ItemLinker().setRegistryName("libvulpes:linker").setCreativeTab(tabMultiblock).setTranslationKey("Linker");
        LibVulpesItems.itemBattery = new ItemIngredient(2).setRegistryName("libvulpes:battery").setCreativeTab(tabMultiblock).setTranslationKey("battery");
        LibVulpesItems.itemHoloProjector = new ItemProjector().setRegistryName("libvulpes:holoprojector").setCreativeTab(tabMultiblock).setTranslationKey("holoProjector");
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public void registerItems(RegistryEvent.Register<Item> evt)
    {
        //Register Items
        LibVulpesBlocks.registerItem(LibVulpesItems.itemLinker);
        LibVulpesBlocks.registerItem(LibVulpesItems.itemBattery);
        LibVulpesBlocks.registerItem(LibVulpesItems.itemHoloProjector);
        
        OreDictionary.registerOre("itemBattery", new ItemStack(LibVulpesItems.itemBattery,1,0));
    }
    
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		proxy.preInitItems();
		proxy.preInitBlocks();
		LibVulpesMetaBlocks.registerItemModels();
	}
    

    @EventHandler
    public void registerRecipes(FMLInitializationEvent evt)
    {
        List<net.minecraft.item.crafting.IRecipe> toRegister = Lists.newArrayList();
   

        
        for(net.minecraft.item.crafting.IRecipe recipe: toRegister)
        {
            GameData.register_impl(recipe);
        }
        
//      //GameRegistry.addShapelessRecipe(new ItemStack(LibVulpesBlocks.blockRFBattery), new ItemStack(LibVulpesBlocks.blockRFOutput));
//      //GameRegistry.addShapelessRecipe(new ItemStack(LibVulpesBlocks.blockRFOutput), new ItemStack(LibVulpesBlocks.blockRFBattery));
    }
	
	@SubscribeEvent(priority=EventPriority.HIGH)
    public void registerBlocks(RegistryEvent.Register<Block> evt)
	{
        //Register Blocks
        LibVulpesBlocks.registerBlock(LibVulpesBlocks.blockPhantom.setTranslationKey("phantomBlock"));
        LibVulpesBlocks.registerBlock(LibVulpesBlocks.blockHatch.setTranslationKey("hatch"), ItemBlockMeta.class, false);
        LibVulpesBlocks.registerBlock(LibVulpesBlocks.blockPlaceHolder.setTranslationKey("placeHolder"));
        LibVulpesBlocks.registerBlock(LibVulpesBlocks.blockStructureBlock.setTranslationKey("structureMachine"));
		LibVulpesBlocks.registerBlock(LibVulpesBlocks.blockAdvStructureBlock.setTranslationKey("advStructureMachine"));
        LibVulpesBlocks.registerBlock(LibVulpesBlocks.blockCreativeInputPlug.setTranslationKey("creativePowerBattery"));
        LibVulpesBlocks.registerBlock(LibVulpesBlocks.blockForgeInputPlug.setTranslationKey("forgePowerInput"));
        LibVulpesBlocks.registerBlock(LibVulpesBlocks.blockForgeOutputPlug.setTranslationKey("forgePowerOutput"));
        LibVulpesBlocks.registerBlock(LibVulpesBlocks.blockCoalGenerator.setTranslationKey("coalGenerator"));

        //Register Tile
        GameRegistry.registerTileEntity(TileOutputHatch.class, "vulpesoutputHatch");
        GameRegistry.registerTileEntity(TileInputHatch.class, "vulpesinputHatch");
        GameRegistry.registerTileEntity(TilePlaceholder.class, "vulpesplaceHolder");
        GameRegistry.registerTileEntity(TileFluidHatch.class, "vulpesFluidHatch");
        GameRegistry.registerTileEntity(TileSchematic.class, "vulpesTileSchematic");
        GameRegistry.registerTileEntity(TileCreativePowerInput.class, "vulpesCreativeBattery");
        GameRegistry.registerTileEntity(TileForgePowerInput.class, "vulpesForgePowerInput");
        GameRegistry.registerTileEntity(TileForgePowerOutput.class, "vulpesForgePowerOutput");
        GameRegistry.registerTileEntity(TileCoalGenerator.class, "vulpesCoalGenerator");
        //GameRegistry.registerTileEntity(TilePlugInputRF.class, "ARrfBattery");
        //GameRegistry.registerTileEntity(TilePlugOutputRF.class, "ARrfOutputRF");
        GameRegistry.registerTileEntity(TilePointer.class, "vulpesTilePointer");
        GameRegistry.registerTileEntity(TileInventoriedPointer.class, "vulpesTileInvPointer");

        if(FMLCommonHandler.instance().getSide().isClient()) {
            //Register Block models
            Item blockItem = Item.getItemFromBlock(LibVulpesBlocks.blockHatch);
            ModelLoader.setCustomModelResourceLocation(blockItem, 0, new ModelResourceLocation("libvulpes:inputhatch", "inventory"));
            ModelLoader.setCustomModelResourceLocation(blockItem, 1, new ModelResourceLocation("libvulpes:outputhatch", "inventory"));
            ModelLoader.setCustomModelResourceLocation(blockItem, 2, new ModelResourceLocation("libvulpes:fluidinputhatch", "inventory"));
            ModelLoader.setCustomModelResourceLocation(blockItem, 3, new ModelResourceLocation("libvulpes:fluidoutputhatch", "inventory"));
        }
        
        materialRegistry.registerOres(tabLibVulpesOres);

	}
	
	@SubscribeEvent(priority=EventPriority.HIGH)
	public void missingMappings(RegistryEvent.MissingMappings<Item> evt)
	{
		for(Mapping<Item> mapping : evt.getAllMappings())
		{
			if (mapping.key.compareTo(new ResourceLocation("libvulpes:productcrystal")) == 0)
				mapping.remap(MaterialRegistry.getItemStackFromMaterialAndType("Dilithium", AllowedProducts.getProductByName("GEM")).getItem());
			
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.preInit();
		LibVulpesMetaBlocks.init();

		//Configuration
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		zmaster587.libVulpes.Configuration.EUMult = (float)config.get(Configuration.CATEGORY_GENERAL, "EUPowerMultiplier", 4, "How many FE one EU makes").getDouble();
		zmaster587.libVulpes.Configuration.powerMult =(float)config.get(Configuration.CATEGORY_GENERAL, "PowerMultiplier", 1, "Power multiplier on machines").getDouble();

		config.save();

		TeslaCapabilityProvider.registerCap();


        //Register allowedProducts
        AllowedProducts.registerProduct("DUST");
        AllowedProducts.registerProduct("INGOT");
        AllowedProducts.registerProduct("GEM");
        AllowedProducts.registerProduct("BOULE");
        AllowedProducts.registerProduct("NUGGET");
        AllowedProducts.registerProduct("COIL", true);
        AllowedProducts.registerProduct("PLATE");
        AllowedProducts.registerProduct("STICK");
        AllowedProducts.registerProduct("BLOCK", true);
        AllowedProducts.registerProduct("ORE", true);
        AllowedProducts.registerProduct("FAN");
        AllowedProducts.registerProduct("SHEET");
        AllowedProducts.registerProduct("GEAR");

        //Register Ores
        materialRegistry.registerMaterial(new zmaster587.libVulpes.api.material.Material("Dilithium", "pickaxe", 3, 0xddcecb, AllowedProducts.getProductByName("DUST").getFlagValue() | AllowedProducts.getProductByName("GEM").getFlagValue()));
        materialRegistry.registerMaterial(new zmaster587.libVulpes.api.material.Material("Iron", "pickaxe", 1, 0xafafaf, AllowedProducts.getProductByName("SHEET").getFlagValue() | AllowedProducts.getProductByName("STICK").getFlagValue() | AllowedProducts.getProductByName("DUST").getFlagValue() | AllowedProducts.getProductByName("PLATE").getFlagValue(), false));
        materialRegistry.registerMaterial(new zmaster587.libVulpes.api.material.Material("Gold", "pickaxe", 1, 0xffff5d, AllowedProducts.getProductByName("DUST").getFlagValue() | AllowedProducts.getProductByName("COIL").getFlagValue() | AllowedProducts.getProductByName("PLATE").getFlagValue(), false));
        materialRegistry.registerMaterial(new zmaster587.libVulpes.api.material.Material("Silicon", "pickaxe", 1, 0x2c2c2b, AllowedProducts.getProductByName("INGOT").getFlagValue() | AllowedProducts.getProductByName("DUST").getFlagValue() | AllowedProducts.getProductByName("BOULE").getFlagValue() | AllowedProducts.getProductByName("NUGGET").getFlagValue() | AllowedProducts.getProductByName("PLATE").getFlagValue(), false));
        materialRegistry.registerMaterial(new zmaster587.libVulpes.api.material.Material("Copper", "pickaxe", 1, 0xd55e28, AllowedProducts.getProductByName("COIL").getFlagValue() | AllowedProducts.getProductByName("BLOCK").getFlagValue() | AllowedProducts.getProductByName("STICK").getFlagValue() | AllowedProducts.getProductByName("INGOT").getFlagValue() | AllowedProducts.getProductByName("NUGGET").getFlagValue() | AllowedProducts.getProductByName("DUST").getFlagValue() | AllowedProducts.getProductByName("PLATE").getFlagValue() | AllowedProducts.getProductByName("SHEET").getFlagValue()));
        materialRegistry.registerMaterial(new zmaster587.libVulpes.api.material.Material("Tin", "pickaxe", 1, 0xcdd5d8, AllowedProducts.getProductByName("BLOCK").getFlagValue() | AllowedProducts.getProductByName("PLATE").getFlagValue() | AllowedProducts.getProductByName("INGOT").getFlagValue() | AllowedProducts.getProductByName("NUGGET").getFlagValue() | AllowedProducts.getProductByName("DUST").getFlagValue()));
        materialRegistry.registerMaterial(new zmaster587.libVulpes.api.material.Material("Steel", "pickaxe", 1, 0x55555d, AllowedProducts.getProductByName("BLOCK").getFlagValue() | AllowedProducts.getProductByName("FAN").getFlagValue() | AllowedProducts.getProductByName("PLATE").getFlagValue() | AllowedProducts.getProductByName("INGOT").getFlagValue() | AllowedProducts.getProductByName("NUGGET").getFlagValue() | AllowedProducts.getProductByName("DUST").getFlagValue() | AllowedProducts.getProductByName("STICK").getFlagValue() | AllowedProducts.getProductByName("GEAR").getFlagValue() | AllowedProducts.getProductByName("SHEET").getFlagValue(), false));
        materialRegistry.registerMaterial(new zmaster587.libVulpes.api.material.Material("Titanium", "pickaxe", 1, 0xccc8fa, AllowedProducts.getProductByName("PLATE").getFlagValue() | AllowedProducts.getProductByName("COIL").getFlagValue() | AllowedProducts.getProductByName("INGOT").getFlagValue() | AllowedProducts.getProductByName("NUGGET").getFlagValue() | AllowedProducts.getProductByName("DUST").getFlagValue() | AllowedProducts.getProductByName("STICK").getFlagValue() | AllowedProducts.getProductByName("BLOCK").getFlagValue() | AllowedProducts.getProductByName("GEAR").getFlagValue() | AllowedProducts.getProductByName("SHEET").getFlagValue(), false));
        materialRegistry.registerMaterial(new zmaster587.libVulpes.api.material.Material("Rutile", "pickaxe", 1, 0xbf936a, AllowedProducts.getProductByName("ORE").getFlagValue(), new String[] {"Rutile", "Titanium"}));
        materialRegistry.registerMaterial(new zmaster587.libVulpes.api.material.Material("Aluminum", "pickaxe", 1, 0xb3e4dc, AllowedProducts.getProductByName("COIL").getFlagValue() | AllowedProducts.getProductByName("BLOCK").getFlagValue() | AllowedProducts.getProductByName("INGOT").getFlagValue() | AllowedProducts.getProductByName("PLATE").getFlagValue() | AllowedProducts.getProductByName("SHEET").getFlagValue() | AllowedProducts.getProductByName("DUST").getFlagValue() | AllowedProducts.getProductByName("NUGGET").getFlagValue() | AllowedProducts.getProductByName("SHEET").getFlagValue()));
        materialRegistry.registerMaterial(new zmaster587.libVulpes.api.material.Material("Iridium", "pickaxe", 2, 0xdedcce, AllowedProducts.getProductByName("COIL").getFlagValue() | AllowedProducts.getProductByName("BLOCK").getFlagValue() | AllowedProducts.getProductByName("DUST").getFlagValue() | AllowedProducts.getProductByName("INGOT").getFlagValue() | AllowedProducts.getProductByName("NUGGET").getFlagValue() | AllowedProducts.getProductByName("PLATE").getFlagValue() | AllowedProducts.getProductByName("STICK").getFlagValue()));

		//
		PacketHandler.INSTANCE.addDiscriminator(PacketMachine.class);
		PacketHandler.INSTANCE.addDiscriminator(PacketEntity.class);
		PacketHandler.INSTANCE.addDiscriminator(PacketChangeKeyState.class);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init();
		PacketHandler.init();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		proxy.registerEventHandlers();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new BucketHandler());
		
		//Init TileMultiblock
		//Item output
		List<BlockMeta> list = new LinkedList<>();
		list.add(new BlockMeta(LibVulpesBlocks.blockHatch, 1));
		list.add(new BlockMeta(LibVulpesBlocks.blockHatch, 9));
		TileMultiBlock.addMapping('O', list);

		//Item Inputs
		list = new LinkedList<>();
		list.add(new BlockMeta(LibVulpesBlocks.blockHatch, 0));
		list.add(new BlockMeta(LibVulpesBlocks.blockHatch, 8));
		TileMultiBlock.addMapping('I', list);

		//Power input
		list = new LinkedList<>();
		list.add(new BlockMeta(LibVulpesBlocks.blockCreativeInputPlug, BlockMeta.WILDCARD));
		list.add(new BlockMeta(LibVulpesBlocks.blockForgeInputPlug, BlockMeta.WILDCARD));
		if(LibVulpesBlocks.blockRFBattery != null)
			list.add(new BlockMeta(LibVulpesBlocks.blockRFBattery, BlockMeta.WILDCARD));
		if(LibVulpesBlocks.blockIC2Plug != null)
			list.add(new BlockMeta(LibVulpesBlocks.blockIC2Plug, BlockMeta.WILDCARD));
		TileMultiBlock.addMapping('P', list);

		//Power output
		list = new LinkedList<>();
		list.add(new BlockMeta(LibVulpesBlocks.blockForgeOutputPlug, BlockMeta.WILDCARD));
		if(LibVulpesBlocks.blockRFOutput != null)
			list.add(new BlockMeta(LibVulpesBlocks.blockRFOutput, BlockMeta.WILDCARD));
		TileMultiBlock.addMapping('p', list);

		//Liquid input
		list = new LinkedList<>();
		list.add(new BlockMeta(LibVulpesBlocks.blockHatch, 2));
		list.add(new BlockMeta(LibVulpesBlocks.blockHatch, 10));
		TileMultiBlock.addMapping('L', list);

		//Liquid output
		list = new LinkedList<>();
		list.add(new BlockMeta(LibVulpesBlocks.blockHatch, 3));
		list.add(new BlockMeta(LibVulpesBlocks.blockHatch, 11));
		TileMultiBlock.addMapping('l', list);

		//User Recipes


	}

	public void loadXMLRecipe(Class clazz) {
		File file = new File(userModifiableRecipes.get(clazz));
		if(!file.exists()) {
			try {
				file.createNewFile();
				BufferedReader inputStream = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/assets/libvulpes/defaultrecipe.xml")));

				BufferedWriter stream2 = new BufferedWriter(new FileWriter(file));


				while(inputStream.ready()) {
					stream2.write(inputStream.readLine() + "\n");
				}


				//Write recipes

				stream2.write("<Recipes useDefault=\"true\">\n");
				for(IRecipe recipe : RecipesMachine.getInstance().getRecipes(clazz)) {
					boolean writeable = true;
					for (ItemStack stack : recipe.getOutput()) {
						if(stack.hasTagCompound()) {
							writeable = false;
							break;
						}
					}

					if(((RecipesMachine.Recipe)recipe).outputToOnlyEmptySlots())
						writeable = false;

					if(writeable)
						stream2.write(XMLRecipeLoader.writeRecipe(recipe) + "\n");
				}
				stream2.write("</Recipes>");
				stream2.close();

				inputStream.close();


			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			XMLRecipeLoader loader = new XMLRecipeLoader();
			try {
				loader.loadFile(file);
				loader.registerRecipes(clazz);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		time++;
	}
}

