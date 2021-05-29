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
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.SimpleCloudKey;
import cloud.commandframework.permission.CommandPermission;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.toComponent;
import static net.kyori.adventure.text.event.ClickEvent.copyToClipboard;
import static net.kyori.adventure.text.event.ClickEvent.openUrl;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;
import static xyz.jpenilla.modscommand.Colors.BLUE;
import static xyz.jpenilla.modscommand.Colors.BRIGHT_BLUE;
import static xyz.jpenilla.modscommand.Colors.EMERALD;
import static xyz.jpenilla.modscommand.Colors.MIDNIGHT_BLUE;
import static xyz.jpenilla.modscommand.Colors.MUSTARD;
import static xyz.jpenilla.modscommand.Colors.PINK;
import static xyz.jpenilla.modscommand.Colors.PURPLE;
import static xyz.jpenilla.modscommand.Mods.mods;

final class ModsCommand implements RegistrableCommand {
  private static final CloudKey<ModDescription> MOD_ARGUMENT_KEY = SimpleCloudKey.of("mod_id", TypeToken.get(ModDescription.class));
  private static final CloudKey<Integer> PAGE_ARGUMENT_KEY = SimpleCloudKey.of("page_number", TypeToken.get(Integer.class));
  private static final CloudKey<String> QUERY_ARGUMENT_KEY = SimpleCloudKey.of("query", TypeToken.get(String.class));
  private static final Pattern URL_PATTERN = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/\\S*)?"); // copied from adventure-text-serializer-legacy
  private static final Component GRAY_SEPARATOR = text(':', GRAY);
  private static final Component DASH = text(" - ", MIDNIGHT_BLUE);

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
      mods.literal("page")
        .argument(pageArgument())
        .handler(this::executeListMods)
    );
    final Command.Builder<Commander> info = mods.literal("info")
      .argument(ModDescriptionArgument.of(MOD_ARGUMENT_KEY.getName()));
    manager.command(
      info.handler(this::executeModInfo)
    );
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
  }

  private static @NonNull CommandArgument<Commander, Integer> pageArgument() {
    return IntegerArgument.<Commander>newBuilder(PAGE_ARGUMENT_KEY.getName())
      .withMin(1)
      .asOptionalWithDefault(1)
      .build();
  }

  private void executeListMods(final @NonNull CommandContext<Commander> ctx) {
    final int page = ctx.getOptional(PAGE_ARGUMENT_KEY).orElse(1);
    final Pagination<ModDescription> pagination = Pagination.<ModDescription>builder()
      .header((currentPage, pages) -> TextComponent.ofChildren(
        text("Loaded Mods", PURPLE, BOLD),
        text(String.format(" (%s total, %s top-level)", mods().allMods().count(), mods().topLevelMods().size()), GRAY, ITALIC)
      ))
      .footer(this.footerRenderer(p -> String.format("/%s page %d", this.label, p)))
      .pageOutOfRange(ModsCommand::pageOutOfRange)
      .item((item, lastOfPage) -> TextComponent.ofChildren(DASH, this.shortModDescription(item)))
      .build();
    pagination.render(mods().topLevelMods(), page, 8).forEach(ctx.getSender()::sendMessage);
  }

  private void executeListChildren(final @NonNull CommandContext<Commander> ctx) {
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
      .item((item, lastOfPage) -> TextComponent.ofChildren(DASH, this.shortModDescription(item)))
      .build();
    pagination.render(mod.children(), page, 8).forEach(ctx.getSender()::sendMessage);
  }

  private void executeSearch(final @NonNull CommandContext<Commander> ctx) {
    final String rawQuery = ctx.getOptional(QUERY_ARGUMENT_KEY).orElse("").toLowerCase(Locale.ROOT).trim();
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
      .collect(toList());
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
      .header((currentPage, pages) -> TextComponent.ofChildren(
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
      .item((item, lastOfPage) -> TextComponent.ofChildren(DASH, this.shortModDescription(item)))
      .build();
    pagination.render(results, page, 8).forEach(ctx.getSender()::sendMessage);
  }

  private static @NonNull Predicate<ModDescription> matchesQuery(final @NonNull String query) {
    return mod -> mod.modId().toLowerCase(Locale.ROOT).contains(query)
      || mod.name().toLowerCase(Locale.ROOT).contains(query)
      || "clientsided client-sided client sided".contains(query) && mod.environment() == ModDescription.Environment.CLIENT
      || "serversided server-sided server sided".contains(query) && mod.environment() == ModDescription.Environment.SERVER
      || mod.authors().stream().anyMatch(author -> author.toLowerCase(Locale.ROOT).contains(query));
  }

  private @NonNull BiFunction<@NonNull Integer, @NonNull Integer, @NonNull ComponentLike> footerRenderer(final @NonNull IntFunction<String> commandFunction) {
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

  private static @NonNull Component previousPageButton(final int currentPage, final @NonNull IntFunction<String> commandFunction) {
    return text()
      .content("←")
      .color(BRIGHT_BLUE)
      .clickEvent(runCommand(commandFunction.apply(currentPage - 1)))
      .hoverEvent(text("Click for previous page.", EMERALD))
      .build();
  }

  private static @NonNull Component nextPageButton(final int currentPage, final @NonNull IntFunction<String> commandFunction) {
    return text()
      .content("→")
      .color(BRIGHT_BLUE)
      .clickEvent(runCommand(commandFunction.apply(currentPage + 1)))
      .hoverEvent(text("Click for next page.", EMERALD))
      .build();
  }

  private static @NonNull Component pageOutOfRange(final int currentPage, final int pages) {
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

  private void executeModInfo(final @NonNull CommandContext<Commander> ctx) {
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
    if (mod.environment() != ModDescription.Environment.UNIVERSAL) { // should be fine
      builder.append(newline())
        .append(space())
        .append(labelled("environment", mod.environment().display()));
    }
    final ModDescription parent = mod.parent();
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

  private static @NonNull Component labelled(final @NonNull String label, final @NonNull ComponentLike value) {
    final TextComponent.Builder builder = text()
      .append(text(label, BLUE))
      .append(GRAY_SEPARATOR);
    if (value != empty()) {
      builder.append(space())
        .append(value);
    }
    return builder.build();
  }

  private static @NonNull Component openUrlOrCopyToClipboard(final @NonNull String value) {
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

  private @NonNull Component shortModDescription(final @NonNull ModDescription mod) {
    final TextComponent.Builder builder = text()
      .apply(this.modClickAndHover(mod))
      .append(text(mod.name(), BLUE))
      .append(text(String.format(" (%s)", mod.modId()), GRAY, ITALIC));
    if (!mod.version().isEmpty()) {
      builder.append(text(String.format(" v%s", mod.version()), EMERALD));
    }
    if (!mod.children().isEmpty()) {
      builder.append(text(String.format(" (%d child mods)", mod.childrenStream().count()), GRAY, ITALIC));
    }
    return builder.build();
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
          .color(EMERALD)
          .content("Click to see more about ")
          .append(coloredBoldModName(mod))
          .append(text('!'))
          .build());
  }

  private static @NonNull TextComponent coloredBoldModName(final @NonNull ModDescription mod) {
    return text(mod.name(), PURPLE, BOLD);
  }

  private @NonNull ClickEvent modInfo(final @NonNull ModDescription description) {
    return runCommand(String.format("/%s mod %s", this.label, description.modId()));
  }
}
