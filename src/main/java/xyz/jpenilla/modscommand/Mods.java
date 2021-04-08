package xyz.jpenilla.modscommand;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.jpenilla.modscommand.ModDescription.WrappingModDescription;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

final class Mods {
  private static final String FABRIC_API_MOD_ID = "fabric";
  private static final String FABRIC_API_MODULE_MARKER = "fabric-api:module-lifecycle";
  private static final String LOOM_GENERATED_MARKER = "fabric-loom:generated";

  private final Map<String, ModDescription> mods;

  private Mods() {
    this.mods = loadModDescriptions();
  }

  public @Nullable ModDescription findMod(final @NonNull String modId) {
    final ModDescription potential = this.mods.get(modId);
    if (potential != null) {
      return potential;
    }
    return this.mods.values().stream()
      .flatMap(ModDescription::childrenStream)
      .filter(it -> it.modId().equals(modId))
      .findFirst()
      .orElse(null);
  }

  public @NonNull Stream<@NonNull ModDescription> allMods() {
    return this.mods.values().stream()
      .flatMap(ModDescription::selfAndChildren);
  }

  public @NonNull Collection<@NonNull ModDescription> topLevelMods() {
    return this.mods.values();
  }

  public static @NonNull Mods mods() {
    return Holder.INSTANCE;
  }

  private static @NonNull Map<String, ModDescription> loadModDescriptions() {
    final FabricLoader loader = FabricLoader.getInstance();

    final List<String> hiddenModIds = ModsCommandModInitializer.instance().config().hiddenModIds();

    final Map<String, ModDescription> descriptions = loader.getAllMods().stream()
      .map(ModContainer::getMetadata)
      .map(WrappingModDescription::new)
      .filter(mod -> !hiddenModIds.contains(mod.modId()))
      .sorted(comparing(ModDescription::modId))
      .collect(toLinkedHashMap());

    final ModDescription fapi = descriptions.get(FABRIC_API_MOD_ID);
    if (fapi != null) {
      final List<ModDescription> fapiModules = descriptions.values().stream()
        .filter(it -> it instanceof WrappingModDescription && ((WrappingModDescription) it).wrapped().containsCustomValue(FABRIC_API_MODULE_MARKER))
        .collect(toList());
      fapiModules.forEach(module -> {
        descriptions.remove(module.modId());
        ((ModDescription.AbstractModDescription) fapi).addChild(module);
      });
    }

    final List<ModDescription> loomGeneratedMods = descriptions.values().stream()
      .filter(it -> it instanceof WrappingModDescription && ((WrappingModDescription) it).wrapped().containsCustomValue(LOOM_GENERATED_MARKER))
      .collect(toList());
    loomGeneratedMods.forEach(module -> descriptions.remove(module.modId()));
    if (!loomGeneratedMods.isEmpty()) {
      descriptions.put(
        "loom-generated",
        new ModDescription.ModDescriptionImpl(
          loomGeneratedMods,
          "loom-generated",
          "Loom Generated",
          "1.0",
          "category",
          "Parent mod to all Loom-generated library mods.",
          emptyList(),
          emptyList(),
          emptyList(),
          emptyMap(),
          ModDescription.Environment.UNIVERSAL
        )
      );
    }

    return ImmutableMap.<String, ModDescription>builder()
      .orderEntriesByValue(comparing(ModDescription::modId))
      .putAll(descriptions)
      .build();
  }

  private static @NonNull Collector<ModDescription, ?, LinkedHashMap<String, ModDescription>> toLinkedHashMap() {
    return toMap(
      ModDescription::modId,
      UnaryOperator.identity(),
      (e1, e2) -> {
        throw new RuntimeException(String.format("Two mods with the same id? ('%s' and '%s')", e1, e2));
      },
      LinkedHashMap::new
    );
  }

  private static final class Holder {
    static final Mods INSTANCE = new Mods();
  }
}
