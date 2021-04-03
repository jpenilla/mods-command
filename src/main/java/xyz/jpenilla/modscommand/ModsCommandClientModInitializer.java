package xyz.jpenilla.modscommand;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricClientCommandManager;
import net.fabricmc.api.ClientModInitializer;

import java.util.stream.Stream;

public final class ModsCommandClientModInitializer implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    final FabricClientCommandManager<Commander> manager = new FabricClientCommandManager<>(
      CommandExecutionCoordinator.simpleCoordinator(),
      Commander.ClientCommander::new,
      commander -> ((Commander.ClientCommander) commander).source()
    );

    ModDescriptionArgument.registerParser(manager);

    Stream.of(
      new ModsCommand("clientmods", null),
      new ModsCommand("modscommand:clientmods", null),
      new DumpModsCommand("dumpclientmods", null),
      new DumpModsCommand("modscommand:dumpclientmods", null)
    ).forEach(command -> command.register(manager));
  }
}
