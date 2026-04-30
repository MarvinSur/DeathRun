package pl.mrstudios.deathrun.arena.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.commons.inject.annotation.Inject;
import pl.mrstudios.deathrun.arena.Arena;
import pl.mrstudios.deathrun.config.Configuration;

import static org.bukkit.event.EventPriority.MONITOR;
import static pl.mrstudios.deathrun.api.arena.enums.GameState.PLAYING;
import static pl.mrstudios.deathrun.api.arena.user.enums.Role.RUNNER;

public class ArenaBlockEffectListener implements Listener {

    private final Arena arena;
    private final Configuration configuration;

    @Inject
    public ArenaBlockEffectListener(
            @NotNull Arena arena,
            @NotNull Configuration configuration
    ) {
        this.arena = arena;
        this.configuration = configuration;
    }

    @EventHandler(priority = MONITOR)
    public void onStepOnBlockEffect(
            @NotNull PlayerMoveEvent event
    ) {

        // BUG-9 fix: gunakan || agar salah satu rotasi saja sudah cukup untuk skip (hanya noleh)
        if (
                event.getFrom().getBlockX() == event.getTo().getBlockX()
                        && event.getFrom().getBlockY() == event.getTo().getBlockY()
                        && event.getFrom().getBlockZ() == event.getTo().getBlockZ()
                        && (event.getFrom().getPitch() != event.getTo().getPitch()
                        || event.getFrom().getYaw() != event.getTo().getYaw())
        ) return;

        // BUG-5 fix: hanya aktif saat PLAYING dan hanya untuk Runner
        if (this.arena.getGameState() != PLAYING)
            return;

        var user = this.arena.getUser(event.getPlayer());
        if (user == null || user.getRole() != RUNNER)
            return;

        this.configuration.plugin().blockEffects
                .stream()
                .filter((effect) -> effect.blockType() == event.getTo().clone().add(0, -1, 0).getBlock().getType())
                .findFirst()
                .ifPresent((effect) -> event.getPlayer().addPotionEffect(new PotionEffect(effect.effectType(), (int) (20 * effect.duration()), effect.amplifier(), false, false, true)));

    }

}
