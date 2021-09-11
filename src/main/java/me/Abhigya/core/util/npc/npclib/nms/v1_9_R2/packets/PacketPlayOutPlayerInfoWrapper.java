/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package me.Abhigya.core.util.npc.npclib.nms.v1_9_R2.packets;

import me.Abhigya.core.util.npc.npclib.tinyprotocol.Reflection;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_9_R2.WorldSettings;

import java.util.Collections;
import java.util.List;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutPlayerInfoWrapper {

    private final Class<?> packetPlayOutPlayerInfoClazz = Reflection.getMinecraftClass("PacketPlayOutPlayerInfo");
    private final Class<?> playerInfoDataClazz = Reflection.getMinecraftClass("PacketPlayOutPlayerInfo$PlayerInfoData");
    private final Reflection.ConstructorInvoker playerInfoDataConstructor = Reflection.getConstructor(playerInfoDataClazz,
            packetPlayOutPlayerInfoClazz, GameProfile.class, int.class, WorldSettings.EnumGamemode.class, IChatBaseComponent.class);

    public PacketPlayOutPlayerInfo create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction action, GameProfile gameProfile, String name) {
        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        Reflection.getField(packetPlayOutPlayerInfo.getClass(), "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.class)
                .set(packetPlayOutPlayerInfo, action);

        Object playerInfoData = playerInfoDataConstructor.invoke(packetPlayOutPlayerInfo, gameProfile, 1, WorldSettings.EnumGamemode.NOT_SET,
                IChatBaseComponent.ChatSerializer.b("{\"text\":\"[NPC] " + name + "\",\"color\":\"dark_gray\"}"));

        Reflection.FieldAccessor<List> fieldAccessor = Reflection.getField(packetPlayOutPlayerInfo.getClass(), "b", List.class);
        fieldAccessor.set(packetPlayOutPlayerInfo, Collections.singletonList(playerInfoData));

        return packetPlayOutPlayerInfo;
    }

}
