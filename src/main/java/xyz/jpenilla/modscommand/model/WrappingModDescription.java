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
package xyz.jpenilla.modscommand.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.kyori.examination.ExaminableProperty;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class WrappingModDescription extends AbstractModDescription {
  private final ModMetadata metadata;

  public WrappingModDescription(
    final ModMetadata metadata,
    final ModDescription... children
  ) {
    super(Arrays.asList(children));
    this.metadata = metadata;
  }

  public ModMetadata wrapped() {
    return this.metadata;
  }

  @Override
  public String modId() {
    return this.metadata.getId();
  }

  @Override
  public String name() {
    return this.metadata.getName();
  }

  @Override
  public String version() {
    return this.metadata.getVersion().getFriendlyString();
  }

  @Override
  public String type() {
    return this.metadata.getType();
  }

  @Override
  public String description() {
    return this.metadata.getDescription();
  }

  @Override
  public Collection<String> authors() {
    return this.metadata.getAuthors().stream()
      .map(Person::getName)
      .toList();
  }

  @Override
  public Collection<String> contributors() {
    return this.metadata.getContributors().stream()
      .map(Person::getName)
      .toList();
  }

  @Override
  public Collection<String> licenses() {
    return this.metadata.getLicense();
  }

  @Override
  public Map<String, String> contact() {
    return this.metadata.getContact().asMap();
  }

  @Override
  public Environment environment() {
    return fromFabric(this.metadata.getEnvironment());
  }

  @Override
  public Stream<ExaminableProperty> examinableProperties() {
    return Stream.concat(super.examinableProperties(), Stream.of(ExaminableProperty.of("metadata", this.metadata)));
  }

  private static Environment fromFabric(final ModEnvironment modEnvironment) {
    return switch (modEnvironment) {
      case CLIENT -> Environment.CLIENT;
      case SERVER -> Environment.SERVER;
      case UNIVERSAL -> Environment.UNIVERSAL;
    };
  }
}
