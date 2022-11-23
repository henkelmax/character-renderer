# Character Renderer

A client-side Fabric mod that renders characters in Minecraft.
Either press `C` or enter the `/characterrenderer` command to open the GUI.

## Commands

Commands with this mod are client side only, so you don't need this on the server and don't need any special permissions on the server.

`/characterrenderer <playername>` Shows the character renderer GUI for the specified player.

*If the targeted player is out of render distance, their equipment won't be visible.*

## Configuration

`.minecraft/config/characterrenderer/characterrenderer.properties`

| Name            | Default                          | Description                          |
|:----------------|:---------------------------------|:-------------------------------------|
| `save_folder`   | `.minecraft/character_renderer/` | The folder where renders are stored. |
| `render_width`  | `2000`                           | The width of the rendered image.     |
| `render_height` | `2000`                           | The height of the rendered image.    |