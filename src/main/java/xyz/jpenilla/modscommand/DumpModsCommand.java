package xyz.jpenilla.modscommand;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.permission.CommandPermission;
import net.fabricmc.loader.api.metadata.Person;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.copyToClipboard;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static xyz.jpenilla.modscommand.Mods.MODS;

final class DumpModsCommand implements RegistrableCommand {
  private final String label;
  private final CommandPermission permission;

  DumpModsCommand(
    final @NonNull String primaryAlias,
    final @Nullable CommandPermission permission
  ) {
    this.label = primaryAlias;
    this.permission = permission;
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
    final StringWriter stringWriter = new StringWriter();
    final BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
    final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
      .sink(() -> bufferedWriter)
      .build();
    final CommentedConfigurationNode node = loader.createNode();
    try {
      for (final ModDescription mod : MODS.topLevelMods()) {
        serializeModDescriptionToNode(node.node("mods"), mod);
      }
      loader.save(node);
      ctx.getSender().sendMessage(
        text()
          .content("Click to copy mod list to clipboard")
          .color(GREEN)
          .clickEvent(copyToClipboard(stringWriter.toString()))
          .hoverEvent(text("Click to copy mod list to clipboard"))
      );
    } catch (final ConfigurateException ex) {
      throw new RuntimeException(ex);
    }
  }

  private static void serializeModDescriptionToNode(final @NonNull ConfigurationNode node, final @NonNull ModDescription mod) throws SerializationException {
    final ConfigurationNode modNode = node.appendListNode();
    modNode.node("mod-id").set(mod.modId());
    modNode.node("name").set(mod.name());
    modNode.node("version").set(mod.version());
    if (!mod.authors().isEmpty()) {
      modNode.node("authors").set(
        mod.authors().stream().map(Person::getName).collect(Collectors.joining(", "))
      );
    }
    for (final ModDescription child : mod.children()) {
      serializeModDescriptionToNode(modNode.node("children"), child);
    }
  }
}
