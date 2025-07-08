package helper;

import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public enum RedisJsonCommand implements ProtocolCommand {
    JSON_SET("JSON.SET");

    private final byte[] raw;

    RedisJsonCommand(String command) {
        this.raw = SafeEncoder.encode(command);
    }

    @Override
    public byte[] getRaw() {
        return raw;
    }
}
