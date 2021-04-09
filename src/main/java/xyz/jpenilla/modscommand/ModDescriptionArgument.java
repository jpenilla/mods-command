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

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static xyz.jpenilla.modscommand.Mods.mods;

final class ModDescriptionArgument extends CommandArgument<Commander, ModDescription> {
  private ModDescriptionArgument(
    final boolean required,
    final @NonNull String name,
    final @NonNull String defaultValue,
    final @Nullable BiFunction<CommandContext<Commander>, String, List<String>> suggestionsProvider,
    final @NonNull ArgumentDescription defaultDescription
  ) {
    super(
      required,
      name,
      new Parser(),
      defaultValue,
      ModDescription.class,
      suggestionsProvider,
      defaultDescription
    );
  }

  public static void registerParser(final @NonNull CommandManager<Commander> manager) {
    manager.getParserRegistry().registerParserSupplier(
      TypeToken.get(ModDescription.class),
      parserParameters -> new Parser()
    );
  }

  public static @NonNull CommandArgument<Commander, ModDescription> of(final @NonNull String name) {
    return builder(name).build();
  }

  public static @NonNull CommandArgument<Commander, ModDescription> optional(final @NonNull String name) {
    return builder(name).asOptional().build();
  }

  public static @NonNull Builder builder(final @NonNull String name) {
    return new Builder(name);
  }

  public static final class Parser implements ArgumentParser<Commander, ModDescription> {
    @Override
    public @NonNull ArgumentParseResult<@NonNull ModDescription> parse(final @NonNull CommandContext<Commander> commandContext, final @NonNull Queue<@NonNull String> inputQueue) {
      final ModDescription meta = mods().findMod(Objects.requireNonNull(inputQueue.peek(), "inputQueue.peek() returned null"));
      if (meta != null) {
        inputQueue.remove();
        return ArgumentParseResult.success(meta);
      }
      return ArgumentParseResult.failure(new IllegalArgumentException(String.format(
        "No mod with id '%s'.",
        inputQueue.peek()
      )));
    }

    @Override
    public @NonNull List<@NonNull String> suggestions(final @NonNull CommandContext<Commander> commandContext, final @NonNull String input) {
      return mods().allMods()
        .map(ModDescription::modId)
        .collect(Collectors.toList());
    }
  }

  public static final class Builder extends TypedBuilder<Commander, ModDescription, Builder> {
    private Builder(final @NonNull String name) {
      super(ModDescription.class, name);
    }

    @Override
    public @NonNull CommandArgument<Commander, ModDescription> build() {
      return new ModDescriptionArgument(
        this.isRequired(),
        this.getName(),
        this.getDefaultValue(),
        this.getSuggestionsProvider(),
        this.getDefaultDescription()
      );
    }
  }
}
