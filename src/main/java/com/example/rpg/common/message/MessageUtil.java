package com.example.rpg.common.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MessageUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private MessageUtil() {
    }

    public static Component mm(String message) {
        return MM.deserialize(message);
    }

    public static Component red(String message) {
        return MM.deserialize("<red>" + message + "</red>");
    }

    public static Component blue(String message) {
        return MM.deserialize("<blue>" + message + "</blue>");
    }

    public static Component yellow(String message) {
        return MM.deserialize("<yellow>" + message + "</yellow>");
    }

    public static Component cyan(String message) {
        return MM.deserialize("<cyan>" + message + "</cyan>");
    }

    public static Component rainbow(String message) {
        return MM.deserialize("<rainbow>" + message + "</rainbow>");
    }

    public static Component green(String message) {
        return MM.deserialize("<green>" + message + "</green>");
    }

    public static Component gray(String message) {
        return MM.deserialize("<gray>" + message + "</gray>");
    }

    public static Component gold(String message) {
        return MM.deserialize("<gold>" + message + "</gold>");
    }
}
