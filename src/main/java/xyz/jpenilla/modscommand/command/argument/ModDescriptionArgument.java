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
package xyz.jpenilla.modscommand.command.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import io.leangen.geantyref.TypeToken;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiFunction;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import xyz.jpenilla.modscommand.command.Commander;
import xyz.jpenilla.modscommand.model.ModDescription;

import static cloud.commandframework.arguments.parser.ArgumentParseResult.failure;
import static cloud.commandframework.arguments.parser.ArgumentParseResult.success;
import static xyz.jpenilla.modscommand.model.Mods.mods;

@DefaultQualifier(NonNull.class)
public final class ModDescriptionArgument extends CommandArgument<Commander, ModDescription> {
  private ModDescriptionArgument(
    final boolean required,
    final String name,
    final String defaultValue,
    final @Nullable BiFunction<CommandContext<Commander>, String, List<String>> suggestionsProvider,
    final ArgumentDescription defaultDescription
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

  public static void registerParser(final CommandManager<Commander> manager) {
    manager.parserRegistry().registerParserSupplier(
      TypeToken.get(ModDescription.class),
      parserParameters -> new Parser()
    );
  }

  public static CommandArgument<Commander, ModDescription> of(final String name) {
    return builder(name).build();
  }

  public static CommandArgument<Commander, ModDescription> optional(final String name) {
    return builder(name).asOptional().build();
  }

  public static Builder builder(final String name) {
    return new Builder(name);
  }

  public static final class Parser implements ArgumentParser<Commander, ModDescription> {
    @Override
    public ArgumentParseResult<ModDescription> parse(final CommandContext<Commander> commandContext, final Queue<String> inputQueue) {
      final @Nullable ModDescription meta = mods().findMod(
        Objects.requireNonNull(inputQueue.peek(), "inputQueue.peek() returned null")
      );
      if (meta != null) {
        inputQueue.remove();
        return success(meta);
      }
      return failure(new IllegalArgumentException(
        String.format("No mod with id '%s'.", inputQueue.peek())
      ));
    }

    @Override
    public List<String> suggestions(final CommandContext<Commander> commandContext, final String input) {
      return mods().allMods()
        .map(ModDescription::modId)
        .toList();
    }
  }

  public static final class Builder extends TypedBuilder<Commander, ModDescription, Builder> {
    private Builder(final String name) {
      super(ModDescription.class, name);
    }

    @Override
    public CommandArgument<Commander, ModDescription> build() {
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
