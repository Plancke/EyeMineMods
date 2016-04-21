package com.specialeffect.mods;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.ChangeFlyingStateMessage;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import scala.actors.threadpool.LinkedBlockingQueue;

// This mod is purely a wrapper to cluster a few
// child mods. The parent (this) handles config
// so that it can be changed all in one place.
@Mod(modid = SpecialEffectMovements.MODID, 
  	 version = ModUtils.VERSION, 
	 name = SpecialEffectMovements.NAME, 
	 guiFactory = "com.specialeffect.gui.GuiFactoryMovements")
public class SpecialEffectMovements extends BaseClassWithCallbacks {

	public static final String MODID = "specialeffect.movements";	
	public static final String NAME = "SpecialEffectsMovements";

	public static Configuration mConfig;	

	// Flying options
	private static int flyHeightManual = 2;
	private static int flyHeightAuto = 6;
	
	// Walking options -> walk with gaze
	private static int filterLength = 50;
    private static boolean moveWhenMouseStationary = false;
    public static float customSpeedFactor = 0.8f;
	
    // OptiKey adjustments
    private static int viewIncrement = 2;
    private static double moveIncrement = 2;

	// AutoJump
    private static boolean defaultDoAutoJump = true;
    
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"A selection of mods for accessible moving methods");

		// Set up config
		mConfig = new Configuration(event.getSuggestedConfigurationFile());
		this.syncConfig();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.modID.equals(this.MODID)) {
			syncConfig();
		}
	}
	
	public static void syncConfig() {

		// Flying
		flyHeightManual = mConfig.getInt("Fly height manual", Configuration.CATEGORY_GENERAL, flyHeightManual, 1, 20, "How high to fly in manual mode");
		flyHeightAuto = mConfig.getInt("Fly height auto", Configuration.CATEGORY_GENERAL, flyHeightAuto, 1, 20, "How high to fly in auto mode");
		
		// Move with gaze
		filterLength = mConfig.getInt("Smoothness filter", Configuration.CATEGORY_GENERAL, filterLength, 
									  1, 200, "How many ticks to take into account for slowing down while looking around. (smaller number = faster)");
        moveWhenMouseStationary = mConfig.getBoolean("Move when mouse stationary", Configuration.CATEGORY_GENERAL, 
        									moveWhenMouseStationary, "Continue walking forward when the mouse is stationary. Recommended to be turned off for eye gaze control, on for joysticks.");
        customSpeedFactor = mConfig.getFloat("Speed factor", Configuration.CATEGORY_GENERAL, customSpeedFactor, 0.0f, 1.0f, 
        						"A scaling factor for speed of walk-with-gaze. 1.0 = maximum."); 
        
        // OptiKey adjustments
        viewIncrement = mConfig.getInt("View adjustment (degrees)", Configuration.CATEGORY_GENERAL, 
        								viewIncrement, 1, 45, 
        								"Fixed rotation for small view adjustments");
        moveIncrement = (double)mConfig.getFloat("Move adjustment", Configuration.CATEGORY_GENERAL, (float)moveIncrement, 
        										 0.1f, 10.0f,
        										 "Fixed distance for small movement adjustments");
        
        // AutoJump
        defaultDoAutoJump = mConfig.getBoolean("Auto-jump switched on by default?", Configuration.CATEGORY_GENERAL, defaultDoAutoJump,
        									   "Whether auto-jump is on at the beginning of a new game.");
       
		if (mConfig.hasChanged()) {
			mConfig.save();
		}
	}

}
