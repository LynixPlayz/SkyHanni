package at.hannibal2.skyhanni.config.features.rift.area.mountaintop;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MountaintopConfig {
    @ConfigOption(name = "Sun Gecko", desc = "")
    @Accordion
    @Expose
    public SunGeckoConfig sunGecko = new SunGeckoConfig();
}
