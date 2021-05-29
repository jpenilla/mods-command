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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.BiFunction;

import static java.util.Objects.requireNonNull;
import static net.kyori.adventure.text.Component.empty;

interface Pagination<T> {
  @NonNull ComponentLike header(int page, int pages);

  @NonNull ComponentLike footer(int page, int pages);

  @NonNull ComponentLike pageOutOfRange(int page, int pages);

  @NonNull ComponentLike item(@NonNull T item, boolean lastOfPage);

  default @NonNull List<@NonNull Component> render(
    final @NonNull Collection<@NonNull T> content,
    final int page,
    final int itemsPerPage
  ) {
    if (content.isEmpty()) {
      throw new IllegalArgumentException("Cannot paginate an empty collection.");
    }

    final int pages = (int) Math.ceil(content.size() / (itemsPerPage * 1.00));
    if (page < 1 || page > pages) {
      return Collections.singletonList(this.pageOutOfRange(page, pages).asComponent());
    }

    final List<Component> renderedContent = new ArrayList<>();

    final Component header = this.header(page, pages).asComponent();
    if (header != empty()) {
      renderedContent.add(header);
    }

    final int start = itemsPerPage * (page - 1);
    final int maxIndex = start + itemsPerPage;

    if (content instanceof RandomAccess && content instanceof List) {
      final List<T> contentList = (List<T>) content;
      for (int i = start; i < maxIndex; i++) {
        if (i > content.size() - 1) {
          break;
        }
        renderedContent.add(this.item(contentList.get(i), i == maxIndex - 1).asComponent());
      }
    } else {
      final Iterator<T> iterator = content.iterator();
      for (int i = 0; i < start && iterator.hasNext(); i++) {
        iterator.next();
      }
      for (int i = start; i < maxIndex && iterator.hasNext(); ++i) {
        renderedContent.add(this.item(iterator.next(), i == maxIndex - 1).asComponent());
      }
    }

    final Component footer = this.footer(page, pages).asComponent();
    if (footer != empty()) {
      renderedContent.add(footer);
    }

    return Collections.unmodifiableList(renderedContent);
  }

  static <T> @NonNull Builder<T> builder() {
    return new Builder<>();
  }

  final class Builder<T> {
    private BiFunction<Integer, Integer, ComponentLike> headerRenderer = (currentPage, pages) -> empty();
    private BiFunction<Integer, Integer, ComponentLike> footerRenderer = (currentPage, pages) -> empty();
    private BiFunction<Integer, Integer, ComponentLike> pageOutOfRangeRenderer;
    private BiFunction<T, Boolean, ComponentLike> itemRenderer;

    private Builder() {
    }

    public @NonNull Builder<T> header(final @NonNull BiFunction<@NonNull Integer, @NonNull Integer, @NonNull ComponentLike> headerRenderer) {
      this.headerRenderer = headerRenderer;
      return this;
    }

    public @NonNull Builder<T> footer(final @NonNull BiFunction<@NonNull Integer, @NonNull Integer, @NonNull ComponentLike> footerRenderer) {
      this.footerRenderer = footerRenderer;
      return this;
    }

    public @NonNull Builder<T> pageOutOfRange(final @NonNull BiFunction<@NonNull Integer, @NonNull Integer, @NonNull ComponentLike> pageOutOfRangeRenderer) {
      this.pageOutOfRangeRenderer = pageOutOfRangeRenderer;
      return this;
    }

    public @NonNull Builder<T> item(final @NonNull BiFunction<@NonNull T, @NonNull Boolean, @NonNull ComponentLike> itemRenderer) {
      this.itemRenderer = itemRenderer;
      return this;
    }

    public @NonNull Pagination<T> build() {
      return new DelegatingPaginationImpl<>(
        requireNonNull(this.headerRenderer, "Must provide a header renderer!"),
        requireNonNull(this.footerRenderer, "Must provide a footer renderer!"),
        requireNonNull(this.pageOutOfRangeRenderer, "Must provide a page out of range renderer!"),
        requireNonNull(this.itemRenderer, "Must provide an item renderer!")
      );
    }

    private static final class DelegatingPaginationImpl<T> implements Pagination<T> {
      private final BiFunction<Integer, Integer, ComponentLike> headerRenderer;
      private final BiFunction<Integer, Integer, ComponentLike> footerRenderer;
      private final BiFunction<Integer, Integer, ComponentLike> pageOutOfRangeRenderer;
      private final BiFunction<T, Boolean, ComponentLike> itemRenderer;

      DelegatingPaginationImpl(
        final BiFunction<Integer, Integer, ComponentLike> headerRenderer,
        final BiFunction<Integer, Integer, ComponentLike> footerRenderer,
        final BiFunction<Integer, Integer, ComponentLike> pageOutOfRangeRenderer,
        final BiFunction<T, Boolean, ComponentLike> itemRenderer
      ) {
        this.headerRenderer = headerRenderer;
        this.footerRenderer = footerRenderer;
        this.pageOutOfRangeRenderer = pageOutOfRangeRenderer;
        this.itemRenderer = itemRenderer;
      }

      @Override
      public @NonNull ComponentLike header(final int page, final int pages) {
        return this.headerRenderer.apply(page, pages);
      }

      @Override
      public @NonNull ComponentLike footer(final int page, final int pages) {
        return this.footerRenderer.apply(page, pages);
      }

      @Override
      public @NonNull ComponentLike pageOutOfRange(final int page, final int pages) {
        return this.pageOutOfRangeRenderer.apply(page, pages);
      }

      @Override
      public @NonNull ComponentLike item(final @NonNull T item, final boolean lastOfPage) {
        return this.itemRenderer.apply(item, lastOfPage);
      }
    }
  }
}
