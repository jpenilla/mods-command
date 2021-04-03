package xyz.jpenilla.modscommand;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricServerCommandManager;
import cloud.commandframework.permission.Permission;
import net.fabricmc.api.ModInitializer;

public final class ModsCommandModInitializer implements ModInitializer {
  @Override
  public void onInitialize() {
    final FabricServerCommandManager<Commander> manager = new FabricServerCommandManager<>(
      CommandExecutionCoordinator.simpleCoordinator(),
      Commander.ServerCommander::new,
      commander -> ((Commander.ServerCommander) commander).source()
    );

    ModDescriptionArgument.registerParser(manager);

    final ModsCommand modsCommand = new ModsCommand("mods", Permission.of("modscommand.mods"));
    modsCommand.register(manager);

    final DumpModsCommand dumpModsCommand = new DumpModsCommand("dumpmods", Permission.of("modscommand.dumpmods"));
    dumpModsCommand.register(manager);
  }
}
