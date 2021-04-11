# Mods Command

![version badge](https://img.shields.io/github/v/release/jpenilla/ModsCommand?label=version)

A Fabric mod adding commands to list, search, and get information about installed mods. Requires [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api).

### Commands
Command | Description | Permission
--------|-------------|-----------
`/mods` | List, search, and get information about installed mods. | `modscommand.mods`
`/dumpmods` | Dumps the list of installed mods and some information about the current environment to `installed-mods.yml` in the game directory. When used in game, the contents of the file can be copied to the clipboard by clicking a chat message. This is a diagnostics command, meant to aid in creating more useful bug reports. | `modscommand.dumpmods`

### Client Commands
Client commands are commands which are processed on the client and never sent to the server.

Command | Description
--------|------------
`/clientmods` | Same as `/mods`, but client-side.
`/dumpclientmods` | Same as `/dumpmods`, but client-side.

### Configuration
Mods Command can be configured though the `mods-command.conf` generated in the `config` directory after the first run.
