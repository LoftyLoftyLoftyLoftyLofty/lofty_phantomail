package games.lofty.phantomail.record;

import games.lofty.phantomail.Phantomail;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import games.lofty.phantomail.record.ModPayloadHandler;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPayloads
{
    public static void register(RegisterPayloadHandlersEvent event)
    {
        // Sets the current network version
        final PayloadRegistrar registrarRequestPhantomailGUIPacket = event.registrar("1");
        registrarRequestPhantomailGUIPacket.playToServer(
                PhantomailRequestStampGUIPacket.TYPE,
                PhantomailRequestStampGUIPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ModPayloadHandler::handleDataOnNetwork,
                        ModPayloadHandler::handleDataOnNetwork
                ));

        final PayloadRegistrar registrarUpdatePhantomailGUIPacket = event.registrar("1");
        registrarUpdatePhantomailGUIPacket.playToClient(
                PhantomailUpdateStampGUIPacket.TYPE,
                PhantomailUpdateStampGUIPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ModPayloadHandler::handleDataOnNetwork,
                        ModPayloadHandler::handleDataOnNetwork
                ));
    }

    public record PhantomailRequestStampGUIPacket(int paginationIndex, int selectionConfirmed, String chosenUUID) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<PhantomailRequestStampGUIPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Phantomail.MOD_ID, "phantomail_request_stamp_gui_packet"));

        public static final StreamCodec<ByteBuf, PhantomailRequestStampGUIPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                PhantomailRequestStampGUIPacket::paginationIndex,

                ByteBufCodecs.VAR_INT,
                PhantomailRequestStampGUIPacket::selectionConfirmed,

                ByteBufCodecs.STRING_UTF8,
                PhantomailRequestStampGUIPacket::chosenUUID,

                PhantomailRequestStampGUIPacket::new
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record PhantomailUpdateStampGUIPacket(String uuidSlot0, String displayNameSlot0) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<PhantomailUpdateStampGUIPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Phantomail.MOD_ID, "phantomail_update_stamp_gui_packet"));

        public static final StreamCodec<ByteBuf, PhantomailUpdateStampGUIPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                PhantomailUpdateStampGUIPacket::uuidSlot0,

                ByteBufCodecs.STRING_UTF8,
                PhantomailUpdateStampGUIPacket::displayNameSlot0,

                PhantomailUpdateStampGUIPacket::new
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
