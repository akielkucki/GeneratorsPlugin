package com.gungens.generators.libs;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class GeneratorUtils {
    public static final GeneratorUtils instance = new GeneratorUtils();
    public List<ItemStack> deserializeItemStacks(String base64arr) {
        String[] parts = base64arr.split(";");
        List<ItemStack> items = new ArrayList<>();

        for (String part : parts) {
            if (part.isEmpty()) continue;
            try {
                byte[] data = Base64.getDecoder().decode(part);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                ItemStack item = (ItemStack) dataInput.readObject();
                dataInput.close();
                items.add(item);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize ItemStack", e);
            }
        }

        return items;
    }
    public static String serializeItemStacks(List<ItemStack> items) {
        StringBuilder serialized = new StringBuilder();
        for (ItemStack itemStack : items) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
                dataOutput.writeObject(itemStack);
                dataOutput.close();
                serialized.append(Base64.getEncoder().encodeToString(outputStream.toByteArray())).append(';');
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize ItemStack", e);
            }
        }
        return serialized.toString();
    }
}
