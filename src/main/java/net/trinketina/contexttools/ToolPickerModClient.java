package net.trinketina.contexttools;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolPickerModClient implements ClientModInitializer {
    public static int cooldown;
    public static final String MOD_ID = "context-tools";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
    }
}
