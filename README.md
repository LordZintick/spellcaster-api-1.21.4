# SpellCaster API
SpellCaster API is a Fabric mod to load spells from JSON files.<br>
While [you can add new spells with a datapack](url), the mod itself contains a plethora of default spells that you can use anytime.<br>
**Note that this mod may not be very balanced, proceed with caution in large modpacks or servers**

## List of Default Spells (With a description and sorted alphabetically)
> [!CAUTION]
> \* \- Exercise caution with disabled cooldowns/mana costs or in Creative Mode, as these spells can possibly crash the server if they are used rapidly

- Arrow Blast* - 2 second cooldown; 50 mana - Shoots a blast of arrows in every direction
  - Ingredients: Arrow and TNT
- Arrow Cluster* - 2 second cooldown; 50 mana - Shoots a cluster of arrows in front of you
  - Ingredients: Arrow and Cobblestone
- Arrow Stream* - 3 second cooldown; 40 mana - Shoots a stream of arrows in front of you
  - Ingredients: Arrow and Iron Ingot
- Big Fireball - 1.5 second cooldown; 35 mana - Shoots an explosive ghast fireball in front of you
  - Ingredients: TNT and Fire Charge
- Cookie Blast* - 2 second cooldown; 50 mana - Causes a huge explosion of cookies
  - Ingredients: Cookie and TNT
- Dash - 6 second cooldown; 50 mana - Gives you super speed for a few seconds
  - Ingredients: Sugar and Sugar
- Dragon Barrage* - 3.5 second cooldown; 50 mana - Shoots a barrage of dragon fireballs in front of you
  - Ingredients: Dragon's Breath, Fire Charge, and Bow
- Dragon Fireball - 1 second cooldown; 15 mana - Shoots a dragon fireball that explodes into a cloud of toxic mist in front of you
  - Ingredients: Dragon's Breath and Fire Charge
- Explosive Barrage* - 4.5 second cooldown; 65 mana - Shoots a barrage of explosive ghast fireballs in front of you
  - Ingredients: Fire Charge, TNT, and Bow
- Fang Circle - 5 second cooldown; 50 mana - Summons a circle of evoker fangs around you
  - Ingredients: Shears and Diamond
- Fang Line - 2 second cooldown; 25 mana - Summons a line of evoker fangs in front of you
  - Ingredients: Shears and Gold Ingot
- Fireball - 0.5 second cooldown; 5 mana - Shoots a small blaze fireball that ignites the block it hits
  - Ingredient: Fire Charge
- Fireball Barrage* - 3.5 second cooldown; 50 mana - Shoots a barrage of small blaze fireballs that ignite the blocks they hit
  - Ingredients: Fire Charge and Bow
- Gigantism - 10 second cooldown; 100 mana - Creative Only - Makes you ridiculously large for a bit
  - Ingredients: Command Block and Enchanted Golden Apple
- Grow - 5 second cooldown; 25 mana - Makes you bigger for a bit
  - Ingredients: Seeds and Golden Apple
- Heal - 2.5 second cooldown; 25 mana - Instantly heals a bit of your health
  - Ingredient: Glistering Melon
- Invulnerability - 0.5 second cooldown; 100 mana - Creative Only - Makes you completely invulnerable to damage
  - Ingredients: Shield and Command BLock
- Large Fang Circle - 6 second cooldown; 75 mana - Summons a massive circle of evoker fangs around you
  - Ingredients: Shears, Diamond, and Emerald
- Leap - 6 second cooldown; 50 mana - Grants you super jump for a short time
  - Ingredients: Slime Block and Slime Ball
- Long Fang Line - 3 second cooldown; 50 mana - Summons a long line of evoker fangs in front of you
  - Ingredients: Shears, Gold Ingot, and Emerald
- Night Vision - 5 second cooldown; 50 mana - Grants night vision for two minutes
  - Ingredients: Golden Carrot
- Regenerate - 5 second cooldown; 50 mana - Regenerates some health over eight seconds
  - Ingredients: Ghast Tear
- Resist - 6 second cooldown; 100 mana - Grants fire resistance and general damage resistance for a short time
  - Ingredients: Shield and Blaze Powder
- Saturate - 5 second cooldown; 50 mana - Refills your hunger
  - Ingredients: Steak, Cooked Mutton, and Cooked Chicken
- Shrink - 5 second cooldown; 25 mana - Shrinks you down to half your normal size
  - Ingredients: Golden Apple and Rotten Flesh
- Smoke Screen - 2.5 second cooldown; 50 mana - Makes you and nearby players invisible, and creates a smoke screen around you
  - Ingredients: White Wool and Glass
- Spectral Blast* - 2 second cooldown; 50 mana - Shoots a blast of spectral arrows in every direction
  - Ingredients: Spectral Arrow and TNT
- Spectral Cluster* - 2 second cooldown; 50 mana - Shoots a cluster of spectral arrows in front of you
  - Ingredients: Spectral Arrow and Cobblestone
- Spectral Stream* - 3 second cooldown; 40 mana - Shoots a stream of spectral arrows where you are looking
  - Ingredients: Spectral Arrow and Iron Ingot
- Speedy Fireball - 1 second cooldown; 10 mana - Shoots a fast blaze fireball that ignites the block it hits
  - Ingredients: Fire Charge and Sugar
- Teleport - 1.5 second cooldown; 15 mana - Shoots an ender pearl that teleports you to where it lands
  - Ingredients: Ender Pearl
- Timeslow - 15 second cooldown; 100 mana - Creative Only - Slows time to half speed for 10 seconds
  - Ingredients: Clock, Fermented Spider Eye, and Command Block
- Timestop - 15 second cooldown; 100 mana - Creative Only - Stops time (you and other players can still move) for 10 seconds
  - Ingredients: Clock, Ice, and Command Block
- TNT Shot - 2 second cooldown; 25 mana - Shoots a lit tnt in front of you
  - Ingredients: TNT
- Wither Skull - 1 second cooldown; 20 mana - Shoots a wither skull that slightly explodes when it hits something in front of you
  - Ingredients: Wither Skeleton Skull

## How to add a new spell
To add a new spell, first create a new datapack as normal.<br>
Your datapck should look something like this:
```
- ExampleDataPack <-- name this whatever you want
  - pack.mcmeta <-- setup as normal
  - data
    - example <-- replace this with your namespace
```
Next, create a `spells` folder inside your namespace folder (the `example` folder).<br>
Your datapack should now look something like this:
```
- ExampleDataPack
  - pack.mcmeta
  - data
    - example
      - spells
```
Now, create a `.json` file inside your `spells` folder to hold all of your spells.<br>
This can be named whatever, but make sure it is inside your `spells` folder!<br>
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
    + Example: `spellcaster-api:fireball(1.0, false)` will look for a spell action with id `fireball` with a `Float` parameter and a `Boolean` parameter in the spell provider with namespace `spellcaster-api` (the default provided spell actions). It will then trigger the method, providing `1.0` as the first parameter, and `false` as the second.

You just created a spell file! This file you just created creates a spell with a 1-second cooldown and a mana cost of 10 that shoots a small blaze fireball with a speed of 1! In order to craft it, you combine a stick and a diamond.<br>
You can now go into the game and test your new spell! (if you already had the game open, use the `/reload` command)
You can add as many spells as you want, feel free to add other ones!
