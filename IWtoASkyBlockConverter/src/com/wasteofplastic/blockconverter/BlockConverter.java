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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import pl.islandworld.IslandWorld;
import pl.islandworld.entity.MyLocation;
import pl.islandworld.entity.SimpleIsland;
import pl.islandworld.entity.SimpleIslandV6;

import com.evilmidget38.UUIDFetcher;

public class BlockConverter extends JavaPlugin implements Listener {
    private File plugins;
    private File aSkyBlockConfig;
    private File islandWorldConfig;
    private FileConfiguration aSkyBlockConf;
    private FileConfiguration islandWorldConf;
    private List<String> playerNames = new ArrayList<String>();
    private HashMap<String,Players> players = new HashMap<String,Players>();
    private boolean UUIDflag;
    private BukkitTask check;
    // UUID list from Mojang
    private Map<String, UUID> response = null;
    // Lower case version of UUID list
    HashMap<String,UUID> lowerCaseNames;
    private IslandWorld IWPlugin;

    @Override
    public void onEnable() {
	// Check to see if IslandWorld is active or ASkyblock
	if (getServer().getPluginManager().isPluginEnabled("IslandWorld")) {
	    getLogger().severe("IslandWorld plugin found");
	    IWPlugin = (IslandWorld) getServer().getPluginManager().getPlugin("IslandWorld");
	} else {
	    getLogger().severe("IslandWorld plugin not found, disabling plugin");
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
	aSkyBlockConf.set("general.resetwait", (islandWorldConf.getInt("time-limit",0) * 60));
	// distance
	int distance = islandWorldConf.getInt("island-size",100);
	aSkyBlockConf.set("island.distance", distance);
	int spacing = islandWorldConf.getInt("region-spacing",1);
	aSkyBlockConf.set("island.protectionRange", (islandWorldConf.getInt("island-size",100) - spacing));
	aSkyBlockConf.set("island.xoffset", Math.round((double)distance/2D));
	aSkyBlockConf.set("island.zoffset", Math.round((double)distance/2D));
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
	aSkyBlockConf.set("island.vipteamsize", islandWorldConf.getInt("party-limit-vip",6));
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

	// Check that the world exists
	if (getServer().getWorld(world) == null) {
	    sender.sendMessage(ChatColor.RED + "The world ("+world+") in the IslandWorld config does not exist!");
	    sender.sendMessage(ChatColor.RED + "Stopping conversion.");
	    return true;
	}
	// Make an islands folder in aSkyblock
	File asbIslandDir = new File(plugins.getPath() + File.separator + "ASkyBlock" + File.separator + "islands");
	if (!asbIslandDir.exists()) {
	    asbIslandDir.mkdir();
	}
	
	
	// Go to the islands folder and see how many there are
	if (new File(plugins.getPath() + File.separator + "IslandWorld" , "islelistV6.dat").exists())
	{
	    HashMap<String, SimpleIslandV6> isleList = new HashMap<String, SimpleIslandV6>();
	    try
	    {
		isleList = (HashMap<String, SimpleIslandV6>) SLAPI.load(plugins.getPath() + File.separator + "IslandWorld" + File.separator + "islelistV6.dat");
	    }
	    catch (Exception e)
	    {
		getLogger().warning("Error: " + e.getMessage());
	    }
	    if (isleList == null || isleList.isEmpty()) {
		sender.sendMessage(ChatColor.RED + "No islands data found in IslandWorld!");
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
		// Bedrock goes 5 below height of islands.
		/*
		 * Location of island spawn points with default schematic is as follows:
		 * For distance = 100
		 * 1st island: 50, 46 - island area is 0 to 100 for x and z, with the island placed roughly in the middle
		 * 2nd island: 50, 146
		 */
		int xLocation = (islandData.getX() * distance) + (distance/2);
		int zLocation = (islandData.getZ() * distance) + (distance/2);
		Location islandLocation = new Location(getServer().getWorld(world),xLocation, height - 5,zLocation);
		getLogger().info("New Island Location is :"+((islandData.getX() * distance)+ (distance/2))+ "," + ((islandData.getZ() * distance)+ (distance/2)));
		// Save the name to the aSkyblock folder
		String islandName = xLocation + "," + zLocation;
		File newIsland = new File(plugins.getPath() + File.separator + "ASkyBlock" + File.separator + "islands" + File.separator + islandName + ".yml");  
		// Save file
		try {
		    newIsland.createNewFile();
		} catch (IOException e) {
		    getLogger().severe("Could not save the island location file in aSkyblock/islands!");
		    e.printStackTrace();
		}

		// Place bedrock
		/*
	    Block keyBlock = islandLocation.getBlock();
	    Material blockType = keyBlock.getType();
	    // Just break everything.
	    if (!blockType.equals(Material.BEDROCK)) {
		sender.sendMessage(ChatColor.RED + "Broke " + blockType.toString() + " to make room for bedrock");
		keyBlock.breakNaturally();
		keyBlock.setType(Material.BEDROCK);
	    }*/

		// Get the island leader
		String leaderName = player.getKey();
		getLogger().info("Leader/owner is :"+leaderName.toLowerCase());
		// Create this player
		Players leader = new Players(this,leaderName);
		leader.setHasIsland(true);
		leader.setIslandLocation(islandLocation);
		MyLocation leaderHome = islandData.getLocation();
		if (leaderHome != null) {
		    //getLogger().info("Leader's home is " + leaderHome.toString());
		    leader.setHomeLocation(new Location(getServer().getWorld(world),leaderHome.getBlockX(),leaderHome.getBlockY(),leaderHome.getBlockZ()));
		}
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
			if (memberHome != null) {
			    teamMember.setHomeLocation(new Location(getServer().getWorld(world),memberHome.getBlockX(),memberHome.getBlockY(),memberHome.getBlockZ()));
			} 
			players.put(name.toLowerCase(),teamMember);
			playerNames.add(name.toLowerCase());
		    } 
		}
	    }
	} else if (new File(plugins.getPath() + File.separator + "IslandWorld" , "islelist.dat").exists())
	{
	    HashMap<String, SimpleIsland> isleList = new HashMap<String, SimpleIsland>();
	    try
	    {
		isleList = (HashMap<String, SimpleIsland>) SLAPI.load(plugins.getPath() + File.separator + "IslandWorld" + File.separator + "islelist.dat");
	    }
	    catch (Exception e)
	    {
		getLogger().warning("Error: " + e.getMessage());
	    }
	    if (isleList == null || isleList.isEmpty()) {
		sender.sendMessage(ChatColor.RED + "No islands data found in IslandWorld!");
		return true;
	    }
	    int total = isleList.size();
	    sender.sendMessage("There are " + total + " islands to convert");
	    int count = 1;
	    // General idea - load all the data, do the name lookups then create the new files

	    for (Entry<String,SimpleIsland> player : isleList.entrySet()) {
		sender.sendMessage("Loading island #" + (count++) + " of " + total);
		// Find out who the owners are of this island
		// Get island info
		// Location
		SimpleIsland islandData = player.getValue();
		// Bedrock goes 5 below height of islands.
		/*
		 * Location of island spawn points with default schematic is as follows:
		 * For distance = 100
		 * 1st island: 50, 46 - island area is 0 to 100 for x and z, with the island placed roughly in the middle
		 * 2nd island: 50, 146
		 */
		int xLocation = (islandData.getX() * distance) + (distance/2);
		int zLocation = (islandData.getZ() * distance) + (distance/2);
		Location islandLocation = new Location(getServer().getWorld(world),xLocation, height - 5,zLocation);
		getLogger().info("New Island Location is :"+((islandData.getX() * distance)+ (distance/2))+ "," + ((islandData.getZ() * distance)+ (distance/2)));
		// Save the name to the aSkyblock folder
		String islandName = xLocation + "," + zLocation;
		File newIsland = new File(plugins.getPath() + File.separator + "ASkyBlock" + File.separator + "islands" + File.separator + islandName + ".yml");  
		// Save file
		try {
		    newIsland.createNewFile();
		} catch (IOException e) {
		    getLogger().severe("Could not save the island location file in aSkyblock/islands!");
		    e.printStackTrace();
		}

		// Place bedrock
		/*
	    Block keyBlock = islandLocation.getBlock();
	    Material blockType = keyBlock.getType();
	    // Just break everything.
	    if (!blockType.equals(Material.BEDROCK)) {
		sender.sendMessage(ChatColor.RED + "Broke " + blockType.toString() + " to make room for bedrock");
		keyBlock.breakNaturally();
		keyBlock.setType(Material.BEDROCK);
	    }*/

		// Get the island leader
		String leaderName = player.getKey();
		getLogger().info("Leader/owner is :"+leaderName.toLowerCase());
		// Create this player
		Players leader = new Players(this,leaderName);
		leader.setHasIsland(true);
		leader.setIslandLocation(islandLocation);
		MyLocation leaderHome = islandData.getLocation();
		if (leaderHome != null) {
		    //getLogger().info("Leader's home is " + leaderHome.toString());
		    leader.setHomeLocation(new Location(getServer().getWorld(world),leaderHome.getBlockX(),leaderHome.getBlockY(),leaderHome.getBlockZ()));
		}
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
			if (memberHome != null) {
			    teamMember.setHomeLocation(new Location(getServer().getWorld(world),memberHome.getBlockX(),memberHome.getBlockY(),memberHome.getBlockZ()));
			} 
			players.put(name.toLowerCase(),teamMember);
			playerNames.add(name.toLowerCase());
		    } 
		}
	    }
	}
	// Now get the UUID's
	sender.sendMessage(ChatColor.GREEN + "Now contacting Mojang to obtain UUID's for players. This could take a while, see console and please wait...");
	for (Entry<String,Players> name : players.entrySet()) {
	    sender.sendMessage(ChatColor.GREEN + name.getKey().toLowerCase());
	}
	sender.sendMessage(ChatColor.GREEN + "Requesting " + playerNames.size() + " UUID's");
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
	    }}, 40L, 40L);
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
	    //getLogger().info("Set UUID for " + name.getKey());
	    if (players.containsKey(name.getKey().toLowerCase())) {
		players.get(name.getKey().toLowerCase()).setUUID(name.getValue());
	    } else {
		getLogger().severe("Oddly, " + name.getKey().toLowerCase() + " is not in the list of players. Skipping...");
	    }
	}
	File playerDir = new File(plugins.getPath() + File.separator + "ASkyBlock" + File.separator + "players");
	if (!playerDir.exists()) {
	    playerDir.mkdir();
	}
	// Now save all the players
	List<String> noUUIDs = new ArrayList<String>();
	for (String name : players.keySet()) {
	    if (players.get(name.toLowerCase()).getUUID() != null) {
		players.get(name.toLowerCase()).save(playerDir);
	    } else {
		// Try and obtain local UUID if offline mode is true
		if (!getServer().getOnlineMode()) {
		    @SuppressWarnings("deprecation")
		    UUID offlineUUID = getServer().getOfflinePlayer(name).getUniqueId();
		    if (offlineUUID != null) {
			getLogger().warning("Set *offline* UUID for " + name);
			players.get(name).setUUID(offlineUUID);
			players.get(name).save(playerDir);
		    }
		} else {
		    getLogger().warning(name + " has no UUID. Cannot save this player!");
		    noUUIDs.add(name);
		}
	    }  
	}
	if (!noUUIDs.isEmpty()) {
	    getLogger().warning("The following player names have no UUID (according to Mojang or offline server) so had to be skipped:");
	    for (String n : noUUIDs) {
		getLogger().warning(n);
	    }
	}
	getLogger().info("***** All Done! *****");
	getLogger().info("Stop server and check that config.yml in askyblock folder is okay");
	getLogger().info("Then copy askyblock.jar to /plugins folder. Remove the converter jar and then restart server.");
    }

}
