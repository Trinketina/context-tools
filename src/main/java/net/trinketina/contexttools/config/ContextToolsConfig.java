package net.trinketina.contexttools.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.*;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;

@Config(name = "context-tools")
public class ContextToolsConfig implements ConfigData {
    @PrefixText()
    public boolean requireControlHeldDown = false;
    public boolean requireCrouching = false;

    @PrefixText()
    @Tooltip()
    public boolean requireToolInHand = true;
}
