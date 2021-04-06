package xyz.jpenilla.modscommand;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricClientCommandManager;
import net.fabricmc.api.ClientModInitializer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

public final class ModsCommandClientModInitializer implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    final FabricClientCommandManager<Commander> manager = new FabricClientCommandManager<>(
      CommandExecutionCoordinator.simpleCoordinator(),
      Commander.ClientCommander::new,
      commander -> ((Commander.ClientCommander) commander).source()
    );
    Commands.configureCommandManager(manager);

    this.registerCommand(manager, "clientmods", label -> new ModsCommand(label, null));
    this.registerCommand(manager, "dumpclientmods", label -> new ModsCommand(label, null));
  }

  private void registerCommand(final @NonNull CommandManager<Commander> commandManager, final @NonNull String label, final @NonNull Function<String, RegistrableCommand> factory) {
    factory.apply(label).register(commandManager);
    factory.apply(String.format("modscommand:%s", label)).register(commandManager);
  }
}
