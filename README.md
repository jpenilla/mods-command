# Mods Command

[![build](https://img.shields.io/github/checks-status/jpenilla/ModsCommand/master?label=build)](https://github.com/jpenilla/ModsCommand/actions) [![license](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE) [![latest release](https://img.shields.io/github/v/release/jpenilla/ModsCommand)](https://github.com/jpenilla/ModsCommand/releases)

A Fabric mod adding commands to list, search, and get information about installed mods.
Requires [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api).

### Commands

*(Minecraft Command Syntax Reference: [Minecraft Wiki](https://minecraft.fandom.com/wiki/Commands#Syntax))*

Command | Description | Permission
--------|-------------|-----------
`/mods [page] [<page_number>]` | Displays a paginated view of installed mods. | `modscommand.mods`
`/mods info <mod_id>` | Displays detailed information about the specified mod. | `modscommand.mods`
`/mods info <mod_id> children [<page_number>]` | Displays a paginated view of child mods for the specified mod. | `modscommand.mods`
`/mods search <query> [<page_number>]` | Displays a paginated view of mods matching the search query. | `modscommand.mods`
`/dumpmods` | Dumps the list of installed mods and some information about the current environment to `installed-mods.yml` in the game directory. When used in game, the contents of the file can be copied to the clipboard by clicking a chat message. This is a diagnostics command, meant to aid in creating more useful bug reports. | `modscommand.dumpmods`

### Client Commands

Client commands are commands which are processed on the client and never sent to the server.

All of Mods Command's commands are usable as client commands.

- `/mods` and all subcommands are registered under `/clientmods` and `/modscommand:clientmods`.
- `/dumpmods` is registered as `/dumpclientmods` and `/modscommand:dumpclientmods`.

### Configuration

Mods Command can be configured though the `mods-command.conf` generated in the `config` directory after the first run.
