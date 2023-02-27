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
package xyz.jpenilla.modscommand.command.commands;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.SimpleCloudKey;
import cloud.commandframework.permission.CommandPermission;
import com.terraformersmc.modmenu.ModMenu;
import io.leangen.geantyref.TypeToken;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import xyz.jpenilla.modscommand.command.Commander;
import xyz.jpenilla.modscommand.command.RegistrableCommand;
import xyz.jpenilla.modscommand.command.argument.ModDescriptionArgument;
import xyz.jpenilla.modscommand.model.Environment;
import xyz.jpenilla.modscommand.model.ModDescription;
import xyz.jpenilla.modscommand.util.BiIntFunction;
import xyz.jpenilla.modscommand.util.Pagination;

import static java.util.Comparator.comparing;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.Component.toComponent;
import static net.kyori.adventure.text.event.ClickEvent.copyToClipboard;
import static net.kyori.adventure.text.event.ClickEvent.openUrl;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;
import static xyz.jpenilla.modscommand.model.Mods.mods;
import static xyz.jpenilla.modscommand.util.Colors.BLUE;
import static xyz.jpenilla.modscommand.util.Colors.BRIGHT_BLUE;
import static xyz.jpenilla.modscommand.util.Colors.EMERALD;
import static xyz.jpenilla.modscommand.util.Colors.MIDNIGHT_BLUE;
import static xyz.jpenilla.modscommand.util.Colors.MUSTARD;
import static xyz.jpenilla.modscommand.util.Colors.PINK;
import static xyz.jpenilla.modscommand.util.Colors.PURPLE;

