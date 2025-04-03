package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmProperties;
import net.minecraft.world.item.ItemStack;

public interface SensitiveFilmItem extends FilmItem {
    default FilmProperties getFilmProperties(ItemStack itemStack) {
        return itemStack.getOrDefault(Exposure.DataComponents.FILM_PROPERTIES, FilmProperties.EMPTY);
    }
}
