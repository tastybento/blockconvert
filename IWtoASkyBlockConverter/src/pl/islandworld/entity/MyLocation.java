package pl.islandworld.entity;

import java.io.Serializable;

import org.bukkit.Location;
import org.bukkit.util.NumberConversions;

public class MyLocation implements Serializable
{
	private static final long serialVersionUID = 1L;
	private double x;
	private double y;
	private double z;
	private float pitch;
	private float yaw;

	public MyLocation(Location loc)
	{
		this(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
	}

	public MyLocation(final double x, final double y, final double z, final float yaw, final float pitch)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
	}

	public void setX(double x)
	{
		this.x = x;
	}

	public double getX()
	{
		return x;
	}

	public int getBlockX()
	{
		return locToBlock(x);
	}

	public void setY(double y)
	{
		this.y = y;
	}

	public double getY()
	{
		return y;
	}

	public int getBlockY()
	{
		return locToBlock(y);
	}

	public void setZ(double z)
	{
		this.z = z;
	}

	public double getZ()
	{
		return z;
	}

	public int getBlockZ()
	{
		return locToBlock(z);
	}

	public void setYaw(float yaw)
	{
		this.yaw = yaw;
	}

	public float getYaw()
	{
		return yaw;
	}

	public void setPitch(float pitch)
	{
		this.pitch = pitch;
	}

	public float getPitch()
	{
		return pitch;
	}

	public static int locToBlock(double loc)
	{
		return NumberConversions.floor(loc);
	}
}
