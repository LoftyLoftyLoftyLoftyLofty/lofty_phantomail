package games.lofty.phantomail.record;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.core.jmx.Server;

public class ModPayloadHandler {

    public static void handleDataOnNetwork(final ModPayloads.PhantomailRequestStampGUIPacket data, final IPayloadContext context) {
        // Do something with the data, on the network thread
        //blah(data.name());

        // Do something with the data, on the main thread
        context.enqueueWork(() -> {
                    System.out.println("handleDataOnNetwork -> request stamp packet");
                    System.out.println("chosen uuid = " + data.chosenUUID());
                    PacketDistributor.sendToPlayer(((ServerPlayer)context.player()), new ModPayloads.PhantomailUpdateStampGUIPacket("00000000-0000-0000-0000-000000000001", "test name"));
                })
                .exceptionally(e -> {
                    // Handle exception
                    context.disconnect(Component.literal("Packet handling failure"));
                    return null;
                });
    }

    public static void handleDataOnNetwork(final ModPayloads.PhantomailUpdateStampGUIPacket data, final IPayloadContext context) {
        // Do something with the data, on the network thread
        //blah(data.name());

        // Do something with the data, on the main thread
        context.enqueueWork(() -> {
                    System.out.println("handleDataOnNetwork -> update stamp packet");
                    System.out.println("Received uuid slot 0: " + data.uuidSlot0());
                })
                .exceptionally(e -> {
                    // Handle exception
                    context.disconnect(Component.literal("Packet handling failure"));
                    return null;
                });
    }
}