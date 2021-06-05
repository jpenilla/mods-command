/*
 * Mods Command
 * Copyright (c) 2021 Jason Penilla
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

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.permission.CommandPermission;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.copyToClipboard;
import static net.kyori.adventure.text.event.ClickEvent.openFile;
import static xyz.jpenilla.modscommand.Colors.EMERALD;
import static xyz.jpenilla.modscommand.Colors.PINK;
import static xyz.jpenilla.modscommand.Mods.mods;

final class DumpModsCommand implements RegistrableCommand {
  private final String label;
  private final CommandPermission permission;
  private final Path dumpFile;

  DumpModsCommand(
    final @NonNull String primaryAlias,
    final @Nullable CommandPermission permission
  ) {
    this.label = primaryAlias;
    this.permission = permission;
    this.dumpFile = FabricLoader.getInstance().getGameDir().resolve("installed-mods.yml");
  }

  @Override
  public void register(final @NonNull CommandManager<Commander> manager) {
    final Command.Builder<Commander> builder = manager.commandBuilder(this.label);
    if (this.permission == null) {
      manager.command(builder.handler(this::executeDumpModList));
    } else {
      manager.command(builder.permission(this.permission).handler(this::executeDumpModList));
    }
  }

  private void executeDumpModList(final @NonNull CommandContext<Commander> ctx) {
    final String dump;
    try {
      dump = createDump();
      Files.writeString(this.dumpFile, dump);
    } catch (final IOException ex) {
      throw new RuntimeException("Failed to create mod list dump.", ex);
    }
    final TextComponent.Builder message = text()
      .content("Saved list of installed mods to ")
      .append(text(builder -> {
        builder.content(this.dumpFile.getFileName().toString()).color(PINK);
        if (ctx.getSender() instanceof Commander.ClientCommander) {
          builder.clickEvent(openFile(this.dumpFile.toAbsolutePath().toString()))
            .hoverEvent(text("Click to open file!", EMERALD));
        }
      }))
      .append(text(" in the game directory."));
    ctx.getSender().sendMessage(message);
    final TextComponent.Builder copyMessage = text()
      .content("Click here to copy it's contents to the clipboard.")
      .color(PINK)
      .clickEvent(copyToClipboard(dump))
      .hoverEvent(text("Click to copy to clipboard!", EMERALD));
    ctx.getSender().sendMessage(copyMessage);
  }

  private static @NonNull String createDump() throws ConfigurateException {
    final StringWriter stringWriter = new StringWriter();
    final BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
    final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
      .sink(() -> bufferedWriter)
      .build();
    final ConfigurationNode root = loader.createNode();

    final FabricLoader fabricLoader = FabricLoader.getInstance();
    root.node("environment-type").set(fabricLoader.getEnvironmentType());
    root.node("development-environment").set(fabricLoader.isDevelopmentEnvironment());
    root.node("launch-arguments").set(fabricLoader.getLaunchArguments(true));

    final ConfigurationNode os = root.node("operating-system");
    os.node("arch").set(System.getProperty("os.arch"));
    os.node("name").set(System.getProperty("os.name"));
    os.node("version").set(System.getProperty("os.version"));

    final ConfigurationNode java = root.node("java");
    java.node("vendor").set(System.getProperty("java.vendor"));
    java.node("vendor-url").set(System.getProperty("java.vendor.url"));
    java.node("version").set(System.getProperty("java.version"));

    final ConfigurationNode modsNode = root.node("mods");
    for (final ModDescription mod : mods().topLevelMods()) {
      serializeModDescriptionToNode(modsNode, mod);
    }

    loader.save(root);
    return stringWriter.toString();
  }

  private static void serializeModDescriptionToNode(final @NonNull ConfigurationNode node, final @NonNull ModDescription mod) throws SerializationException {
    final ConfigurationNode modNode = node.appendListNode();
    modNode.node("mod-id").set(mod.modId());
    modNode.node("name").set(mod.name());
    modNode.node("version").set(mod.version());
    if (!mod.authors().isEmpty()) {
      modNode.node("authors").set(String.join(", ", mod.authors()));
    }
    for (final ModDescription child : mod.children()) {
      serializeModDescriptionToNode(modNode.node("children"), child);
    }
  }
}
