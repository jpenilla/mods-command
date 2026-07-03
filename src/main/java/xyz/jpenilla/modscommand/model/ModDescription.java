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
package xyz.jpenilla.modscommand.model;

import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface ModDescription {
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

  default boolean hasAttribute(final TypeToken<?> type) {
    return false;
  }

  default boolean hasAttribute(final Class<?> type) {
    return this.hasAttribute(TypeToken.get(type));
  }

  default <A> A attribute(final TypeToken<A> type) {
    throw new IllegalArgumentException();
  }

  default <A> A attribute(final Class<A> type) {
    return this.attribute(TypeToken.get(type));
  }

  default Stream<ModDescription> parentStream() {
    final ModDescription parent = this.parent();
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

  static ModDescription fromFabric(final ModMetadata fabric) {
    return new FabricModMetadataModDescription(fabric);
  }

  static ModDescription create(
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
    return new ModDescriptionImpl(children, modId, name, version, type, description, authors, contributors, licenses, contact, environment);
  }
}