@DefaultQualifier(NonNull.class)
public final class ModsCommand implements RegistrableCommand {
  private static final CloudKey<ModDescription> MOD_ARGUMENT_KEY = SimpleCloudKey.of("mod_id", TypeToken.get(ModDescription.class));
  private static final CloudKey<Integer> PAGE_ARGUMENT_KEY = SimpleCloudKey.of("page_number", TypeToken.get(Integer.class));
  private static final CloudKey<String> QUERY_ARGUMENT_KEY = SimpleCloudKey.of("query", TypeToken.get(String.class));
  private static final Pattern URL_PATTERN = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/\\S*)?"); // copied from adventure-text-serializer-legacy
  private static final Component GRAY_SEPARATOR = text(':', GRAY);
  private static final Component DASH = text(" - ", MIDNIGHT_BLUE);

  private final String label;
  private final @Nullable CommandPermission permission;

  public ModsCommand(final String primaryAlias, final @Nullable CommandPermission permission) {
    this.label = primaryAlias;
    this.permission = permission;
  }

  @Override
  public void register(final CommandManager<Commander> manager) {
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
      mods.literal("page")
        .argument(pageArgument())
        .handler(this::executeListMods)
    );
    final Command.Builder<Commander> info = mods.literal("info")
      .argument(ModDescriptionArgument.of(MOD_ARGUMENT_KEY.getName()));
    manager.command(info.handler(this::executeModInfo));
    manager.command(
      info.literal("children")
        .argument(pageArgument())
        .handler(this::executeListChildren)
    );
    manager.command(
      mods.literal("search")
        .argument(StringArgument.greedy(QUERY_ARGUMENT_KEY.getName()))
        .handler(this::executeSearch)
    );

    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && FabricLoader.getInstance().isModLoaded("modmenu")) {
      manager.command(
        mods.literal("config")
          .argument(ModDescriptionArgument.of(MOD_ARGUMENT_KEY.getName()))
          .handler(ctx -> {
            final ModDescription mod = ctx.get(MOD_ARGUMENT_KEY);
            final Minecraft client = Minecraft.getInstance();
            final @Nullable Screen configScreen = ModMenu.getConfigScreen(mod.modId(), client.screen);
            if (configScreen == null) {
              ctx.getSender().sendMessage(textOfChildren(coloredBoldModName(mod), text(" does not have a config screen!", MUSTARD)));
              return;
            }
            client.execute(() -> client.setScreen(configScreen));
          })
      );
    }
  }

  private static CommandArgument<Commander, Integer> pageArgument() {
    return IntegerArgument.<Commander>newBuilder(PAGE_ARGUMENT_KEY.getName())
      .withMin(1)
      .asOptionalWithDefault(1)
      .build();
  }

  private void executeListMods(final CommandContext<Commander> ctx) {
    final int page = ctx.getOptional(PAGE_ARGUMENT_KEY).orElse(1);
    final Pagination<ModDescription> pagination = Pagination.<ModDescription>builder()
      .header((currentPage, pages) -> Component.textOfChildren(
        text("Loaded Mods", PURPLE, BOLD),
        text(String.format(" (%s total, %s top-level)", mods().totalModCount(), mods().topLevelModCount()), GRAY, ITALIC)
      ))
      .footer(this.footerRenderer(p -> String.format("/%s page %d", this.label, p)))
      .pageOutOfRange(ModsCommand::pageOutOfRange)
      .item((item, lastOfPage) -> Component.textOfChildren(DASH, this.shortModDescription(item)))
      .build();
    pagination.render(mods().topLevelMods(), page, 8).forEach(ctx.getSender()::sendMessage);
  }

  private void executeListChildren(final CommandContext<Commander> ctx) {
    final ModDescription mod = ctx.get(MOD_ARGUMENT_KEY);
    final int page = ctx.get(PAGE_ARGUMENT_KEY);
    if (mod.children().isEmpty()) {
      final TextComponent.Builder message = text()
        .color(MUSTARD)
        .content("Mod ")
        .append(text()
          .append(coloredBoldModName(mod))
          .apply(this.modClickAndHover(mod)))
        .append(text(" does not have any child mods!"));
      ctx.getSender().sendMessage(message);
      return;
    }
    final Pagination<ModDescription> pagination = Pagination.<ModDescription>builder()
      .header((currentPage, pages) -> text()
        .color(MUSTARD)
        .append(coloredBoldModName(mod))
        .append(text(" child mods")))
      .footer(this.footerRenderer(p -> String.format("/%s info %s children %s", this.label, mod.modId(), p)))
      .pageOutOfRange(ModsCommand::pageOutOfRange)
      .item((item, lastOfPage) -> Component.textOfChildren(DASH, this.shortModDescription(item)))
      .build();
    pagination.render(mod.children(), page, 8).forEach(ctx.getSender()::sendMessage);
  }

  private void executeSearch(final CommandContext<Commander> ctx) {
    final String rawQuery = ctx.getOptional(QUERY_ARGUMENT_KEY).orElse("").toLowerCase(Locale.ENGLISH).trim();
    final String[] split = rawQuery.split(" ");
    int page = 1;
    String tempQuery = rawQuery;
    if (split.length > 1) {
      try {
        final String pageText = split[split.length - 1];
        page = Integer.parseInt(pageText);
        tempQuery = rawQuery.substring(0, Math.max(rawQuery.lastIndexOf(pageText) - 1, 0));
      } catch (final NumberFormatException ex) {
        page = 1;
        tempQuery = rawQuery;
      }
    }
    final String query = tempQuery;

    final List<ModDescription> results = mods().allMods()
      .filter(matchesQuery(query))
      .flatMap(match -> Stream.concat(match.parentStream(), match.selfAndChildren()))
      .distinct()
      .sorted(comparing(ModDescription::modId))
      .toList();
    if (results.isEmpty()) {
      ctx.getSender().sendMessage(
        text()
          .color(MUSTARD)
          .content("No results for query '")
          .append(text(query, PURPLE))
          .append(text("'."))
      );
      return;
    }
    final Pagination<ModDescription> pagination = Pagination.<ModDescription>builder()
      .header((currentPage, pages) -> Component.textOfChildren(
        text()
          .decorate(BOLD)
          .append(text(results.size(), PINK))
          .append(text(" results for query", PURPLE)),
        GRAY_SEPARATOR,
        space(),
        text(query, MUSTARD)
      ))
      .footer(this.footerRenderer(p -> String.format("/%s search %s %d", this.label, query, p)))
      .pageOutOfRange(ModsCommand::pageOutOfRange)
      .item((item, lastOfPage) -> Component.textOfChildren(DASH, this.shortModDescription(item)))
      .build();
    pagination.render(results, page, 8).forEach(ctx.getSender()::sendMessage);
  }

  private static Predicate<ModDescription> matchesQuery(final String query) {
    final String queryLower = query.toLowerCase(Locale.ENGLISH);
    return mod -> mod.modId().toLowerCase(Locale.ENGLISH).contains(queryLower)
      || mod.name().toLowerCase(Locale.ENGLISH).contains(queryLower)
      || "clientsided client-sided client sided".contains(queryLower) && mod.environment() == Environment.CLIENT
      || "serversided server-sided server sided".contains(queryLower) && mod.environment() == Environment.SERVER
      || mod.authors().stream().anyMatch(author -> author.toLowerCase(Locale.ENGLISH).contains(queryLower));
  }

  private BiIntFunction<ComponentLike> footerRenderer(final IntFunction<String> commandFunction) {
    return (currentPage, pages) -> {
      if (pages == 1) {
        return empty(); // we don't need to see 'Page 1/1'
      }
      final TextComponent.Builder builder = text()
        .color(MUSTARD)
        .content("Page ")
        .append(text(currentPage, PURPLE))
        .append(text('/'))
        .append(text(pages, PURPLE));
      if (currentPage > 1) {
        builder.append(space())
          .append(previousPageButton(currentPage, commandFunction));
      }
      if (currentPage < pages) {
        builder.append(space())
          .append(nextPageButton(currentPage, commandFunction));
      }
      return builder;
    };
  }

  private static Component previousPageButton(final int currentPage, final IntFunction<String> commandFunction) {
    return text()
      .content("←")
      .color(BRIGHT_BLUE)
      .clickEvent(runCommand(commandFunction.apply(currentPage - 1)))
      .hoverEvent(text("Click for previous page.", EMERALD))
      .build();
  }

  private static Component nextPageButton(final int currentPage, final IntFunction<String> commandFunction) {
    return text()
      .content("→")
      .color(BRIGHT_BLUE)
      .clickEvent(runCommand(commandFunction.apply(currentPage + 1)))
      .hoverEvent(text("Click for next page.", EMERALD))
      .build();
  }

  private static Component pageOutOfRange(final int currentPage, final int pages) {
    return text()
      .color(MUSTARD)
      .content("Page ")
      .append(text(currentPage, PURPLE))
      .append(text(" is out of range"))
      .append(text('!'))
      .append(text(" There are only "))
      .append(text(pages, PURPLE))
      .append(text(" pages of results."))
      .build();
  }

  private void executeModInfo(final CommandContext<Commander> ctx) {
    final ModDescription mod = ctx.get(MOD_ARGUMENT_KEY);
    final TextComponent.Builder builder = text()
      .append(coloredBoldModName(mod))
      .color(MUSTARD)
      .append(newline())
      .append(space())
      .append(labelled("mod id", text(mod.modId())));

    if (!mod.version().isEmpty()) {
      builder.append(newline())
        .append(space())
        .append(labelled("version", text(mod.version())));
    }
    if (!mod.description().isEmpty()) {
      builder.append(newline())
        .append(space())
        .append(labelled("description", text(mod.description())));
    }
    if (!mod.authors().isEmpty()) {
      builder.append(newline())
        .append(space())
        .append(labelled(
          "authors",
          mod.authors().stream()
            .map(Component::text)
            .collect(toComponent(text(", ", GRAY)))
        ));
    }
    if (!mod.contributors().isEmpty()) {
      builder.append(newline())
        .append(space())
        .append(labelled(
          "contributors",
          mod.contributors().stream()
            .map(Component::text)
            .collect(toComponent(text(", ", GRAY)))
        ));
    }
    if (!mod.licenses().isEmpty()) {
      builder.append(newline())
        .append(space())
        .append(labelled(
          "license",
          mod.licenses().stream()
            .map(Component::text)
            .collect(toComponent(text(", ", GRAY)))
        ));
    }
    builder.append(newline())
      .append(space())
      .append(labelled("type", text(mod.type())));
    if (mod.environment() != Environment.UNIVERSAL) { // should be fine
      builder.append(newline())
        .append(space())
        .append(labelled("environment", mod.environment().display()));
    }
    final @Nullable ModDescription parent = mod.parent();
    if (parent != null) {
      builder.append(newline())
        .append(space())
        .append(labelled(
          "parent mod",
          text()
            .content(parent.modId())
            .apply(this.modClickAndHover(parent))
        ));
    }
    if (!mod.children().isEmpty()) {
      builder.append(newline())
        .append(space())
        .append(labelled(
          "child mods",
          mod.children().stream()
            .limit(5)
            .map(this::modIdWithClickAndHover)
            .collect(toComponent(text(", ", GRAY)))
        ));
      if (mod.children().size() > 5) {
        builder.append(
          text()
            .color(GRAY)
            .decorate(ITALIC)
            .content(", and " + (mod.children().size() - 5) + " more...")
            .hoverEvent(text()
              .color(EMERALD)
              .content("Click to see all of ")
              .append(coloredBoldModName(mod))
              .append(text("'s child mods."))
              .build())
            .clickEvent(runCommand(String.format("/%s info %s children", this.label, mod.modId())))
        );
      }
    }
    if (!mod.contact().isEmpty()) {
      builder.append(newline())
        .append(space())
        .append(labelled("contact", empty()));
      mod.contact().forEach((key, value) -> {
        builder.append(newline());
        final TextComponent.Builder info = text()
          .append(space())
          .append(DASH)
          .append(labelled(key, openUrlOrCopyToClipboard(value)));
        builder.append(info);
      });
    }
    ctx.getSender().sendMessage(builder);
  }

  private static Component labelled(final String label, final ComponentLike value) {
    final TextComponent.Builder builder = text()
      .append(text(label, BLUE))
      .append(GRAY_SEPARATOR);
    if (value != empty()) {
      builder.append(space())
        .append(value);
    }
    return builder.build();
  }

  private static Component openUrlOrCopyToClipboard(final String value) {
    final TextComponent.Builder builder = text()
      .content(value)
      .color(BRIGHT_BLUE);
    final Matcher matcher = URL_PATTERN.matcher(value);
    if (matcher.find() && matcher.group().equals(value)) {
      builder.hoverEvent(text("Click to open url!", EMERALD));
      builder.clickEvent(openUrl(value));
      builder.decorate(UNDERLINED);
    } else {
      builder.hoverEvent(text("Click to copy to clipboard!", EMERALD));
      builder.clickEvent(copyToClipboard(value));
    }
    return builder.build();
  }

  private Component shortModDescription(final ModDescription mod) {
    final TextComponent.Builder builder = text()
      .apply(this.modClickAndHover(mod))
      .append(text(mod.name(), BLUE))
      .append(text(String.format(" (%s)", mod.modId()), GRAY, ITALIC));
    if (!mod.version().isEmpty()) {
      builder.append(text(String.format(" v%s", mod.version()), EMERALD));
    }
    if (!mod.children().isEmpty()) {
      final String mods = mod.children().size() == 1 ? "mod" : "mods";
      builder.append(text(String.format(" (%d child %s)", mod.childrenStream().count(), mods), GRAY, ITALIC));
    }
    return builder.build();
  }

  private Component modIdWithClickAndHover(final ModDescription mod) {
    return text()
      .content(mod.modId())
      .apply(this.modClickAndHover(mod))
      .build();
  }

  private Consumer<? super ComponentBuilder<?, ?>> modClickAndHover(final ModDescription mod) {
    return builder ->
      builder.clickEvent(this.modInfo(mod))
        .hoverEvent(text()
          .color(EMERALD)
          .content("Click to see more about ")
          .append(coloredBoldModName(mod))
          .append(text('!'))
          .build());
  }

  private static TextComponent coloredBoldModName(final ModDescription mod) {
    return text(mod.name(), PURPLE, BOLD);
  }

  private ClickEvent modInfo(final ModDescription description) {
    return runCommand(String.format("/%s info %s", this.label, description.modId()));
  }
}
