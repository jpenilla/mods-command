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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.text;
import static xyz.jpenilla.modscommand.util.Colors.EMERALD;

@DefaultQualifier(NonNull.class)
public enum Environment {
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
