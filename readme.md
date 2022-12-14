# Character Renderer

A client-side Fabric mod that renders characters in Minecraft.
Either press `C` or enter one of the `/characterrenderer` commands to open the GUI.

## Commands

Commands with this mod are client side only, so you don't need this on the server and don't need any special permissions on the server.

`/characterrenderer player <playername>` Shows the character renderer GUI for the specified player.

`/characterrenderer player <playername> [<playername_skin>]` Shows the character renderer GUI for the specified player, with the skin of the player in the second argument.

*If the targeted player is out of render distance, their equipment won't be visible.*

`/characterrenderer view` Shows the character renderer GUI for the living entity, you are looking at.

`/characterrenderer entity <entity_id>` Shows the character renderer GUI for the living entity, you specified.

## Configuration

`.minecraft/config/characterrenderer/characterrenderer.properties`

| Name            | Default                          | Description                          |
|:----------------|:---------------------------------|:-------------------------------------|
| `save_folder`   | `.minecraft/character_renderer/` | The folder where renders are stored. |
| `render_width`  | `2000`                           | The width of the rendered image.     |
| `render_height` | `2000`                           | The height of the rendered image.    |