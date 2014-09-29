package com.wasteofplastic.blockconverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import pl.islandworld.entity.MyLocation;
import pl.islandworld.entity.SimpleIslandV6;

import com.evilmidget38.UUIDFetcher;

public class BlockConverter extends JavaPlugin implements Listener {
    File plugins;
    File aSkyBlockConfig;
    File islandWorldConfig;
    FileConfiguration aSkyBlockConf;
    FileConfiguration islandWorldConf;
    List<String> playerNames = new ArrayList<String>();
    HashMap<String,Players> players = new HashMap<String,Players>();
    boolean UUIDflag;
    BukkitTask check;
    HashMap<String, SimpleIslandV6> isleList;
    // UUID list from Mojang
    Map<String, UUID> response = null;
    // Lower case version of UUID list
    HashMap<String,UUID> lowerCaseNames;

    @Override
    public void onEnable() {
	isleList = new HashMap<String, SimpleIslandV6>();
	// Check to see if IslandWorld is active or ASkyblock
	if (getServer().getPluginManager().isPluginEnabled("IslandWorld")) {
	    getLogger().severe("IslandWorld is active - please remove IslandWorld.jar from plugins before running this converter.");
	    getServer().getPluginManager().disablePlugin(this);
	}
	if (getServer().getPluginManager().isPluginEnabled("ASkyBlock")) {
	    getLogger().severe("A SkyBlock is active - please remove askyblock.jar from plugins before running this converter.");
	    getServer().getPluginManager().disablePlugin(this);
	}
	// Check that directories exist
	plugins = getDataFolder().getParentFile();
	islandWorldConfig = new File(plugins.getPath() + File.separator + "IslandWorld" + File.separator + "config.yml");
	if (!islandWorldConfig.exists()) {
	    getLogger().severe("There appears to be no IslandWorld folder or config in the plugins folder!");
	    getServer().getPluginManager().disablePlugin(this);
	} else {
	    getLogger().info("Found IslandWorld config.");
	}
	aSkyBlockConfig = new File(plugins.getPath() + File.separator + "ASkyBlock" + File.separator + "config.yml");
	if (!aSkyBlockConfig.exists()) {
	    getLogger().severe("There appears to be no ASkyBlock folder or config in the plugins folder!");
	    getServer().getPluginManager().disablePlugin(this);
	} else {
	    getLogger().info("Found ASkyBlock config in the plugins folder.");
	}
    }
    @Override
    public void onDisable() {
	getLogger().info("IslandWorld to A Skyblock converter disabled");
    }

    @SuppressWarnings("unchecked")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
	// Just do it
	sender.sendMessage(ChatColor.GREEN + "Starting conversion...");
	// Set up configs first
	aSkyBlockConf = YamlConfiguration.loadConfiguration(aSkyBlockConfig);
	islandWorldConf = YamlConfiguration.loadConfiguration(islandWorldConfig);

	// Chest items
	/*
	String chestItems = islandWorldConf.getString("options.island.chestItems","");
	getLogger().info("IslandWorld: Chest items = " + chestItems);
	String aChestItems = "";
	if (!chestItems.isEmpty()) {
	    // Parse
	    String[] items = chestItems.split(" ");
	    for (String item : items){
		//getLogger().info("DEBUG: parsing = " + item);
		String[] split = item.split(":");
		Material material = Material.getMaterial(Integer.valueOf(split[0]));
		if (material != null) {
		    if (aChestItems.isEmpty()) {
			aChestItems = material.toString() + ":" + split[1]; 
		    } else {
			aChestItems = aChestItems + " " + material.toString() + ":" + split[1]; 
		    }
		}
	    }
	    getLogger().info("ASkyBlock: Chest items = " + aChestItems);
	    aSkyBlockConf.set("island.chestItems", aChestItems);
	}*/
	// World name
	String world = islandWorldConf.getString("world-isle","IslandWorld");
	aSkyBlockConf.set("general.worldName", world );
	// reset wait - minutes to seconds
	aSkyBlockConf.set("general.cooldownRestart", (islandWorldConf.getInt("time-limit",0) * 60));
	// distance
	aSkyBlockConf.set("island.distance", islandWorldConf.getInt("island-size",100));
	int spacing = islandWorldConf.getInt("region-spacing",1);
	aSkyBlockConf.set("island.protectionRange", (islandWorldConf.getInt("island-size",100) - spacing));
	// Height
	int height = islandWorldConf.getInt("island-height",20);
	aSkyBlockConf.set("general.islandlevel", height);
	// PVP
	boolean pvp = islandWorldConf.getBoolean("flags.pvp",false);
	if (pvp) {
	    aSkyBlockConf.set("island.allowPvP", "allow");
	}
	else {
	    aSkyBlockConf.set("island.allowPvP", "deny");
	}
	// Teleport mob removal
	aSkyBlockConf.set("general.islandremovemobs", islandWorldConf.getBoolean("remove-mob-on-tp",false));
	aSkyBlockConf.set("general.loginremovemobs", islandWorldConf.getBoolean("remove-mob-on-tp",false));
	// Max team size
	aSkyBlockConf.set("island.maxteamsize", islandWorldConf.getInt("party-limit",4));
	aSkyBlockConf.set("island.maxteamsizeVIP", islandWorldConf.getInt("party-limit-vip",6));
	// Protection flags
	aSkyBlockConf.set("island.allowchestaccess", islandWorldConf.getBoolean("flags.chest-access",false));
	aSkyBlockConf.set("island.allowhurtmobs", islandWorldConf.getBoolean("flags.kill-animals",false));

