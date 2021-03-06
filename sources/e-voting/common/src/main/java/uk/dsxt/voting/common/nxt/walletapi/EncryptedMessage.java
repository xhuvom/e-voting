package uk.dsxt.voting.common.nxt.walletapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
class EncryptedMessage {
    String data;
    String nonce;

    @JsonCreator
    public EncryptedMessage(@JsonProperty("data") String data, @JsonProperty("nonce") String nonce) {
        this.data = data;
        this.nonce = nonce;
    }
}
