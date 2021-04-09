/*
 * Mods Command
 * Copyright (c) 2020 Jason Penilla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
