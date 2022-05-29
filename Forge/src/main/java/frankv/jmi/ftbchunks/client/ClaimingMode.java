package frankv.jmi.ftbchunks.client;

import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import dev.ftb.mods.ftblibrary.math.XZ;
import frankv.jmi.JMI;
import frankv.jmi.JMIOverlayHelper;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.IThemeButton;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.event.forge.FullscreenDisplayEvent;
import journeymap.client.api.model.*;
import journeymap.client.api.util.PolygonHelper;
import journeymap.client.ui.component.JmUI;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class ClaimingMode {
    private IClientAPI jmAPI;
    private ClaimedChunkPolygon claimedChunkPolygon;
    public static boolean activated = false;
    private static Minecraft mc = Minecraft.getInstance();
    public static PolygonOverlay claimAreaPolygon = null;
    public static Set<ChunkPos> area = new HashSet<>();

    public ClaimingMode(IClientAPI jmAPI, ClaimedChunkPolygon claimedChunkPolygon) {
        this.jmAPI = jmAPI;
        this.claimedChunkPolygon = claimedChunkPolygon;
    }

    @SubscribeEvent
    public void onAddonButtonDisplay(FullscreenDisplayEvent.AddonButtonDisplayEvent event) {
        var buttonDisplay = event.getThemeButtonDisplay();
        buttonDisplay.addThemeToggleButton("FTBChunks Claiming Mode", "FTBChunks Claiming Mode", "grid", activated, b -> buttonControl(b));
    }

    @SubscribeEvent
    public void onGuiScreen(ScreenOpenEvent event) {
        if (!activated) return;
        if (event.getScreen() instanceof JmUI && event.getScreen() != null) return;
        activated = false;
        removeOverlays();
    }

    private void buttonControl(IThemeButton button) {
        if (mc.player == null) return;
        if (!activated) {
            activated = true;
            createClaimingAreaOverlays();
            button.setToggled(true);
        } else {
            activated = false;
            removeOverlays();
            button.setToggled(false);
        }
    }

    private void removeOverlays() {
        if (claimAreaPolygon == null) return;
        jmAPI.remove(claimAreaPolygon);

        claimedChunkPolygon.showForceLoadedByArea(false);
        claimAreaPolygon = null;
        area.clear();
    }

    public static PolygonOverlay dragPolygon(XZ xz) {
        var player = Minecraft.getInstance().player;
        var displayId = "drag_polygon_" + xz.x + "_" + xz.z;

        var shapeProps = new ShapeProperties()
                .setStrokeWidth(0)
                .setFillColor(0xffffff).setFillOpacity(.3f);

        var polygon = PolygonHelper.createChunkPolygon(xz.x, 10, xz.z);

        return new PolygonOverlay(JMI.MOD_ID, displayId, player.clientLevel.dimension(), shapeProps, polygon);
    }

    public static PolygonOverlay forceLoadedPolygon(ChunkDimPos pos) {
        var player = Minecraft.getInstance().player;
        var displayId = "ftb_force_loaded_" + pos.x + "_" + pos.z;

        var shapeProps = new ShapeProperties()
                .setStrokeWidth(2).setStrokeColor(0xff0000)
                .setFillOpacity(0f);

        var polygon = PolygonHelper.createChunkPolygon(pos.x, 10, pos.z);

        return new PolygonOverlay(JMI.MOD_ID, displayId, player.clientLevel.dimension(), shapeProps, polygon);
    }

    private void createClaimingAreaOverlays() {
        var player = Minecraft.getInstance().player;
        var startPoint = new ChunkPos(player.chunkPosition().x-7, player.chunkPosition().z-7);

        var displayId = "claim_mode";
        var shapeProps = new ShapeProperties()
                .setStrokeWidth(3)
                .setStrokeColor(0xffffff).setStrokeOpacity(1.0f)
                .setFillOpacity(0f);

        for (var x=0; x<15; x++) {
            for (var z=0; z<15; z++){
                this.area.add(new ChunkPos(startPoint.x+x, startPoint.z+z));
            }
        }

        claimedChunkPolygon.showForceLoadedByArea(true);

        var polygons = PolygonHelper.createChunksPolygon(this.area, 100);

        var overlay = new PolygonOverlay(JMI.MOD_ID, displayId, player.level.dimension(), shapeProps, polygons.get(0));
        if (JMIOverlayHelper.createPolygon(overlay)) claimAreaPolygon = overlay;
    }
}
