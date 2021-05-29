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

import cloud.commandframework.fabric.FabricCommandManager;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

import static cloud.commandframework.minecraft.extras.AudienceProvider.nativeAudience;

final class Commands {
  private Commands() {
  }

  static void configureCommandManager(final @NonNull FabricCommandManager<Commander, ?> manager) {
    manager.brigadierManager().setNativeNumberSuggestions(false);

    new MinecraftExceptionHandler<Commander>()
      .withDefaultHandlers()
      .apply(manager, nativeAudience());

    ModDescriptionArgument.registerParser(manager);
  }
}
