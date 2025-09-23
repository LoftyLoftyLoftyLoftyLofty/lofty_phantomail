package games.lofty.phantomail.item.custom.component;

import java.util.Objects;

// A record example
public record PhantomailStampDataComponent(String recipientUUID) {}

// A class example
/*
public class PhantomailStampDataComponent {

    private final String recipientUUID;

    public PhantomailStampDataComponent(String recipientUUID)
    {
        this.recipientUUID = recipientUUID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.recipientUUID);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else {
            return obj instanceof PhantomailStampDataComponent ex
                    && Objects.equals(recipientUUID, ex.recipientUUID);
        }
    }
}
*/