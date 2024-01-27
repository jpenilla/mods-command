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
package xyz.jpenilla.modscommand.command.argument.parser;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import xyz.jpenilla.modscommand.command.Commander;
import xyz.jpenilla.modscommand.model.ModDescription;

import static org.incendo.cloud.parser.ArgumentParseResult.failure;
import static org.incendo.cloud.parser.ArgumentParseResult.success;
import static xyz.jpenilla.modscommand.model.Mods.mods;

@DefaultQualifier(NonNull.class)
public final class ModDescriptionParser implements ArgumentParser<Commander, ModDescription>, BlockingSuggestionProvider.Strings<Commander> {
  public static void registerParser(final CommandManager<Commander> manager) {
    manager.parserRegistry().registerParserSupplier(
      TypeToken.get(ModDescription.class),
      parserParameters -> new ModDescriptionParser()
    );
  }

  public static ParserDescriptor<Commander, ModDescription> modDescriptionParser() {
    return ParserDescriptor.of(new ModDescriptionParser(), ModDescription.class);
  }

  @Override
  public ArgumentParseResult<ModDescription> parse(final CommandContext<Commander> commandContext, final CommandInput input) {
    final String read = input.readString();
    final @Nullable ModDescription meta = mods().findMod(read);
    if (meta != null) {
      return success(meta);
    }
    return failure(new IllegalArgumentException(
      String.format("No mod with id '%s'.", read)
    ));
  }

  @Override
  public Iterable<String> stringSuggestions(final CommandContext<Commander> commandContext, final CommandInput input) {
    return mods().allMods()
      .map(ModDescription::modId)
      .toList();
  }
}
