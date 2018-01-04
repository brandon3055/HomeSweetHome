# Home Sweet Home Mod Mechanics

Everything that follows will be configurable.

---

### Homes

Homes have the following properties.
- Their location
- Their homeliness
- Is home permanent
- Radius (this is a base value + homeliness multiplied by a multiplier)

### Home mechanics
At the start a player has no home location.
When a player sleeps in a bed their home is set to that location.
This home is not permanent so if they sleep in a different location it will move and reset.

The more nights the player sleeps at a single home the more its homeliness increases.
Simply spending time within the home's radius will also increase homeliness but at a much slower rate.

Once the homeliness reaches a certain threshold the player will be given an option to make the home permanent.
At this point the home can not be moved.

Homeliness will be incremented logarithmically (similar to xp levels)
Meaning in the beginning its easy to increase homeliness but the the more 
homeliness increases the slower it increases.

Homeliness level will not be the same everywhere within the homes radius but will instead
be highest at the center of the home area and decrease towards the parameter of its radius.

Idea for moving homes (slightly)
If the player sleeps outside the center of their home but within the home's radius the center could move toward that 
location by some amount every time they sleep.

### Effects (all configurable)
Do you want me to just use vanilla/mod potion effects or implement something custom? 

While the player has no permanent home they will get a small de-buff every time they sleep.

Once the player has a home, sleeping within the homes radius will give the player positive effects.
The effect strength will be determined by the homeliness at their location.

May also have some permanent effects as long as the player is within their home.

### Homesickness
Homesickness is enabled once the player has a permanent home. 
As long as a player is outside their home radius their homesickness slowly increases.
The rate of increase would be determined by how far from home they are. (with some max increase rate)
Sleeping outside their home adds a significant amount of home sickness.

As homesickness increases the player will start to receive negative effects that increase in severity 
the longer the player stays away from home.

Once the player returns to their home homesickness will decrease rapidly and sleeping at home 
will clear it completely.

### Visuals
Still got a lot to figure out here but i am thinking about possibly 
rendering a tooltip when a player looks at a bed displaying information 
about the homeliness at the bed location

## Op Commands

### Configuration
All configuration will be handled via the following ingame commands
(you will also be able to edit the config json manually if needed)

<pre>
/hsh_config list
- Lists all configurable properties, their values and descriptions.
<br>
/hsh_config set < property> < value >
- Sets the value of the specified property.
<br>
/hsh_config get < property >
- Displays the value of the specified prperty
<br>
/hsh_config effects
- TBD
</pre>
   
### Other

<pre>
/hsh_reset < player >
- Resets the specified player's home (As if they have just logged in for the first time).
</pre>