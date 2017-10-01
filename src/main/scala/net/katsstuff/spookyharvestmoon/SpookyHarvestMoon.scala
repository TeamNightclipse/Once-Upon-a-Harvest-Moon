package net.katsstuff.spookyharvestmoon

import net.katsstuff.spookyharvestmoon.helper.LogHelper
import net.katsstuff.spookyharvestmoon.lib.LibMod
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}

@Mod(modid = LibMod.Id, name = LibMod.Name, version = LibMod.Version, modLanguage = "scala")
object SpookyHarvestMoon {

  @Mod.EventHandler
  def preinit(event: FMLPreInitializationEvent): Unit = {
    LogHelper.assignLog(event.getModLog)
  }

  @Mod.EventHandler
  def init(event: FMLInitializationEvent): Unit = {}

  @Mod.EventHandler
  def postinit(event: FMLPostInitializationEvent): Unit = {}

}
