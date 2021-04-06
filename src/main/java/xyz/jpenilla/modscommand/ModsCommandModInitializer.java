package xyz.jpenilla.modscommand;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricServerCommandManager;
import cloud.commandframework.permission.Permission;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static xyz.jpenilla.modscommand.Mods.mods;

public final class ModsCommandModInitializer implements ModInitializer {
  static final Logger LOGGER = LogManager.getLogger("ModsCommand");

  @Override
  public void onInitialize() {
    final Mods mods = mods(); // Initialize so it can't fail later
    LOGGER.info("Mods Command detected " + mods.allMods().count() + " loaded mods."); // We identify ourselves in log messages due to Vanilla MC's terrible Log4j config.

    final FabricServerCommandManager<Commander> manager = new FabricServerCommandManager<>(
      CommandExecutionCoordinator.simpleCoordinator(),
      Commander.ServerCommander::new,
      commander -> ((Commander.ServerCommander) commander).source()
    );
    Commands.configureCommandManager(manager);

    final ModsCommand modsCommand = new ModsCommand("mods", Permission.of("modscommand.mods"));
    modsCommand.register(manager);

    final DumpModsCommand dumpModsCommand = new DumpModsCommand("dumpmods", Permission.of("modscommand.dumpmods"));
    dumpModsCommand.register(manager);
  }
}
