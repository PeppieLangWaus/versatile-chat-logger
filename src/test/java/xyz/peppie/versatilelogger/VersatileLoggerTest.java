package xyz.peppie.versatilelogger;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import xyz.peppie.versatilelogger.VersatileLoggerPlugin;

public class VersatileLoggerTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(VersatileLoggerPlugin.class);
		RuneLite.main(args);
	}
}
