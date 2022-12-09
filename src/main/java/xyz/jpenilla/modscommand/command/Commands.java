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
package xyz.jpenilla.modscommand.command;

import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.fabric.FabricCommandManager;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import xyz.jpenilla.modscommand.command.argument.ModDescriptionArgument;

import static cloud.commandframework.minecraft.extras.AudienceProvider.nativeAudience;

@DefaultQualifier(NonNull.class)
public final class Commands {
  private Commands() {
  }

  public static void configureCommandManager(final @NonNull FabricCommandManager<Commander, ?> manager) {
    manager.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor<>(
      FilteringCommandSuggestionProcessor.Filter.<Commander>contains(true).andTrimBeforeLastSpace()
    ));

    manager.brigadierManager().setNativeNumberSuggestions(false);

    new MinecraftExceptionHandler<Commander>()
      .withDefaultHandlers()
      .apply(manager, nativeAudience());

    ModDescriptionArgument.registerParser(manager);
  }
}
