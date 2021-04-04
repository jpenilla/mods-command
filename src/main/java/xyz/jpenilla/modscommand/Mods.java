package xyz.jpenilla.modscommand;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import xyz.jpenilla.modscommand.ModDescription.WrappingModDescription;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

enum Mods {
  MODS;

  private static final String FABRIC_API_MOD_ID = "fabric";
  private static final String FABRIC_API_MODULE_MARKER = "fabric-api:module-lifecycle";
  private static final String LOOM_GENERATED_MARKER = "fabric-loom:generated";

  private final Map<String, ModDescription> mods = new LinkedHashMap<>();

  private void loadModDescriptions() {
    this.mods.clear();

    final FabricLoader loader = FabricLoader.getInstance();
    final Map<String, ModDescription> descriptions = loader.getAllMods().stream()
      .map(ModContainer::getMetadata)
      .map(WrappingModDescription::new)
      .sorted(comparing(ModDescription::modId))
      .collect(toLinkedHashMap());

    final ModDescription fapi = descriptions.get(FABRIC_API_MOD_ID);
    if (fapi != null) {
      final List<ModDescription> fapiModules = descriptions.values().stream()
        .filter(it -> it instanceof WrappingModDescription && ((WrappingModDescription) it).wrapped().containsCustomValue(FABRIC_API_MODULE_MARKER))
        .collect(toList());
      fapiModules.forEach(module -> descriptions.remove(module.modId()));
      fapi.children().addAll(fapiModules);
    }

    final List<ModDescription> loomGenerated = descriptions.values().stream()
      .filter(it -> it instanceof WrappingModDescription && ((WrappingModDescription) it).wrapped().containsCustomValue(LOOM_GENERATED_MARKER))
      .collect(toList());
    loomGenerated.forEach(module -> descriptions.remove(module.modId()));
    if (!loomGenerated.isEmpty()) {
      descriptions.put(
        "loom-generated",
        new ModDescription.ModDescriptionImpl(
          loomGenerated,
          "loom-generated",
          "Loom Generated",
          "1.0",
          "category",
          "Parent mod to all Loom-generated library mods.",
          Collections.emptyList(),
          Collections.emptyList(),
          Collections.emptyList(),
          Collections.emptyMap(),
          ModDescription.Environment.UNIVERSAL
        )
      );
    }

    this.mods.putAll(
      descriptions.values().stream()
        .sorted(comparing(ModDescription::modId))
        .collect(toLinkedHashMap())
    );
  }

  private static @NotNull Collector<ModDescription, ?, LinkedHashMap<String, ModDescription>> toLinkedHashMap() {
    return toMap(
      ModDescription::modId,
      UnaryOperator.identity(),
      (e1, e2) -> {
        throw new RuntimeException(String.format("Two mods with the same id? ('%s' and '%s')", e1, e2));
      },
      LinkedHashMap::new
    );
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
