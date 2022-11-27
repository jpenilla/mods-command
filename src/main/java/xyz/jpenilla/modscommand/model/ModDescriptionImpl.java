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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
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
