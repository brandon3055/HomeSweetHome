# Home Sweet Home Mod Mechanics

Everything that follows is configurable.

---

### Homes

Homes have the following properties.
- Their location
- Their homeliness
- Is home permanent
- Radius (this is a base value + homeliness multiplied by a multiplier)

### Home mechanics
At the start a player has no home.
When a player sleeps in a bed their home is set at that location.
This home is not permanent so if they sleep in a different location it will move and reset.

The more nights the player sleeps at a single home the more its homeliness increases.
Simply spending time within the home's radius will also increase homeliness but at a much slower rate.

Homeliness works similar to experience. As the players homeliness increases they will gain homeliness levels.
The player can view their home stats by looking at a bed or pressing the huf key (unbound by default).
Homeliness is displayed as a level and a progress bar similar to xp where the progress bar shows how close they are to the next level.

Once the homeliness reaches a certain threshold the player will be given an option to make the home permanent.
At this point the home can not be moved and some additional mechanics activate (more on that later)

Homeliness will be incremented logarithmically (similar to xp levels)
Meaning in the beginning its easy to increase homeliness but the the more 
homeliness increases the slower it increases. Once the player passes the level required
to make their home permanent the rate of gain will decrease significantly.
This is extended to make it easy for the player to get their home started but this is configurable. 

// Not yet fully implemented. 
// Homeliness level will not be the same everywhere within the homes radius but will instead
// be highest at the center of the home area and decrease towards the parameter of its radius.

// Not yet implemented. 
// Idea for moving homes (slightly)
// If the player sleeps outside the center of their home but within the home's radius the center could move toward that 
// location by some amount every time they sleep.

### Effects (all configurable)
All effects are configurable via in game commands. Start by running ``/home_sweet_home effect add``<br>
and it will walk you through the process of adding an effect.

Effects and effect conditions are fully configurable.
This mod uses minecraft potion effects and should support mod added effects.
Effect particles are disabled.

To add an effect you need to define the effect, effect amplifier, source , trigger value and some effects also require a duration.
the source plus the trigger value combine to create a condition under which the effect will be active.
The trigger value relates to different stats depending on the source.
The following is a list of all sources and what their trigger value relates to.

```
tiredness - For this source the trigger value is the number of minutes the player must be awake past the time until tired config in order for this effect to trigger. 
homesickness - For this source the trigger value is the number of minutes the player must be away past the time until homesick config in order for this effect to trigger.
slept_away - For this source the trigger value is how far (in blocks) the player must be from home for the effect to trigger.
slept_no_home - This source does not use the trigger value but a value must still be entered.
slept_at_home - For this source the trigger value is the required homeliness level for the effect to trigger.
in_home - For this source the trigger value is the required homeliness level for the effect to trigger.
```

remember for tiredness and homesickness the trigger value is not the total time the player has been awake / away. 
Its total time minus the configured timeUntilTired/timeUntilHomesick threshold for that effect.
so using the homesickness source with a trigger value of 1 will cause the effect to activate 1 minute after the player becomes homesick.

Effects applied when the player sleeps are temporary and require a duration is seconds. all other effects are permanent as long as the 
trigger condition is met.

### Homesickness
Homesickness is enabled once the player has a permanent home and is measured in days away from home.
Once the players days away from home pass the configured daysBeforeHomesick the homesickness effect will activate.
The effects of homesickness depends entirely on the effect config
Sleeping outside their home adds (by default) 15 minutes to the time away from home value.

As homesickness increases different and or more severe effects can be added to the player configurable via the effect configuration. 

Once the player returns to their home the time away value will decrease at a rate of 5 minutes for every minute they spend at home.
Sleeping at home will by default decrease time away by 80%

### UI
When looking at a bed a hid will be shown providing the user with information about their current home status.
Home sickness and tiredness stats will be shown in a separate hud when those effects are accurate.

There is also an unassigned by default key bind to show this info. Pressing CTRL while holding this key bind allows you
to move the hud elements.

### Sleeping
This mod completely overhauls the normal sleep mechanic. 
First of all players can sleep at any time as long as they have been awake for at least 15 minutes (configurable)
And they can not sleep if they are not tired even at nigh.
When a player sleeps during the day they will automatically wake up fully rested once the normal sleep time of 5 seconds passes.
If it is night time then instead of waking up immediately they will be given the option to wake up or wait for more players sleep / vote
to skip the night.
Once more than the configured percentage of players on the server (default 70%) are sleeping the night will be skipped.
If a player is unable to sleep at night because they are not tired they will be given an option to vote for day
via a clickable message in chat. This counts them towards the total number of players sleeping.

Players can now also sleep in all dimensions though it should be noted that bed explosions are only disabled
for the vanilla beds. Mod beds that also implement explosions will likely still explode.
When night is skipped it is skipped for every dimension where at at least 1 players is sleeping.
The end and nether are considered as always day. Whether or not time skipping works for mod dimension 
will depend on the mod.

## Commands

This mod uses some fairly advanced commands to help make the configuration process as easy as possible.

For general configuration running the `/home_sweet_home config list` command will display a list of all
configurable properties. hovering your mouse over these in the chat window will display info for each of them.
You can also click on a command in chat to auto fill the command required to set that property.

For effect configuration the command can walk you though the entire process of adding an effect.
Start by running `/home_sweet_home effect add` this will print out a list of all registered potion effects.
Choose one from the list and it will be added as the next parameter in the command window. Now ether leave the default
amplifier of 0 or change it to another value and press enter. A list off all effect sources will now be displayed. 
Select an effect source and add the trigger value to the end of the command. Now press enter to add the effect.
Depending on the command you may also need to add a duration for the effect.

You can view and easily remove configured effects via the one of the three list commands.

Once you understand the command parameters you can of course just fill them in manually. Also tab completion is a thing!

There are also player commands but these are very simple and should not require a detailed explanation. 
For now just use the help command to figure out how they work and what they do.

The following are the avalible help commands:<br>

`/home_sweet_home config help`<br>
`/home_sweet_home effect help`<br>
`/home_sweet_home player help`<br>
 