package xyz.jpenilla.modscommand;

import cloud.commandframework.CommandManager;
import org.checkerframework.checker.nullness.qual.NonNull;

interface RegistrableCommand {
  void register(final @NonNull CommandManager<Commander> commandManager);
}
