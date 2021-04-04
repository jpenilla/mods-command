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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static net.kyori.adventure.text.Component.text;
import static xyz.jpenilla.modscommand.Colors.EMERALD;

interface ModDescription extends Examinable {
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

  @NonNull Environment environment();

  default @NonNull Stream<@NonNull ModDescription> selfAndChildren() {
    return Stream.concat(Stream.of(this), this.children().stream());
  }

  default @NonNull Stream<@NonNull ExaminableProperty> examinableProperties() {
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
    @Override
    public String toString() {
      return StringExaminer.simpleEscaping().examine(this);
    }
  }

  final class ModDescriptionImpl extends AbstractModDescription {
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
    private final Environment environment;

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
      final @NonNull Map<@NonNull String, @NonNull String> contact,
      final @NonNull Environment environment
    ) {
      this.children.addAll(children);
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
    public @NonNull Collection<@NonNull Person> authors() {
      return this.authors;
    }

    @Override
    public @NonNull Collection<@NonNull Person> contributors() {
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
