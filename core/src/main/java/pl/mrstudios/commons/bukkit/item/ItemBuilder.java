package pl.mrstudios.commons.bukkit.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;
import java.util.UUID;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(@NotNull Material material) {
        this.item = new ItemStack(material);
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder(@NotNull Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = this.item.getItemMeta();
    }

    public @NotNull ItemBuilder name(@NotNull Component name) {
        if (this.meta != null)
            this.meta.displayName(name);
        return this;
    }

    public @NotNull ItemBuilder texture(@Nullable String texture) {
        if (texture == null || texture.isEmpty())
            return this;
        if (!(this.meta instanceof SkullMeta skullMeta))
            return this;
        try {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            String encoded = texture.startsWith("http")
                    ? Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + texture + "\"}}}").getBytes())
                    : texture;
            profile.setProperty(new ProfileProperty("textures", encoded));
            skullMeta.setPlayerProfile(profile);
        } catch (Exception ignored) {}
        return this;
    }

    public @NotNull ItemBuilder itemFlags(@NotNull ItemFlag... flags) {
        if (this.meta != null)
            this.meta.addItemFlags(flags);
        return this;
    }

    public @NotNull ItemStack build() {
        if (this.meta != null)
            this.item.setItemMeta(this.meta);
        return this.item;
    }

}
