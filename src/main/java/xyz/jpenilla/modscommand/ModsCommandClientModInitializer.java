/*
 * Mods Command
 * Copyright (c) 2022 Jason Penilla
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

import java.util.function.Function;
import net.fabricmc.api.ClientModInitializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricClientCommandManager;
import xyz.jpenilla.modscommand.command.Commander;
import xyz.jpenilla.modscommand.command.Commands;
import xyz.jpenilla.modscommand.command.RegistrableCommand;
import xyz.jpenilla.modscommand.command.commands.DumpModsCommand;
import xyz.jpenilla.modscommand.command.commands.ModsCommand;

@DefaultQualifier(NonNull.class)
public final class ModsCommandClientModInitializer implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    final FabricClientCommandManager<Commander> manager = new FabricClientCommandManager<>(
      ExecutionCoordinator.simpleCoordinator(),
      SenderMapper.create(
        Commander.ClientCommander::new,
        commander -> ((Commander.ClientCommander) commander).source()
      )
    );
    Commands.configureCommandManager(manager);

    this.registerCommand(manager, "clientmods", label -> new ModsCommand(label, null));
    this.registerCommand(manager, "dumpclientmods", label -> new DumpModsCommand(label, null));
  }

  private void registerCommand(final CommandManager<Commander> commandManager, final String label, final Function<String, RegistrableCommand> factory) {
    factory.apply(label).register(commandManager);
    factory.apply("modscommand:%s".formatted(label)).register(commandManager);
  }
}
