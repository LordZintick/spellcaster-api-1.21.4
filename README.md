# SpellCaster API
SpellCaster API is a Fabric mod to load spells from JSON files.<br>
Only a datapack is required to add new spells, but a mod is required to add new functionality.<br>

## List of Default Spells (With a description and sorted alphabetically)
- Arrow Blast - 2 second cooldown; 50 mana - Shoots a blast of arrows in every direction
  - Ingredients: Arrow and TNT
- Arrow Cluster - 2 second cooldown; 50 mana - Shoots a cluster of arrows in front of you
  - Ingredients: Arrow and Cobblestone
- Arrow Stream - 3 second cooldown; 40 mana - Shoots a stream of arrows in front of you
  - Ingredients: Arrow and Iron Ingot
- Big Fireball - 1.5 second cooldown; 35 mana - Shoots an explosive ghast fireball in front of you
  - Ingredients: TNT and Fire Charge
- Cookie Blast - 2 second cooldown; 50 mana - Causes a huge explosion of cookies
  - Ingredients: Cookie and TNT
- Dash - 6 second cooldown; 50 mana - Gives you super speed for a few seconds
  - Ingredients: Sugar and Sugar
- Dragon Barrage - 3.5 second cooldown; 50 mana - Shoots a barrage of dragon fireballs in front of you
  - Ingredients: Dragon's Breath, Fire Charge, and Bow
- Dragon Fireball - 1 second cooldown; 15 mana - Shoots a dragon fireball that explodes into a cloud of toxic mist in front of you
  - Ingredients: Dragon's Breath and Fire Charge
- Explosive Barrage - 4.5 second cooldown; 65 mana - Shoots a barrage of explosive ghast fireballs in front of you
  - Ingredients: Fire Charge, TNT, and Bow
- Fang Circle - 5 second cooldown; 50 mana - Summons a circle of evoker fangs around you
  - Ingredients: Shears and Diamond
- Fang Line - 2 second cooldown; 25 mana - Summons a line of evoker fangs in front of you
  - Ingredients: Shears and Gold Ingot
- Fireball - to be continued

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
  "manaCost": 10,
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
+ `manaCost` - This is the mana cost of your spell (The player has a max mana of 100)
+ `description` - This is a description of what your spell does, it's shown underneath the `displayName` in the wand tooltip
+ `ingredients` - This is a list of identifiers for the ingredients needed to craft this spell (e.g. `"minecraft:dirt"` or `"minecraft:diamond"`).
+ `actions` - This is a list of identifiers for the actions this spell performs when cast.<br>
This is the most complicated part of making a spell, but once you get the hand of it, it is much easier than you think.<br>
You can't add new spell actions with just a datapack, that requires a mod.<br>
Spell actions have a special way you need to write them.<br>
They need to be written as `<spell provider namespace>:<spell action id>(<parameters>)`.
    + The `<spell provider namespace>` is the namespace of the spell provider (when using the default spell actions, this will be `spellcaster-api`)
    + The `<spell action id>` is the id of the spell action method inside of the spell provider class (Look at the [Default Spells](https://github.com/LordZintick/spellcaster-api-1.21.4/blob/master/src/main/java/com/lordkittycat/DefaultSpells.java) class to see all of the spell actions and ids)
    + The `<parameters>` is a comma-separated list of parameters to search for an action with (Look at the [Default Spells](https://github.com/LordZintick/spellcaster-api-1.21.4/blob/master/src/main/java/com/lordkittycat/DefaultSpells.java) class to see all of the spell actions and their required parameters)
    + Example: `spellcaster-api:fireball(1.0, false)` will look for a spell action with id `fireball` with a `Float` parameter and a `Boolean` parameter in the spell provider with namespace `spellcaster-api` (the default provided spell actions). It will then fire the method, providing `1.0` as the first parameter, and `false` as the second.

You just created a spell file! This file you just created creates a spell with a 1-second cooldown, that shoots a fireball! In order to craft it, you combine a stick and a diamond.<br>
You can now go into the game and test your new spell!
You can add as many spells as you want, feel free to add other ones!
