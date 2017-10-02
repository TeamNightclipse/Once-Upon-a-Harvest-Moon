package net.katsstuff.spookyharvestmoon

import net.katsstuff.spookyharvestmoon.client.ClientProxy
import net.katsstuff.spookyharvestmoon.helper.LogHelper
import net.katsstuff.spookyharvestmoon.lib.LibMod
import net.katsstuff.spookyharvestmoon.network.SpookyPacketHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.{FMLCommonHandler, Mod, SidedProxy}
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.relauncher.Side

@Mod(modid = LibMod.Id, name = LibMod.Name, version = LibMod.Version, modLanguage = "scala")
object SpookyHarvestMoon {
  MinecraftForge.EVENT_BUS.register(CommonProxy)

  if (FMLCommonHandler.instance().getSide == Side.CLIENT) {
    MinecraftForge.EVENT_BUS.register(ClientProxy)
  }

  assert(LibMod.Id == JLibMod.ID)

  //noinspection VarCouldBeVal
  @SidedProxy(serverSide = LibMod.CommonProxy, clientSide = LibMod.ClientProxy, modId = LibMod.Id)
  var proxy: CommonProxy = _

  @Mod.EventHandler
  def preinit(event: FMLPreInitializationEvent): Unit = {
    LogHelper.assignLog(event.getModLog)
    SpookyPacketHandler.load()
    proxy.registerRenderers()
  }

  @Mod.EventHandler
  def init(event: FMLInitializationEvent): Unit = {
    proxy.bakeRenderModels()
    proxy.registerEntities()
  }

  @Mod.EventHandler
  def postinit(event: FMLPostInitializationEvent): Unit = {}

}
