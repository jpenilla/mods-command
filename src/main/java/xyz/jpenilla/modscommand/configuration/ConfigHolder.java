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
package xyz.jpenilla.modscommand.configuration;

import io.leangen.geantyref.TypeToken;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

@DefaultQualifier(NonNull.class)
public final class ConfigHolder<C> {
  private final TypeToken<C> configType;
  private final Path configFile;
  private final ConfigurationLoader<?> configLoader;
  private volatile @Nullable C config;

  private ConfigHolder(
    final Path file,
    final TypeToken<C> configType
  ) {
    this.configType = configType;
    this.configFile = file;
    this.configLoader = createLoader(this.configFile);
  }

  public C config() {
    final @Nullable C config = this.config;
    if (config == null) {
      throw new IllegalStateException("Config is not loaded (null)");
    }
    return config;
  }

  public @Nullable C configIfLoaded() {
    return this.config;
  }

  public synchronized void load() throws IOException {
    @Nullable C loaded = null;
    @Nullable IOException fail = null;
    try {
      if (!Files.exists(this.configFile.getParent())) {
        Files.createDirectories(this.configFile.getParent());
      }
      final ConfigurationNode load = this.configLoader.load();
      loaded = load.get(this.configType);
    } catch (final IOException ex) {
      fail = ex;
    }
    if (loaded == null) {
      if (fail == null) {
        fail = new IOException("Failed to coerce loaded config node to correct type %s".formatted(this.configType.getType().getTypeName()));
      }
      throw new IOException("Failed to load config file %s".formatted(this.configFile), fail);
    }
    this.config = loaded;

    try {
      this.configLoader.save(this.configLoader.createNode(node -> node.set(this.config)));
    } catch (final IOException ex) {
      throw new IOException("Failed to write back loaded config file %s".formatted(this.configFile), ex);
    }
  }

  private static HoconConfigurationLoader createLoader(final Path file) {
    return HoconConfigurationLoader.builder()
      .path(file)
      .build();
  }

  public static <C> ConfigHolder<C> create(final Path file, final TypeToken<C> configType) {
    return new ConfigHolder<>(file, configType);
  }

  public static <C> ConfigHolder<C> create(final Path file, final Class<C> configType) {
    return new ConfigHolder<>(file, TypeToken.get(configType));
  }

  public static <C> ConfigHolder<C> create(final ModContainer modContainer, final TypeToken<C> configType) {
    final Path file = FabricLoader.getInstance().getConfigDir()
      .resolve(modContainer.getMetadata().getId() + ".conf");
    return create(file, configType);
  }

  public static <C> ConfigHolder<C> create(final ModContainer modContainer, final Class<C> configType) {
    return create(modContainer, TypeToken.get(configType));
  }
}
