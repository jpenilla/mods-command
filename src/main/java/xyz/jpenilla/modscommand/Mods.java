package xyz.jpenilla.modscommand;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.jpenilla.modscommand.ModDescription.WrappingModDescription;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum Mods {
  MODS;

  private static final String FABRIC_API_MOD_ID = "fabric";
  private static final String FABRIC_API_MODULE_MARKER = "fabric-api:module-lifecycle";

  private final Map<String, ModDescription> mods = new HashMap<>();

  private void loadModDescriptions() {
    this.mods.clear();

    final FabricLoader loader = FabricLoader.getInstance();
    final Map<String, ModDescription> descriptions = loader.getAllMods().stream()
      .map(ModContainer::getMetadata)
      .map(WrappingModDescription::new)
      .collect(Collectors.toMap(ModDescription::modId, UnaryOperator.identity()));

    final ModDescription fapi = descriptions.get(FABRIC_API_MOD_ID);
    if (fapi != null) {
      final List<ModDescription> fapiModules = descriptions.values().stream()
        .filter(it -> it instanceof WrappingModDescription && ((WrappingModDescription) it).wrapped().containsCustomValue(FABRIC_API_MODULE_MARKER))
        .collect(Collectors.toList());
      fapiModules.forEach(module -> descriptions.remove(module.modId()));
      fapi.children().addAll(fapiModules);
    }

    this.mods.putAll(descriptions);
  }

  private @NonNull Map<@NonNull String, @NonNull ModDescription> mods() {
    if (this.mods.isEmpty()) {
      synchronized (this) {
        if (this.mods.isEmpty()) {
          this.loadModDescriptions();
        }
      }
    }
    return this.mods;
  }

  public @Nullable ModDescription findMod(final @NonNull String modId) {
    final ModDescription potential = this.mods().get(modId);
    if (potential != null) {
      return potential;
    }
    return this.mods().values().stream()
      .flatMap(desc -> desc.children().stream())
      .filter(it -> it.modId().equals(modId))
      .findFirst()
      .orElse(null);
  }

  public @NonNull Stream<@NonNull ModDescription> allMods() {
    return this.mods().values().stream()
      .flatMap(ModDescription::selfAndChildren);
  }

  public @NonNull Collection<@NonNull ModDescription> topLevelMods() {
    return this.mods().values();
  }
}
