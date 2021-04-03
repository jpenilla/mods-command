package xyz.jpenilla.modscommand;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.permission.CommandPermission;
import net.fabricmc.loader.api.metadata.Person;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.toComponent;
import static net.kyori.adventure.text.event.ClickEvent.copyToClipboard;
import static net.kyori.adventure.text.event.ClickEvent.openUrl;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static xyz.jpenilla.modscommand.Colors.BLUE_VIOLET;
import static xyz.jpenilla.modscommand.Colors.EMERALD;
import static xyz.jpenilla.modscommand.Colors.MUSTARD;
import static xyz.jpenilla.modscommand.Mods.MODS;

final class ModsCommand implements RegistrableCommand {
  private static final String MOD_ARGUMENT_NAME = "mod_id";
  private static final Pattern URL_PATTERN = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/\\S*)?"); // copied from adventure-text-serializer-legacy
  private static final Component GRAY_SEPARATOR = text(':', GRAY);

  private final String label;
  private final CommandPermission permission;

  ModsCommand(final @NonNull String primaryAlias, final @Nullable CommandPermission permission) {
    this.label = primaryAlias;
    this.permission = permission;
  }

  @Override
  public void register(final @NonNull CommandManager<Commander> manager) {
    final Command.Builder<Commander> base = manager.commandBuilder(this.label);
    final Command.Builder<Commander> mods;
    if (this.permission != null) {
      mods = base.permission(this.permission);
    } else {
      mods = base;
    }
    manager.command(
      mods.handler(this::executeListMods)
    );
    manager.command(
      mods.argument(ModDescriptionArgument.of(MOD_ARGUMENT_NAME))
        .handler(this::executeModInfo)
    );
    manager.command(
      mods.argument(ModDescriptionArgument.of(MOD_ARGUMENT_NAME))
        .literal("children")
        .handler(this::executeListChildren)
    );
  }

  private void executeListMods(final @NonNull CommandContext<Commander> ctx) {
    final TextComponent.Builder builder = text()
      .append(text("Loaded Mods", EMERALD, BOLD))
      .append(text(String.format(" (%s)", MODS.allMods().count()), GRAY, ITALIC));
    MODS.topLevelMods().stream()
      .sorted(comparing(ModDescription::modId))
      .forEach(mod ->
        builder.append(newline())
          .append(text(" - "))
          .append(shortModDescription(mod))
      );
    ctx.getSender().sendMessage(builder);
  }

  private void executeListChildren(final @NonNull CommandContext<Commander> ctx) {
    final ModDescription mod = ctx.get(MOD_ARGUMENT_NAME);
    if (mod.children().isEmpty()) {
      ctx.getSender().sendMessage(
        text()
          .color(MUSTARD)
          .content("Mod ")
          .append(text()
            .append(coloredBoldModName(mod))
            .apply(this.modClickAndHover(mod))
          )
          .append(text(" does not have any child mods!"))
      );
      return;
    }
    ctx.getSender().sendMessage(
      text()
        .append(coloredBoldModName(mod))
        .append(text(" child mods", GRAY, BOLD))
        .append(newline())
        .append(
          mod.children().stream()
            .map(child -> text()
              .content(" - ")
              .append(this.shortModDescription(child))
              .build())
            .collect(toComponent(newline()))
        )
    );
  }

  private void executeModInfo(final @NonNull CommandContext<Commander> ctx) {
    final ModDescription mod = ctx.get(MOD_ARGUMENT_NAME);
    final TextComponent.Builder builder = text();
    builder.append(coloredBoldModName(mod))
      .append(newline())
      .append(text(" modid", BLUE_VIOLET), GRAY_SEPARATOR, space())
      .append(text(mod.modId()))
      .append(newline())
      .append(text(" version", BLUE_VIOLET), GRAY_SEPARATOR, space())
      .append(text(mod.version()))
      .append(newline())
      .append(text(" type", BLUE_VIOLET), GRAY_SEPARATOR, space())
      .append(text(mod.type()));

    if (!mod.description().isEmpty()) {
      builder.append(newline())
        .append(text(" description", BLUE_VIOLET), GRAY_SEPARATOR, space())
        .append(text(mod.description()));
    }
    if (!mod.authors().isEmpty()) {
      builder.append(newline())
        .append(text(" authors", BLUE_VIOLET), GRAY_SEPARATOR, space())
        .append(text(mod.authors().stream()
          .map(Person::getName)
          .collect(Collectors.joining(", "))));
    }
    if (!mod.contributors().isEmpty()) {
      builder.append(newline())
        .append(text(" contributors", BLUE_VIOLET), GRAY_SEPARATOR, space())
        .append(text(mod.contributors().stream()
          .map(Person::getName)
          .collect(Collectors.joining(", "))));
    }
    if (!mod.licenses().isEmpty()) {
      builder.append(newline())
        .append(text(" license", BLUE_VIOLET), GRAY_SEPARATOR, space())
        .append(text(String.join(", ", mod.licenses())));
    }
    if (!mod.children().isEmpty()) {
      builder.append(newline())
        .append(text(" child mods", BLUE_VIOLET), GRAY_SEPARATOR, space())
        .append(
          mod.children().stream()
            .sorted(comparing(ModDescription::modId))
            .limit(5)
            .map(this::modIdWithClickAndHover)
            .collect(toComponent(text(", ", GRAY)))
        );
      if (mod.children().size() > 5) {
        builder.append(
          text()
            .content(", and " + (mod.children().size() - 5) + " more...")
            .decorate(ITALIC)
            .color(GRAY)
            .hoverEvent(
              text()
                .content("Click to see all of ")
                .append(coloredBoldModName(mod))
                .append(text("'s child mods."))
                .build()
            )
            .clickEvent(runCommand(String.format("/%s %s children", this.label, mod.modId())))
        );
      }
    }
    if (!mod.contact().isEmpty()) {
      builder.append(newline())
        .append(text(" contact", BLUE_VIOLET))
        .append(GRAY_SEPARATOR);
      mod.contact().forEach((key, value) -> {
        builder.append(newline());
        final TextComponent.Builder info = text()
          .content("  - ")
          .append(text(key, BLUE_VIOLET))
          .append(GRAY_SEPARATOR, space())
          .append(text(value));
        final Matcher matcher = URL_PATTERN.matcher(value);
        if (matcher.find() && matcher.group().equals(value)) {
          info.hoverEvent(text("Click to open url"));
          info.clickEvent(openUrl(value));
        } else {
          info.hoverEvent(text("Click to copy to clipboard"));
          info.clickEvent(copyToClipboard(value));
        }
        builder.append(info);
      });
    }
    ctx.getSender().sendMessage(builder);
  }

  private @NonNull Component shortModDescription(final @NonNull ModDescription mod) {
    final TextComponent.Builder modBuilder = text()
      .color(WHITE)
      .apply(this.modClickAndHover(mod))
      .append(text(mod.name(), BLUE_VIOLET))
      .append(text(String.format(" (%s) ", mod.modId()), GRAY, ITALIC))
      .append(text(String.format("v%s", mod.version())));
    if (!mod.children().isEmpty()) {
      final TextComponent.Builder children = text()
        .color(GRAY)
        .decorate(ITALIC)
        .append(text(" ("))
        .append(text(mod.children().size()))
        .append(text(" child mods)"));
      modBuilder.append(children);
    }
    return modBuilder.build();
  }

  private @NonNull Component modIdWithClickAndHover(final @NonNull ModDescription mod) {
    return text()
      .content(mod.modId())
      .apply(this.modClickAndHover(mod))
      .build();
  }

  private @NonNull Consumer<? super ComponentBuilder<?, ?>> modClickAndHover(final @NonNull ModDescription mod) {
    return builder ->
      builder.clickEvent(this.modInfo(mod))
        .hoverEvent(text()
          .content("Click to see more about ")
          .append(coloredBoldModName(mod))
          .append(text("!"))
          .build());
  }

  private static @NonNull TextComponent coloredBoldModName(final @NonNull ModDescription mod) {
    return text(mod.name(), EMERALD, BOLD);
  }

  private @NonNull ClickEvent modInfo(final @NonNull ModDescription description) {
    return runCommand(String.format("/%s %s", this.label, description.modId()));
  }
}
