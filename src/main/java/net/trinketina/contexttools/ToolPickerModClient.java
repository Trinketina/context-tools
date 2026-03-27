package net.trinketina.contexttools;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.trinketina.contexttools.config.ContextToolsConfig;
import net.trinketina.contexttools.config.ConfigData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolPickerModClient implements ClientModInitializer {
    public static int cooldown;
    public static final String MOD_ID = "context-tools";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ContextToolsConfig.class, GsonConfigSerializer::new);

        ConfigData.CONFIG = AutoConfig.getConfigHolder(ContextToolsConfig.class).getConfig();
    }
}
