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

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricServerCommandManager;
import cloud.commandframework.permission.Permission;
import java.io.IOException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.jpenilla.modscommand.command.Commander;
import xyz.jpenilla.modscommand.command.Commands;
import xyz.jpenilla.modscommand.command.commands.DumpModsCommand;
import xyz.jpenilla.modscommand.command.commands.ModsCommand;
import xyz.jpenilla.modscommand.configuration.Config;
import xyz.jpenilla.modscommand.configuration.ConfigHolder;
import xyz.jpenilla.modscommand.model.Mods;

import static xyz.jpenilla.modscommand.model.Mods.mods;

@DefaultQualifier(NonNull.class)
public final class ModsCommandModInitializer implements ModInitializer {
  private static @MonotonicNonNull ModsCommandModInitializer instance;
  public static final Logger LOGGER = LoggerFactory.getLogger("Mods Command");

  private final ConfigHolder<Config> configHolder = ConfigHolder.create(
    FabricLoader.getInstance().getModContainer("mods-command").orElseThrow(),
    Config.class
  );

  @Override
  public void onInitialize() {
    instance = this;

    this.loadConfig();

    final Mods mods = mods(); // Initialize so it can't fail later
    LOGGER.info("Mods Command detected {} loaded mods ({} top-level).", mods.totalModCount(), mods.topLevelModCount()); // We identify ourselves in log messages due to Vanilla MC's terrible Log4j config.

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

  private void loadConfig() {
    try {
      this.configHolder.load();
    } catch (final IOException ex) {
      throw new RuntimeException("Failed to load Mods Command config", ex);
    }
  }

  public Config config() {
    return this.configHolder.config();
  }

  public static ModsCommandModInitializer instance() {
    if (instance == null) {
      throw new IllegalStateException("Mods Command has not yet been initialized!");
    }
    return instance;
  }
}
