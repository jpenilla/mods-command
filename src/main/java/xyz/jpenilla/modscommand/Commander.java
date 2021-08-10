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

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.platform.fabric.FabricClientAudiences;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;

interface Commander extends ForwardingAudience.Single {
  final class ClientCommander implements Commander {
    private final FabricClientCommandSource source;

    ClientCommander(final @NonNull FabricClientCommandSource source) {
      this.source = source;
    }

    public @NonNull FabricClientCommandSource source() {
      return this.source;
    }

    @Override
    public @NonNull Audience audience() {
      return FabricClientAudiences.of().audience();
    }
  }

  final class ServerCommander implements Commander {
    private final CommandSourceStack source;
    private final Audience audience;

    ServerCommander(final @NonNull CommandSourceStack source) {
      this.source = source;
      this.audience = FabricServerAudiences.of(this.source.getServer()).audience(this.source);
    }

    public @NonNull CommandSourceStack source() {
      return this.source;
    }

    @Override
    public @NonNull Audience audience() {
      return this.audience;
    }
  }
}
