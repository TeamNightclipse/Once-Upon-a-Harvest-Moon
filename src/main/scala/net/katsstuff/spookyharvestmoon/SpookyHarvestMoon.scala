package net.katsstuff.spookyharvestmoon

import net.katsstuff.spookyharvestmoon.helper.LogHelper
import net.katsstuff.spookyharvestmoon.lib.LibMod
import net.katsstuff.spookyharvestmoon.network.SpookyPacketHandler
import net.minecraftforge.fml.common.{Mod, SidedProxy}
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}

@Mod(modid = LibMod.Id, name = LibMod.Name, version = LibMod.Version, modLanguage = "scala")
object SpookyHarvestMoon {

  //noinspection VarCouldBeVal
  @SidedProxy(serverSide = LibMod.CommonProxy, clientSide = LibMod.ClientProxy, modId = LibMod.Id)
  var proxy: CommonProxy = _

  @Mod.EventHandler
  def preinit(event: FMLPreInitializationEvent): Unit = {
    LogHelper.assignLog(event.getModLog)
    SpookyPacketHandler.load()
  }

  @Mod.EventHandler
  def init(event: FMLInitializationEvent): Unit = {}

  @Mod.EventHandler
  def postinit(event: FMLPostInitializationEvent): Unit = {}

}
