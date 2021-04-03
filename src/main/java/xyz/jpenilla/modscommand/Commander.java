package xyz.jpenilla.modscommand;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.platform.fabric.AdventureCommandSourceStack;
import net.kyori.adventure.platform.fabric.FabricClientAudiences;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;

interface Commander extends ForwardingAudience.Single {
  final class ClientCommander implements Commander {
    private final FabricClientCommandSource source;

    ClientCommander(final @NonNull FabricClientCommandSource source) {
      this.source = source;
    }

    public FabricClientCommandSource source() {
      return this.source;
    }

    @Override
    public @NonNull Audience audience() {
      return FabricClientAudiences.of().audience();
    }
  }

  final class ServerCommander implements Commander {
    private final CommandSourceStack source;

    ServerCommander(final @NonNull CommandSourceStack source) {
      this.source = source;
    }

    public CommandSourceStack source() {
      return this.source;
    }

    @Override
    public @NonNull Audience audience() {
      return (AdventureCommandSourceStack) this.source;
    }
  }
}
