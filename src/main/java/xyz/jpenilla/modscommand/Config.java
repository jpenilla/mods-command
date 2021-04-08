package xyz.jpenilla.modscommand;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public final class Config {
  private HiddenMods hiddenMods = new HiddenMods();

  @ConfigSerializable
  public static final class HiddenMods {
    @Comment("Set the list of mod-ids to hide/ignore.")
    private List<String> hiddenModIds = new ArrayList<>();
  }

  public @NonNull List<String> hiddenModIds() {
    return this.hiddenMods.hiddenModIds;
  }
}
