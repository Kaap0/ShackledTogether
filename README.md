# ⛓️ ShackledTogether ⛓️

A serverside Spigot plugin for chaining players together

## Features

- ⛓️ Chain an infinite amount of players together ⛓️
- 🛠️
  Extensive [configurations](https://github.com/Kaap0/ShackledTogether/blob/main/src/main/resources/config.yml)
  🛠️
- 💪 Pull mechanic 💪
- 💥 Chain collision mechanic 💥

## Demo

<video src="https://github.com/Kaap0/ShackledTogether/raw/refs/heads/main/showcase/showcaseA.mp4" width="300"></video>

## Disclaimers

- This plugin has not been tested extensively, so critical bugs might occur.

- Chain collisions edge cases are not fully polished, so chains might get stuck or not collide.

- Development is still in progress.

## Background

This project started in summer of 2024, and had a plan to make it a premium plugin.
But the development stalled for this project at the time and it lacked quality. So I think that making this open and
free would benefit all.

## Roadmap

- Verlet ropes?

- Add support for branching chains

- Support for adding players

- Improve or even redesign collision handling

- Improve performance and API

## Version Support

- As of now this is tested on 1.21.8, should work well with older and newer versions too

## Commands

### Player Commands

- /shackle leave
    - Leaves the chain
        - permission: shackledtogether.leave

### Admin Commands

- /shackle create \<player> \<player> \<player> ...
    - Chains the Players together
        - permission: shackledtogether.create
- /shackle reload
    - Reloads Configs
        - shackledtogether.reload

## Installation

Download and place to plugins folder

- You will need [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) to see a chain

## API Usage

- Clone this repository and maven install this repository locally
- API Documentation is available
  at [ShackledTogetherAPI](https://github.com/Kaap0/ShackledTogether/blob/master/src/main/java/gg/kaapo/shackledtogether/chain/ShackledTogetherAPI.java)
  class
  and [Events](https://github.com/Kaap0/ShackledTogether/blob/master/src/main/java/gg/kaapo/shackledtogether/events)
  package
- API is subject to change

### plugin.yml

```
softdepend: [ ShackledTogether ]
```

### Pom.xml

        <dependency>
            <groupId>gg.kaapo</groupId>
            <artifactId>ShackledTogether</artifactId>
            <version>INSERT_PLUGIN_VERSION</version>
            <scope>provided</scope>
        </dependency>

### YourPlugin

```java
public final class YourPlugin extends JavaPlugin implements Listener {

    private ShackledTogetherAPI shackledTogetherAPI;

    @Override
    public void onEnable() {
        //Initialize the API
        shackledTogetherAPI = getShackledTogetherAPI();

        //Register listener if you want to listen for events
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    //Method to get the API
    private ShackledTogetherAPI getShackledTogetherAPI() {
        Plugin plugin = getServer().getPluginManager().getPlugin("ShackledTogether");

        if (plugin == null || !(plugin instanceof ShackledTogether)) {
            return null;
        }

        return ((ShackledTogether) plugin).getAPI();
    }

    private void yourMethod() {
        //Examples

        //List active chains
        List<Chain> chains = shackledTogetherAPI.getChains();

        //Creating a chain

        //Prepare players, ideally also add players.
        List<Player> players = new ArrayList<>();

        //Prepare configuration, this will default copy your config.yml values
        ChainConfiguration chainConfiguration = new ChainConfiguration();

        //Customize this chain instance
        chainConfiguration.setChainLength(10);
        chainConfiguration.setPullMechanic(false);

        //Create the chain (This returns ChainCreationResult if needed) (You can also pass chainConfiguration as null, so it will use and follow config.yml changes)
        shackledTogetherAPI.createChain(players, chainConfiguration);

        //Removing a player from a chain
        Chain chain = shackledTogetherAPI.getChain(player);
        chain.remove(player);

        //Changing the elasticity when chain is live
        chain.getChainConfiguration().setChainElasticity(0.1);

        //Stopping a chain
        chain.destroy();

    }

    //Example event listener

    @EventHandler
    public void onChainLeave(ChainLeaveEvent event) {
        Bukkit.getLogger().info(event.getPlayer().getName() + " left a chain because of " + event.getReason());
    }
}
```

## Authors

- [@Kaap0](https://www.github.com/Kaap0)
- [@dtzdev](https://github.com/dtzdev)

## License

[![AGPL License](https://img.shields.io/badge/license-AGPL-blue.svg)](http://www.gnu.org/licenses/agpl-3.0)