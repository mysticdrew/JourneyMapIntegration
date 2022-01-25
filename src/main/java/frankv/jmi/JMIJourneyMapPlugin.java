package frankv.jmi;

import dev.ftb.mods.ftbchunks.client.FTBChunksClientConfig;
import frankv.jmi.ftbchunks.client.ClaimingMode;
import frankv.jmi.ftbchunks.client.ClaimedChunkPolygon;
import frankv.jmi.ftbchunks.client.ClaimingModeHandler;
import frankv.jmi.waypointmessage.WaypointChatMessage;
import frankv.jmi.waystones.client.WaystoneMarker;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.FullscreenMapEvent;
import journeymap.client.api.event.RegistryEvent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;

import static journeymap.client.api.event.ClientEvent.Type.*;

@ParametersAreNonnullByDefault
@journeymap.client.api.ClientPlugin
public class JMIJourneyMapPlugin implements IClientPlugin {
    private IClientAPI jmAPI = null;
    private ClaimedChunkPolygon claimedChunkPolygon;
    private ClaimingMode claimMode;
    public WaystoneMarker waystoneWaypoint;

    @Override
    public void initialize(final IClientAPI jmAPI) {
        this.jmAPI = jmAPI;
        JMIOverlayHelper.jmAPI = jmAPI;

        if (JMI.ftbchunks) {
            claimedChunkPolygon = new ClaimedChunkPolygon(jmAPI);
            claimMode = new ClaimingMode(jmAPI, claimedChunkPolygon);
            MinecraftForge.EVENT_BUS.register(claimedChunkPolygon);
            MinecraftForge.EVENT_BUS.register(claimMode);
        }

        if (JMI.waystones) {
            waystoneWaypoint = new WaystoneMarker(jmAPI);
            MinecraftForge.EVENT_BUS.register(waystoneWaypoint);
        }

        MinecraftForge.EVENT_BUS.register(WaypointChatMessage.class);

        this.jmAPI.subscribe(getModId(), EnumSet.of(MAPPING_STARTED, MAPPING_STOPPED, MAP_CLICKED, MAP_DRAGGED, MAP_MOUSE_MOVED, REGISTRY));
        JMI.LOGGER.info("Initialized " + getClass().getName());
    }

    @Override
    public String getModId() {
        return JMI.MODID;
    }

    @Override
    public void onEvent(ClientEvent event) {
        try {
            switch (event.type) {
                case MAPPING_STARTED:
                    if(JMI.ftbchunks) disableFTBChunksThings();
                    break;

                case MAPPING_STOPPED:
                    clearFTBChunksData();
                    WaystoneMarker.markers.clear();
                    jmAPI.removeAll(JMI.MODID);
                    JMI.LOGGER.debug("all elements removed.");
                    break;

                case MAP_CLICKED:
                    if (event instanceof FullscreenMapEvent.ClickEvent.Pre) {
                        ClaimingModeHandler.preClick((FullscreenMapEvent.ClickEvent) event);
                    }
                    break;

                case MAP_DRAGGED:
                    if (event instanceof FullscreenMapEvent.MouseDraggedEvent.Pre) {
                        ClaimingModeHandler.preDrag((FullscreenMapEvent.MouseDraggedEvent) event);
                    }
                    break;

                case MAP_MOUSE_MOVED:
                    ClaimingModeHandler.mouseMove((FullscreenMapEvent.MouseMoveEvent) event);
                    break;

                case REGISTRY:
                    RegistryEvent registryEvent = (RegistryEvent) event;

                    switch (registryEvent.getRegistryType()) {
                        case INFO_SLOT:
                            ((RegistryEvent.InfoSlotRegistryEvent)registryEvent).register(JMI.MODID, "jmi.theme.lablesource.claimed", 1000L, ClaimedChunkPolygon::getPolygonTitleByPlayerPos);
                            break;
                    }
                    break;
            }
        } catch (Throwable t) {
            JMI.LOGGER.error(t.getMessage(), t);
        }
    }

    private void disableFTBChunksThings() {
        if (!JMI.CLIENT_CONFIG.getDisableFTBFunction()) return;
        FTBChunksClientConfig.DEATH_WAYPOINTS.set(false);
        FTBChunksClientConfig.MINIMAP_ENABLED.set(false);
        FTBChunksClientConfig.IN_WORLD_WAYPOINTS.set(false);
    }

    private void clearFTBChunksData() {
        ClaimedChunkPolygon.chunkOverlays.clear();
        ClaimedChunkPolygon.chunkData.clear();
        ClaimedChunkPolygon.forceLoadedOverlays.clear();
        ClaimedChunkPolygon.queue.clear();
        ClaimingMode.claimAreaPolygons.clear();
        ClaimingModeHandler.dragPolygons.clear();
        ClaimingModeHandler.chunks.clear();
    }
}
