package xyz.jpenilla.modscommand;

import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

interface ModDescription {
  @NonNull List<@NonNull ModDescription> children();

  @NonNull String modId();

  @NonNull String name();

  @NonNull String version();

  @NonNull String type();

  @NonNull String description();

  @NonNull Collection<Person> authors();

  @NonNull Collection<Person> contributors();

  @NonNull Collection<String> licenses();

  @NonNull Map<@NonNull String, @NonNull String> contact();

  default @NonNull Stream<@NonNull ModDescription> selfAndChildren() {
    return Stream.concat(Stream.of(this), this.children().stream());
  }

  final class ModDescriptionImpl implements ModDescription {
    private final List<ModDescription> children = new ArrayList<>();
    private final String modId;
    private final String name;
    private final String version;
    private final String type;
    private final String description;
    private final Collection<Person> authors;
    private final Collection<Person> contributors;
    private final Collection<String> licenses;
    private final Map<String, String> contact;

    ModDescriptionImpl(
      final @NonNull List<@NonNull ModDescription> children,
      final @NonNull String modId,
      final @NonNull String name,
      final @NonNull String version,
      final @NonNull String type,
      final @NonNull String description,
      final @NonNull Collection<@NonNull Person> authors,
      final @NonNull Collection<@NonNull Person> contributors,
      final @NonNull Collection<@NonNull String> licenses,
      final @NonNull Map<@NonNull String, @NonNull String> contact
    ) {
      this.modId = modId;
      this.name = name;
      this.version = version;
      this.type = type;
      this.description = description;
      this.authors = authors;
      this.contributors = contributors;
      this.licenses = licenses;
      this.contact = contact;
    }

    @Override
    public @NonNull List<@NonNull ModDescription> children() {
      return this.children;
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
    public @NonNull Collection<Person> authors() {
      return this.authors;
    }

    @Override
    public @NonNull Collection<Person> contributors() {
      return this.contributors;
    }

    @Override
    public @NonNull Collection<String> licenses() {
      return this.licenses;
    }

    @Override
    public @NonNull Map<@NonNull String, @NonNull String> contact() {
      return this.contact;
    }
  }

  final class WrappingModDescription implements ModDescription {
    private final ModMetadata metadata;
    private final List<ModDescription> children;

    public WrappingModDescription(
      final @NonNull ModMetadata metadata,
      final @NonNull ModDescription @NonNull ... children
    ) {
      this.metadata = metadata;
      this.children = new ArrayList<>(Arrays.asList(children));
    }

    public @NonNull ModMetadata wrapped() {
      return this.metadata;
    }

    @Override
    public @NonNull List<@NonNull ModDescription> children() {
      return this.children;
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
    public @NonNull Collection<Person> authors() {
      return this.metadata.getAuthors();
    }

    @Override
    public @NonNull Collection<Person> contributors() {
      return this.metadata.getContributors();
    }

    @Override
    public @NonNull Collection<String> licenses() {
      return this.metadata.getLicense();
    }

    @Override
    public @NonNull Map<@NonNull String, @NonNull String> contact() {
      return this.metadata.getContact().asMap();
    }
  }
}
