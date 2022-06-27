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

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import xyz.jpenilla.modscommand.ModDescription.WrappingModDescription;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

@DefaultQualifier(NonNull.class)
final class Mods {
  private static final String QSL_MOD_ID = "qsl";
  private static final String FABRIC_API_MOD_ID = "fabric";
  private static final String QUILTED_FABRIC_API_MOD_ID = "quilted_fabric_api";
  private static final String FABRIC_API_MODULE_MARKER = "fabric-api:module-lifecycle";
  private static final String LOOM_GENERATED_MARKER = "fabric-loom:generated";

  private final Set<ModDescription> mods;
  private final Map<String, ModDescription> modsById;
  private final Map<String, ModDescription> rootMods;

  private Mods() {
    this.rootMods = loadModDescriptions();
    this.mods = this.rootMods.values().stream().flatMap(ModDescription::selfAndChildren).collect(toUnmodifiableSet());
    this.modsById = this.mods.stream().collect(toUnmodifiableMap(ModDescription::modId, Function.identity()));
  }

  public @Nullable ModDescription findMod(final String modId) {
    return this.modsById.get(modId);
  }

  public int modCount() {
    return this.mods.size();
  }

  public Stream<ModDescription> allMods() {
    return this.mods.stream();
  }

  public Collection<ModDescription> topLevelMods() {
    return this.rootMods.values();
  }

  public static Mods mods() {
    return Holder.INSTANCE;
  }

  @SuppressWarnings("UnstableApiUsage")
  private static Map<String, ModDescription> loadModDescriptions() {
    final FabricLoader loader = FabricLoader.getInstance();

    final Set<String> hiddenModIds = ModsCommandModInitializer.instance().config().hiddenModIds();

    final Map<String, ModDescription> descriptions = loader.getAllMods().stream()
      .map(ModContainer::getMetadata)
      .map(WrappingModDescription::new)
      .filter(mod -> !hiddenModIds.contains(mod.modId()))
      .collect(toMap(ModDescription::modId, UnaryOperator.identity()));

    arrangeChildModsUsingModMenuMetadata(descriptions);
    arrangeQSLChildren(descriptions);
    arrangeQFapiChildren(descriptions);
    arrangeFapiChildren(descriptions);
    arrangeLoomGenerated(descriptions);

    return ImmutableMap.<String, ModDescription>builder()
      .orderEntriesByValue(comparing(ModDescription::modId))
      .putAll(descriptions)
      .build();
  }

  private static void arrangeChildModsUsingModMenuMetadata(final Map<String, ModDescription> descriptions) {
    final Map<String, List<ModDescription>> byParent = new HashMap<>();
    descriptions.values().forEach(modDescription -> {
      final @Nullable String parent = parentUsingModMenuMetadata(modDescription);
      if (parent == null) {
        return;
      }
      byParent.computeIfAbsent(parent, $ -> new ArrayList<>()).add(modDescription);
    });
    byParent.forEach((parentId, children) -> {
      final @Nullable ModDescription parent = descriptions.get(parentId);
      if (parent == null) {
        return;
      }
      for (final ModDescription child : children) {
        descriptions.remove(child.modId());
        ((ModDescription.AbstractModDescription) parent).addChild(child);
      }
    });
  }

  private static void arrangeQSLChildren(final Map<String, ModDescription> descriptions) {
    final List<ModDescription> qslModules = findChildrenUsingModMenuMetadata(QSL_MOD_ID, descriptions);
    if (qslModules.isEmpty()) {
      return;
    }
    // QSL mod may not exist (in case of qfapi qsl)
    final ModDescription qsl = descriptions.computeIfAbsent(QSL_MOD_ID, id -> new ModDescription.ModDescriptionImpl(
      qslModules,
      id,
      "Quilt Standard Libraries",
      "",
      "quilt",
      "A set of libraries to assist in making Quilt mods.",
      List.of("QuiltMC: QSL Team"),
      emptyList(),
      emptyList(),
      emptyMap(),
      ModDescription.Environment.UNIVERSAL
    ));
    qslModules.forEach(module -> {
      descriptions.remove(module.modId());
      if (!qsl.children().contains(module)) {
        ((WrappingModDescription) qsl).addChild(module);
      }
    });
  }

  private static void arrangeQFapiChildren(final Map<String, ModDescription> descriptions) {
    final @Nullable ModDescription qfapi = descriptions.get(QUILTED_FABRIC_API_MOD_ID);
    if (qfapi != null) {
      final List<ModDescription> qfapiModules = descriptions.values().stream()
        .filter(it -> {
          return it instanceof WrappingModDescription
            && ((WrappingModDescription) it).wrapped().containsCustomValue(FABRIC_API_MODULE_MARKER)
            && it.modId().startsWith("quilted_"); // not ideal, but works
        })
        .toList();
      qfapiModules.forEach(module -> {
        descriptions.remove(module.modId());
        ((ModDescription.AbstractModDescription) qfapi).addChild(module);
      });
    }
  }

  private static void arrangeFapiChildren(final Map<String, ModDescription> descriptions) {
    final @Nullable ModDescription fapi = descriptions.get(FABRIC_API_MOD_ID);
    if (fapi != null) {
      final List<ModDescription> fapiModules = descriptions.values().stream()
        .filter(it -> it instanceof WrappingModDescription && ((WrappingModDescription) it).wrapped().containsCustomValue(FABRIC_API_MODULE_MARKER))
        .toList();
      fapiModules.forEach(module -> {
        descriptions.remove(module.modId());
        ((ModDescription.AbstractModDescription) fapi).addChild(module);
      });
    }
  }

  private static void arrangeLoomGenerated(final Map<String, ModDescription> descriptions) {
    final List<ModDescription> loomGeneratedMods = descriptions.values().stream()
      .filter(it -> it instanceof WrappingModDescription && ((WrappingModDescription) it).wrapped().containsCustomValue(LOOM_GENERATED_MARKER))
      .toList();
    loomGeneratedMods.forEach(module -> descriptions.remove(module.modId()));
    if (!loomGeneratedMods.isEmpty()) {
      descriptions.put(
        "loom-generated",
        new ModDescription.ModDescriptionImpl(
          loomGeneratedMods,
          "loom-generated",
          "Loom Generated",
          "",
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
  }

  private static List<ModDescription> findChildrenUsingModMenuMetadata(final String parentId, final Map<String, ModDescription> descriptions) {
    return descriptions.values().stream()
      .filter(description -> parentId.equals(parentUsingModMenuMetadata(description)))
      .toList();
  }

  private static @Nullable String parentUsingModMenuMetadata(final ModDescription modDescription) {
    if (!(modDescription instanceof WrappingModDescription wrapping)) {
      return null;
    }
    if (!wrapping.wrapped().containsCustomValue("modmenu")
      || !wrapping.wrapped().getCustomValue("modmenu").getAsObject().containsKey("parent")) {
      return null;
    }
    final CustomValue parent = wrapping.wrapped()
      .getCustomValue("modmenu")
      .getAsObject()
      .get("parent");
    if (parent.getType() == CustomValue.CvType.STRING) {
      return parent.getAsString();
    } else {
      return parent.getAsObject().get("id").getAsString();
    }
  }

  private static final class Holder {
    static final Mods INSTANCE = new Mods();
  }
}
