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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.kyori.examination.string.StringExaminer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Comparator.comparing;

@DefaultQualifier(NonNull.class)
public abstract class AbstractModDescription implements ModDescription {
  private final List<ModDescription> children = new ArrayList<>();
  private @Nullable ModDescription parent = null;

  protected AbstractModDescription(final List<ModDescription> children) {
    for (final ModDescription child : children) {
      this.addChild(child);
    }
  }

  public void addChild(final ModDescription newChild) {
    if (!(newChild instanceof AbstractModDescription newChildAbs)) {
      throw new IllegalArgumentException(String.format("Cannot add non-AbstractModDescription as a child. Attempted to add %s '%s'.", newChild.getClass().getName(), newChild));
    }
    newChildAbs.parent = this;
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