	try {
	    aSkyBlockConf.save(aSkyBlockConfig);
	} catch (IOException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
	sender.sendMessage(ChatColor.GREEN + "Completed config.yml transfer");

	// Go to the islands folder and see how many there are
	if (isleList.isEmpty() && new File(plugins.getPath() + File.separator + "IslandWorld" , "islelistV6.dat").exists())
	{
	    try
	    {
		isleList = (HashMap<String, SimpleIslandV6>) SLAPI.load(plugins.getPath() + File.separator + "IslandWorld" + File.separator + "islelistV6.dat");
	    }
	    catch (Exception e)
	    {
		getLogger().warning("Error: " + e.getMessage());
	    }
	}
	if (isleList == null) {
	    sender.sendMessage(ChatColor.RED + "There is no islands data in IslandWorld!");
	    return true;
	}
	int total = isleList.size();
	sender.sendMessage("There are " + total + " islands to convert");
	int count = 1;
	// General idea - load all the data, do the name lookups then create the new files

	for (Entry<String,SimpleIslandV6> player : isleList.entrySet()) {
	    sender.sendMessage("Loading island #" + (count++) + " of " + total);
	    // Find out who the owners are of this island
	    // Get island info
	    // Location
	    SimpleIslandV6 islandData = player.getValue();
	    MyLocation loc = islandData.getLocation();
	    // Bedrock goes 5 below height of islands. 
	    Location islandLocation = new Location(getServer().getWorld(world),loc.getBlockX(), height - 5,loc.getBlockZ());
	    
	    getLogger().info("New Island Location is :"+loc.getBlockX()+ "," + loc.getBlockY()+ "," + loc.getBlockZ());
	    // Place bedrock
	    Block keyBlock = islandLocation.getBlock();
	    Material blockType = keyBlock.getType();
	    // Do something intelligent - it's it' valuable
	    switch (blockType) {
	    // Valuable
	    case ACTIVATOR_RAIL:
	    case ANVIL:
	    case BEACON:
	    case BED_BLOCK:
	    case BOOKSHELF:
	    case BREWING_STAND:
	    case BURNING_FURNACE:
	    case CAULDRON:
	    case CHEST:
	    case DAYLIGHT_DETECTOR:
	    case DETECTOR_RAIL:
	    case DIAMOND_BLOCK:
	    case DIODE_BLOCK_OFF:
	    case DIODE_BLOCK_ON:
	    case DISPENSER:
	    case DRAGON_EGG:
	    case DROPPER:
	    case EMERALD_BLOCK:
	    case ENCHANTMENT_TABLE:
	    case ENDER_CHEST:
	    case EXPLOSIVE_MINECART:
	    case FURNACE:
	    case GOLD_BLOCK:
	    case HOPPER:
	    case HOPPER_MINECART:
	    case IRON_BLOCK:
	    case IRON_DOOR_BLOCK:
	    case ITEM_FRAME:
	    case JUKEBOX:
	    case LAPIS_BLOCK:
	    case MINECART:
	    case NOTE_BLOCK:
	    case OBSIDIAN:
	    case PISTON_BASE:
	    case PISTON_EXTENSION:
	    case PISTON_MOVING_PIECE:
	    case PISTON_STICKY_BASE:
	    case POWERED_MINECART:
	    case POWERED_RAIL:
	    case RAILS:
	    case REDSTONE_BLOCK:
	    case REDSTONE_COMPARATOR_OFF:
	    case REDSTONE_COMPARATOR_ON:
	    case REDSTONE_LAMP_OFF:
	    case REDSTONE_LAMP_ON:
	    case REDSTONE_TORCH_OFF:
	    case REDSTONE_TORCH_ON:
	    case REDSTONE_WIRE:
	    case STORAGE_MINECART:
	    case TRAPPED_CHEST:
	    case WOODEN_DOOR:
	    case WOOD_DOOR:
	    case ACACIA_STAIRS:
	    case BIRCH_WOOD_STAIRS:
	    case BOAT:
	    case BRICK:
	    case BRICK_STAIRS:
	    case BROWN_MUSHROOM:
	    case CACTUS:
	    case CAKE_BLOCK:
	    case CARPET:
	    case CARROT:
	    case CLAY:
	    case CLAY_BRICK:
	    case COAL_BLOCK:
	    case COAL_ORE:
	    case COBBLESTONE:
	    case COBBLESTONE_STAIRS:
	    case COBBLE_WALL:

	    case CROPS:

	    case DARK_OAK_STAIRS:

	    case DEAD_BUSH:

	    case DIRT:

	    case DOUBLE_PLANT:

	    case DOUBLE_STEP:

	    case EMERALD_ORE:

	    case ENDER_PORTAL:

	    case ENDER_PORTAL_FRAME:

	    case ENDER_STONE:

	    case FENCE:

	    case FENCE_GATE:

	    case FLOWER_POT:

	    case GLASS:

	    case GLOWING_REDSTONE_ORE:

	    case GLOWSTONE:

	    case GOLD_ORE:

	    case GOLD_PLATE:

	    case GRASS:

	    case GRAVEL:

	    case HARD_CLAY:

	    case HAY_BLOCK:

	    case HUGE_MUSHROOM_1:

	    case HUGE_MUSHROOM_2:

	    case IRON_FENCE:

	    case IRON_ORE:

	    case IRON_PLATE:

	    case JACK_O_LANTERN:

	    case JUNGLE_WOOD_STAIRS:

	    case LADDER:

	    case LAPIS_ORE:

	    case LEAVES:

	    case LEAVES_2:

	    case LEVER:

	    case LOG:

	    case LOG_2:

	    case LONG_GRASS:

	    case MELON_BLOCK:

	    case MELON_STEM:

	    case MOSSY_COBBLESTONE:

	    case MYCEL:

	    case NETHERRACK:

	    case NETHER_BRICK:

	    case NETHER_BRICK_STAIRS:

	    case NETHER_FENCE:

	    case NETHER_STALK:

	    case NETHER_WARTS:

	    case PACKED_ICE:

	    case PAINTING:

	    case PUMPKIN:

	    case PUMPKIN_STEM:

	    case QUARTZ:

	    case QUARTZ_BLOCK:

	    case QUARTZ_ORE:

	    case QUARTZ_STAIRS:

	    case REDSTONE_COMPARATOR:

	    case REDSTONE_ORE:

	    case RED_MUSHROOM:

	    case RED_ROSE:

	    case SAND:

	    case SANDSTONE:

	    case SANDSTONE_STAIRS:

	    case SAPLING:

	    case SIGN:

	    case SIGN_POST:

	    case SKULL:

	    case SMOOTH_BRICK:

	    case SMOOTH_STAIRS:

	    case SNOW:

	    case SNOW_BLOCK:

	    case SOIL:

	    case SOUL_SAND:

	    case SPONGE:

	    case SPRUCE_WOOD_STAIRS:

	    case STAINED_CLAY:

	    case STAINED_GLASS:

	    case STAINED_GLASS_PANE:

	    case STEP:

	    case STONE:

	    case STONE_BUTTON:

	    case STONE_PLATE:

	    case SUGAR_CANE_BLOCK:

	    case THIN_GLASS:

	    case TNT:

	    case TORCH:

	    case TRAP_DOOR:

	    case TRIPWIRE:

	    case TRIPWIRE_HOOK:

	    case VINE:

	    case WALL_SIGN:

	    case WATER_LILY:

	    case WEB:

	    case WHEAT:

	    case WOOD:

	    case WOOD_BUTTON:

	    case WOOD_DOUBLE_STEP:

	    case WOOD_PLATE:

	    case WOOD_STAIRS:

	    case WOOD_STEP:

	    case WOOL:

	    case WORKBENCH:

	    case YELLOW_FLOWER:
		getLogger().info("Broke " + blockType.toString() + " to make room for bedrock");
		keyBlock.breakNaturally();
		keyBlock.setType(Material.BEDROCK);
		break;
	    default:
		keyBlock.setType(Material.BEDROCK);
		break;
	    }
	    // Get the island leader
	    String leaderName = player.getKey();
	    getLogger().info("Leader/owner is :"+leaderName.toLowerCase());
	    // Create this player
	    Players leader = new Players(this,leaderName);
	    leader.setHasIsland(true);
	    leader.setIslandLocation(islandLocation);
	    playerNames.add(leaderName.toLowerCase());
	    players.put(leaderName.toLowerCase(),leader);
	    // Step through the names on this island
	    for (String name : islandData.getMembers()) {
		getLogger().info("Island member " + name);
		if (!name.equalsIgnoreCase(leaderName)) {
		    // Team member
		    Players teamMember = new Players(this,name.toLowerCase());
		    leader.addTeamMember(name.toLowerCase());
		    leader.addTeamMember(leaderName.toLowerCase());
		    leader.setTeamLeaderName(leaderName.toLowerCase());
		    leader.setTeamIslandLocation(islandLocation);
		    leader.setInTeam(true);
		    teamMember.setTeamLeaderName(leaderName.toLowerCase());
		    teamMember.setTeamIslandLocation(islandLocation);
		    teamMember.setInTeam(true);
		    MyLocation memberHome = islandData.getHome(name.toLowerCase());
		    teamMember.setHomeLocation(new Location(getServer().getWorld(world),memberHome.getBlockX(),memberHome.getBlockY(),memberHome.getBlockZ()));
		    players.put(name.toLowerCase(),teamMember);
		    playerNames.add(name.toLowerCase());
		} 
	    }
	}
	// Now get the UUID's
	sender.sendMessage(ChatColor.GREEN + "Now contacting Mojang to obtain UUID's for players. This could take a while, see console and please wait...");
	for (Entry<String,Players> name : players.entrySet()) {
	    sender.sendMessage(ChatColor.GREEN + name.getKey().toLowerCase());
	}
	final UUIDFetcher fetcher = new UUIDFetcher(playerNames);
	UUIDflag = false;
	// Kick off an async task and grab the UUIDs.
	getServer().getScheduler().runTaskAsynchronously(this, new Runnable(){

	    @Override
	    public void run() {
		// Fetch UUID's
		try {
		    response = fetcher.call();
		} catch (Exception e) {
		    getLogger().warning("Exception while running UUIDFetcher");
		    e.printStackTrace();
		}
		UUIDflag = true;
	    }});

	// Kick of a scheduler to check if the UUID results are in yet
	check = getServer().getScheduler().runTaskTimer(this, new Runnable(){
	    @Override
	    public void run() {
		getLogger().info("Checking for name to UUID results");
		// Check to see if UUID has returned
		if (UUIDflag) {
		    getLogger().info("Received!");
		    finish();
		} else {
		    getLogger().info("Waiting...");
		}
	    }}, 20L, 20L);
	return true;
    }
    protected void finish() {
	check.cancel();
	// finishes the conversion
	getLogger().info("Received " + response.size() + " UUID's");
	// Create lower case versions of the response names 
	lowerCaseNames = new HashMap<String,UUID>();
	for (Entry<String,UUID> name : response.entrySet()) {
	    lowerCaseNames.put(name.getKey().toLowerCase(), name.getValue());
	}	
	// Now complete the player objects
	for (Entry<String,UUID> name : lowerCaseNames.entrySet()) {
	    getLogger().info("Set UUID for " + name.getKey());
	    if (players.containsKey(name.getKey().toLowerCase())) {
		players.get(name.getKey().toLowerCase()).setUUID(name.getValue());
	    } else {
		getLogger().severe("Oddly, " + name.getKey().toLowerCase() + " is not in the list of players. Skipping...");
	    }
	}
	File playerDir = new File(plugins.getPath() + File.separator + "aSkyBlock" + File.separator + "players");
	if (!playerDir.exists()) {
	    playerDir.mkdir();
	}
	// Now save all the players
	for (String name : players.keySet()) {
	    if (players.get(name.toLowerCase()).getUUID() != null) {
		players.get(name.toLowerCase()).save(playerDir);
	    } else {
		getLogger().warning(name + " has no UUID. Cannot save this player!");
	    }  
	}
	getLogger().info("***** All Done! *****");
	getLogger().info("Stop server and check that config.yml in askyblock folder is okay");
	getLogger().info("Then copy askyblock.jar to /plugins folder. Remove uaconv.jar and then restart server.");
    }

}
