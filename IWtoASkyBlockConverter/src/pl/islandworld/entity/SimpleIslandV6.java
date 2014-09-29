package pl.islandworld.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;


public class SimpleIslandV6 implements Serializable
{
	private static final long serialVersionUID = 4L;

	private String owner;

	private int isle_x;
	private int isle_z;

	private MyLocation isle_loc;

	private boolean isLocked;

	private long createTime;
	private long ownerLoginTime;

	private String schematic;

	private ArrayList<String> members;
	private HashMap<String, MyLocation> homes;

	private long points;

	private boolean isOpened;
	private boolean openOffline;
	
	private boolean visitBlocked;
	
	private ArrayList<String> blockedPlayers;
	
	public SimpleIslandV6(int x, int z)
	{
		isle_x = x;
		isle_z = z;

		createTime = System.currentTimeMillis();
		ownerLoginTime = System.currentTimeMillis();

		schematic = "normal";

		members = new ArrayList<String>();
		blockedPlayers = new ArrayList<String>();
		homes = new HashMap<String, MyLocation>();
	}

	@Override
	public String toString()
	{
		return "[" + isle_x + "x" + isle_z + "][" + owner + "]";
	}

	public String getHash()
	{
		return "[" + isle_x + "x" + isle_z + "]";
	}
	
	public void clear()
	{
		isle_loc = null;

		createTime = 0;
		ownerLoginTime = 0;

		schematic = null;

		members.clear();
		blockedPlayers.clear();
		homes.clear();
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String ow)
	{
		owner = ow;
	}

	public int getX()
	{
		return isle_x;
	}

	public int getZ()
	{
		return isle_z;
	}

	public void setLocation(Location loc)
	{
		isle_loc = new MyLocation(loc);
	}

	public MyLocation getLocation()
	{
		return isle_loc;
	}

	public void setCreateTime(long time)
	{
		createTime = time;
	}

	public long getCreateTime()
	{
		return createTime;
	}

	public void setOwnerLoginTime(long time)
	{
		ownerLoginTime = time;
	}

	public long getOwnerLoginTime()
	{
		return ownerLoginTime;
	}

	public void setSchematic(String schema)
	{
		schematic = schema;
	}

	public String getSchematic()
	{
		return schematic;
	}

	public List<String> getMembers()
	{
		return members;
	}

	public void addMember(String member)
	{
		members.add(member.toLowerCase());
	}

	public void removeMember(String member)
	{
		if (members.contains(member.toLowerCase()))
			members.remove(member.toLowerCase());
	}

	public boolean isMember(String name)
	{
		final List<String> list = getMembers();
		if (list != null && !list.isEmpty())
		{
			for(String memb : list)
			{
				if (memb.equalsIgnoreCase(name))
					return true;
			}
		}
		return false;
	}
	
	public HashMap<String, MyLocation> getHomes()
	{
		return homes;
	}

	public void addHome(String name, MyLocation loc)
	{
		homes.put(name, loc);
	}

	public void removeHome(String name)
	{
		homes.remove(name);
	}

	public MyLocation getHome(String name)
	{
		if (homes.containsKey(name))
			return homes.get(name);

		return null;
	}

	public boolean isLocked()
	{
		return isLocked;
	}

	public void setLocked(boolean state)
	{
		isLocked = state;
	}

	public boolean isOpened()
	{
		return isOpened;
	}
	
	public void setOpened(boolean state)
	{
		isOpened = state;
	}
	
	public boolean isOpenedOffline()
	{
		return openOffline;
	}
	
	public void setOpenedOffline(boolean state)
	{
		openOffline = state;
	}
	
	public void setPoints(long p)
	{
		points = p;
	}

	public long getPoints()
	{
		return points;
	}

	public boolean isVisitBlocked()
	{
		return visitBlocked;
	}

	public void setVisitBlocked(boolean val)
	{
		visitBlocked = val;
	}
	
	public ArrayList<String> getBlockedPlayers()
	{
		return blockedPlayers;
	}

	public void addBlockedPlayer(String name)
	{
		if (blockedPlayers == null)
			blockedPlayers = new ArrayList<String>();
		
		blockedPlayers.add(name);
	}
	
	public void removeBlockedPlayers(String name)
	{
		if (blockedPlayers == null)
			blockedPlayers = new ArrayList<String>();
		
		if (blockedPlayers.contains(name))
			blockedPlayers.remove(name);
	}
	
	public boolean isPlayerBlocked(String name)
	{
		final List<String> list = getBlockedPlayers();
		if (list != null && !list.isEmpty())
		{
			for(String memb : list)
			{
				if (memb.equalsIgnoreCase(name))
					return true;
			}
		}
		return false;
	}
}