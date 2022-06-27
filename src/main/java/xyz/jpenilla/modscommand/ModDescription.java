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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
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
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Comparator.comparing;
import static net.kyori.adventure.text.Component.text;
import static xyz.jpenilla.modscommand.Colors.EMERALD;

@DefaultQualifier(NonNull.class)
interface ModDescription extends Examinable {
  @Nullable ModDescription parent();

  List<ModDescription> children();

  String modId();

  String name();

  String version();

  String type();

  String description();

  Collection<String> authors();

  Collection<String> contributors();

  Collection<String> licenses();

  Map<String, String> contact();

  Environment environment();

  default Stream<ModDescription> parentStream() {
    final @Nullable ModDescription parent = this.parent();
    if (parent == null) {
      return Stream.empty();
    }
    return parent.selfAndParents();
  }

  default Stream<ModDescription> selfAndParents() {
    return Stream.concat(Stream.of(this), this.parentStream());
  }

  default Stream<ModDescription> childrenStream() {
    return this.children().stream().flatMap(ModDescription::selfAndChildren);
  }

  default Stream<ModDescription> selfAndChildren() {
    return Stream.concat(Stream.of(this), this.childrenStream());
  }

  @Override
  default Stream<ExaminableProperty> examinableProperties() {
    final @Nullable ModDescription parent = this.parent();
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

    Environment(final ComponentLike display) {
      this.display = display.asComponent();
    }

    public Component display() {
      return this.display;
    }
  }

  abstract class AbstractModDescription implements ModDescription {
    private final List<ModDescription> children = new ArrayList<>();
    private @Nullable ModDescription parent = null;

    protected AbstractModDescription(final List<ModDescription> children) {
      for (final ModDescription child : children) {
        this.addChild(child);
      }
    }

    public void addChild(final ModDescription newChild) {
      if (!(newChild instanceof AbstractModDescription)) {
        throw new IllegalArgumentException(String.format("Cannot add non-AbstractModDescription as a child. Attempted to add %s '%s'.", newChild.getClass().getName(), newChild));
      }
      ((AbstractModDescription) newChild).parent = this;
      this.children.add(newChild);
      this.children.sort(comparing(ModDescription::modId));
    }

    @Override
    public @Nullable ModDescription parent() {
      return this.parent;
    }

    @Override
    public List<ModDescription> children() {
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
      final List<ModDescription> children,
      final String modId,
      final String name,
      final String version,
      final String type,
      final String description,
      final Collection<String> authors,
      final Collection<String> contributors,
      final Collection<String> licenses,
      final Map<String, String> contact,
      final Environment environment
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
    public String modId() {
      return this.modId;
    }

    @Override
    public String name() {
      return this.name;
    }

    @Override
    public String version() {
      return this.version;
    }

    @Override
    public String type() {
      return this.type;
    }

    @Override
    public String description() {
      return this.description;
    }

    @Override
    public Collection<String> authors() {
      return this.authors;
    }

    @Override
    public Collection<String> contributors() {
      return this.contributors;
    }

    @Override
    public Collection<String> licenses() {
      return this.licenses;
    }

    @Override
    public Map<String, String> contact() {
      return this.contact;
    }

    @Override
    public Environment environment() {
      return this.environment;
    }
  }

  final class WrappingModDescription extends AbstractModDescription {
    private final ModMetadata metadata;

    WrappingModDescription(
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
