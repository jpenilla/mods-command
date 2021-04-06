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
