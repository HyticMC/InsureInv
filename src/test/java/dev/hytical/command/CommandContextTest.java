package dev.hytical.command;

import dev.hytical.InsureInv;
import dev.hytical.economy.EconomyManager;
import dev.hytical.managers.ConfigManager;
import dev.hytical.i18n.MessageManager;
import dev.hytical.storages.StorageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("CommandContext Tests")
class CommandContextTest {

    private InsureInv mockPlugin;
    private ConfigManager mockConfigManager;
    private StorageManager mockStorageManager;
    private EconomyManager mockEconomyManager;
    private MessageManager mockMessageManager;

    @BeforeEach
    void setUp() {
        mockPlugin = mock(InsureInv.class);
        mockConfigManager = mock(ConfigManager.class);
        mockStorageManager = mock(StorageManager.class);
        mockEconomyManager = mock(EconomyManager.class);
        mockMessageManager = mock(MessageManager.class);
    }

    private CommandContext createContext(CommandSender sender, String... args) {
        return new CommandContext(
                sender,
                args,
                mockPlugin,
                mockConfigManager,
                mockStorageManager,
                mockEconomyManager,
                mockMessageManager);
    }

    @Test
    @DisplayName("arg() should return correct value at valid index")
    void arg_validIndex_returnsValue() {
        CommandSender mockSender = mock(CommandSender.class);
        CommandContext ctx = createContext(mockSender, "buy", "10", "test");
        assertEquals("buy", ctx.arg(0));
        assertEquals("10", ctx.arg(1));
        assertEquals("test", ctx.arg(2));
    }

    @Test
    @DisplayName("arg() should return null for out-of-bounds index")
    void arg_invalidIndex_returnsNull() {
        CommandSender mockSender = mock(CommandSender.class);
        CommandContext ctx = createContext(mockSender, "buy");
        assertNull(ctx.arg(1));
        assertNull(ctx.arg(10));
    }

    @Test
    @DisplayName("argInt() should parse valid integer correctly")
    void argInt_validInteger_returnsValue() {
        CommandSender mockSender = mock(CommandSender.class);
        CommandContext ctx = createContext(mockSender, "buy", "42", "-5");
        assertEquals(42, ctx.argInt(1));
        assertEquals(-5, ctx.argInt(2));
    }

    @Test
    @DisplayName("argInt() should return null for non-integer string")
    void argInt_invalidInteger_returnsNull() {
        CommandSender mockSender = mock(CommandSender.class);
        CommandContext ctx = createContext(mockSender, "buy", "abc", "3.14");
        assertNull(ctx.argInt(1));
        assertNull(ctx.argInt(2));
    }

    @Test
    @DisplayName("argDouble() should parse valid double correctly")
    void argDouble_validDouble_returnsValue() {
        CommandSender mockSender = mock(CommandSender.class);
        CommandContext ctx = createContext(mockSender, "setprice", "99.99", "-10.5");
        assertEquals(99.99, ctx.argDouble(1));
        assertEquals(-10.5, ctx.argDouble(2));
    }

    @Test
    @DisplayName("argDouble() should return null for non-numeric string")
    void argDouble_invalidDouble_returnsNull() {
        CommandSender mockSender = mock(CommandSender.class);
        CommandContext ctx = createContext(mockSender, "setprice", "invalid");
        assertNull(ctx.argDouble(1));
    }

    @Test
    @DisplayName("player property should return null when sender is not Player")
    void player_whenSenderIsNotPlayer_returnsNull() {
        CommandSender mockSender = mock(CommandSender.class);
        CommandContext ctx = createContext(mockSender, "test");
        assertNull(ctx.getPlayer());
    }

    @Test
    @DisplayName("player property should return Player when sender is Player")
    void player_whenSenderIsPlayer_returnsPlayer() {
        Player mockPlayer = mock(Player.class);
        CommandContext ctx = createContext(mockPlayer, "test");
        assertEquals(mockPlayer, ctx.getPlayer());
    }

    @Test
    @DisplayName("playerOrThrow should throw when sender is not Player")
    void playerOrThrow_whenSenderIsNotPlayer_throws() {
        CommandSender mockSender = mock(CommandSender.class);
        CommandContext ctx = createContext(mockSender, "test");
        assertThrows(IllegalStateException.class, ctx::getPlayerOrThrow);
    }
}
