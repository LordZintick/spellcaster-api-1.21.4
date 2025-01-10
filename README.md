# SpellCaster API
SpellCaster API is a small Fabric mod to load spells from JSON files.<br>
Only a datapack is required to add new spells, but a mod is required to add new functionality.<br>
## How to add a new spell
To add a new spell, first create a new datapack as normal.<br>
Your datapck should look something like this:
```
- ExampleDataPack <-- name this whatever you want
  - pack.mcmeta <-- setup as normal
  - data
    - example <-- replace this with your namespace
```
Next, create a ```spells``` folder inside your namespace folder (the ```example``` folder).<br>
Your datapack should now look something like this:
```
- ExampleDataPack
  - pack.mcmeta
  - data
    - example
      - spells
```
Now, create a ```.json``` file inside your ```spells``` folder to hold all of your spells.<br>
This can be named whatever, but make sure it is inside your ```spells``` folder!<br>
Your datapack should now look something like this:
```
- ExampleDataPack
  - pack.mcmeta
  - data
    - example
      - spells
        - my-spell.json <-- name this whatever you want
```
Now, open up your spell file and type this into it:
```
{
  "displayName": "My Spell",
  "cooldown": 1.0,
  "description": "My cool spell!",
  "ingredients": [
    "minecraft:diamond"
  ],
  "actions": [
    "spellcaster-api:fireball(1.0, false)"
  ]
}
```
What was all that? Well, that was the spell JSON file! Here's a breakdown of what all that means:
+ `displayName` - This is the display name of your spell, what is shown in the wand tooltip
+ `cooldown` - This is the cooldown of your spell, in seconds
+ `description` - This is a description of what your spell does, it's shown underneath the `displayName` in the wand tooltip
+ `ingredients` - This is a list of identifiers for the ingredients needed to craft this spell (e.g. `"minecraft:dirt"` or `"minecraft:diamond"`).
+ `actions` - This is a list of identifiers for the actions this spell performs when cast. These are the most complicated, and you can't add new ones with a datapack (you need a mod).<br>
This mod provides some for you to use, and they have a special way you need to write them. They need to be written as `<spell provider namespace>:<spell action id>(<parameters>)`:
    + The `<spell provider namespace>` is the namespace of the spell provider (when using the default spell actions, this will be `spellcaster-api`)
    + The `<spell action id>` is the id of the spell action method inside of the spell provider class (Look at)
