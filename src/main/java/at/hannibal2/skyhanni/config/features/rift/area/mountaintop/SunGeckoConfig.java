package at.hannibal2.skyhanni.config.features.rift.area.mountaintop;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.config.features.rift.CruxTalismanDisplayConfig;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class SunGeckoConfig {
    @Expose
    @ConfigOption(name = "Combo Display", desc = "Displays the hits 2 left until a combo increase.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean comboDisplay = true;

    @Expose
    @ConfigOption(name = "Always Render", desc = "Always renders the display.")
    @ConfigEditorBoolean
    public boolean alwaysRender = false;

    @Expose
    @ConfigOption(name = "Trace to Combo Buff", desc = "Draws a line to any combo buffs in the area.")
    @ConfigEditorBoolean
    public boolean comboBuffTrace = true;

    @Expose
    @ConfigOption(name = "Time Left Threshold", desc = "The amount of time left needed to display the timer.")
    @ConfigEditorSlider(minValue = 0f, maxValue = 5, minStep = 0.1f)
    public float timeLeftThreshold = 2.5f;

    @Expose
    @ConfigLink(owner = SunGeckoConfig.class, field = "comboDisplay")
    public Position position = new Position(144, 139, false, true);

    @Expose
    @ConfigLink(owner = SunGeckoConfig.class, field = "comboDisplay")
    public Position position2 = new Position(144, 179, false, true);
}
