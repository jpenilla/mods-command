/*
 * Mods Command
 * Copyright (c) 2020 Jason Penilla
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

import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import net.kyori.examination.string.StringExaminer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static net.kyori.adventure.text.Component.text;
import static xyz.jpenilla.modscommand.Colors.EMERALD;

interface ModDescription extends Examinable {
  @Nullable ModDescription parent();

  @NonNull List<@NonNull ModDescription> children();

  @NonNull String modId();

  @NonNull String name();

  @NonNull String version();

  @NonNull String type();

  @NonNull String description();

  @NonNull Collection<String> authors();

  @NonNull Collection<String> contributors();

  @NonNull Collection<String> licenses();

  @NonNull Map<@NonNull String, @NonNull String> contact();

  @NonNull Environment environment();

  default @NonNull Stream<@NonNull ModDescription> parentStream() {
    final ModDescription parent = this.parent();
    if (parent == null) {
      return Stream.empty();
    }
    return parent.selfAndParents();
  }

  default @NonNull Stream<@NonNull ModDescription> selfAndParents() {
    return Stream.concat(Stream.of(this), this.parentStream());
  }

  default @NonNull Stream<@NonNull ModDescription> childrenStream() {
    return this.children().stream().flatMap(ModDescription::selfAndChildren);
  }

  default @NonNull Stream<@NonNull ModDescription> selfAndChildren() {
    return Stream.concat(Stream.of(this), this.childrenStream());
  }

  default @NonNull Stream<@NonNull ExaminableProperty> examinableProperties() {
    final ModDescription parent = this.parent();
    return Stream.of(
      ExaminableProperty.of("modId", this.modId()),
      ExaminableProperty.of("name", this.name()),
      ExaminableProperty.of("version", this.version()),
      ExaminableProperty.of("type", this.type()),
      ExaminableProperty.of("description", this.description()),
      ExaminableProperty.of("authors", this.authors()),
      ExaminableProperty.of("contributors", this.contributors()),
      ExaminableProperty.of("licenses", this.licenses()),
      ExaminableProperty.of("contact", this.contact()),
      ExaminableProperty.of("environment", this.environment()),
      ExaminableProperty.of("parent", parent == null ? null : parent.modId()),
      ExaminableProperty.of("children", this.children())
    );
  }

  enum Environment {
    CLIENT(text()
      .content("client")
      .hoverEvent(text("Only runs on the client.", EMERALD))),
    SERVER(text()
      .content("server")
      .hoverEvent(text("Only runs on dedicated servers.", EMERALD))),
    UNIVERSAL(text()
      .content("universal")
      .hoverEvent(text("Can run on the client or on dedicated servers.", EMERALD)));

    private final Component display;

    Environment(final @NonNull ComponentLike display) {
      this.display = display.asComponent();
    }

    public @NonNull Component display() {
      return this.display;
    }
  }

  abstract class AbstractModDescription implements ModDescription {
    private final List<ModDescription> children = new ArrayList<>();
    private ModDescription parent = null;

    protected AbstractModDescription(final @NonNull List<@NonNull ModDescription> children) {
      for (final ModDescription child : children) {
        this.addChild(child);
      }
    }

    public void addChild(final @NonNull ModDescription newChild) {
      this.children.add(newChild);
      if (!(newChild instanceof AbstractModDescription)) {
        throw new IllegalArgumentException(String.format("Cannot add non-AbstractModDescription as a child. Attempted to add %s '%s'.", newChild.getClass().getSimpleName(), newChild));
      }
      ((AbstractModDescription) newChild).parent = this;
    }

    @Override
    public @Nullable ModDescription parent() {
      return this.parent;
    }

    @Override
    public @NonNull List<@NonNull ModDescription> children() {
      return Collections.unmodifiableList(this.children);
    }

    @Override
    public String toString() {
      return StringExaminer.simpleEscaping().examine(this);
    }
  }

  final class ModDescriptionImpl extends AbstractModDescription {
    private final String modId;
    private final String name;
    private final String version;
    private final String type;
    private final String description;
    private final Collection<String> authors;
    private final Collection<String> contributors;
    private final Collection<String> licenses;
    private final Map<String, String> contact;
    private final Environment environment;

    ModDescriptionImpl(
      final @NonNull List<@NonNull ModDescription> children,
      final @NonNull String modId,
      final @NonNull String name,
      final @NonNull String version,
      final @NonNull String type,
      final @NonNull String description,
      final @NonNull Collection<@NonNull String> authors,
      final @NonNull Collection<@NonNull String> contributors,
      final @NonNull Collection<@NonNull String> licenses,
      final @NonNull Map<@NonNull String, @NonNull String> contact,
      final @NonNull Environment environment
    ) {
      super(children);
      this.modId = modId;
      this.name = name;
      this.version = version;
      this.type = type;
      this.description = description;
      this.authors = authors;
      this.contributors = contributors;
      this.licenses = licenses;
      this.contact = contact;
      this.environment = environment;
    }

    @Override
    public @NonNull String modId() {
      return this.modId;
    }

    @Override
    public @NonNull String name() {
      return this.name;
    }

    @Override
    public @NonNull String version() {
      return this.version;
    }

    @Override
    public @NonNull String type() {
      return this.type;
    }

    @Override
    public @NonNull String description() {
      return this.description;
    }

    @Override
    public @NonNull Collection<@NonNull String> authors() {
      return this.authors;
    }

    @Override
    public @NonNull Collection<@NonNull String> contributors() {
      return this.contributors;
    }

    @Override
    public @NonNull Collection<@NonNull String> licenses() {
      return this.licenses;
    }

    @Override
    public @NonNull Map<@NonNull String, @NonNull String> contact() {
      return this.contact;
    }

    @Override
    public @NonNull Environment environment() {
      return this.environment;
    }
  }

  final class WrappingModDescription extends AbstractModDescription {
    private final ModMetadata metadata;

    WrappingModDescription(
      final @NonNull ModMetadata metadata,
      final @NonNull ModDescription @NonNull ... children
    ) {
      super(Arrays.asList(children));
      this.metadata = metadata;
    }

    public @NonNull ModMetadata wrapped() {
      return this.metadata;
    }

    @Override
    public @NonNull String modId() {
      return this.metadata.getId();
    }

    @Override
    public @NonNull String name() {
      return this.metadata.getName();
    }

    @Override
    public @NonNull String version() {
      return this.metadata.getVersion().getFriendlyString();
    }

    @Override
    public @NonNull String type() {
      return this.metadata.getType();
    }

    @Override
    public @NonNull String description() {
      return this.metadata.getDescription();
    }

    @Override
    public @NonNull Collection<@NonNull String> authors() {
      return this.metadata.getAuthors().stream()
        .map(Person::getName)
        .collect(toList());
    }

    @Override
    public @NonNull Collection<@NonNull String> contributors() {
      return this.metadata.getContributors().stream()
        .map(Person::getName)
        .collect(toList());
    }

    @Override
    public @NonNull Collection<@NonNull String> licenses() {
      return this.metadata.getLicense();
    }

    @Override
    public @NonNull Map<@NonNull String, @NonNull String> contact() {
      return this.metadata.getContact().asMap();
    }

    @Override
    public @NonNull Environment environment() {
      return fromFabric(this.metadata.getEnvironment());
    }

    @Override
    public @NonNull Stream<@NonNull ExaminableProperty> examinableProperties() {
      return Stream.concat(super.examinableProperties(), Stream.of(ExaminableProperty.of("metadata", this.metadata)));
    }

    private static @NonNull Environment fromFabric(final @NonNull ModEnvironment modEnvironment) {
      if (modEnvironment == ModEnvironment.CLIENT) {
        return Environment.CLIENT;
      } else if (modEnvironment == ModEnvironment.SERVER) {
        return Environment.SERVER;
      } else if (modEnvironment == ModEnvironment.UNIVERSAL) {
        return Environment.UNIVERSAL;
      } else {
        throw new RuntimeException("Unknown environment type " + modEnvironment);
      }
    }
  }
}
