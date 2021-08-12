/*
 * This file is part of MessageChannel, licensed under the MIT License (MIT).
 *
 * Copyright (c) Crypnotic <https://www.crypnotic.me>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.crypnotic.messagechannel.bukkit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ChatComponentArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import me.crypnotic.messagechannel.api.MessageChannelAPI;
import me.crypnotic.messagechannel.api.access.IMessageChannel;
import me.crypnotic.messagechannel.api.access.IRelay;
import me.crypnotic.messagechannel.api.exception.MessageChannelException;
import me.crypnotic.messagechannel.api.pipeline.PipelineMessage;
import me.crypnotic.messagechannel.core.MessageChannelCore;

import java.util.Arrays;

public class MessageChannelBukkit extends JavaPlugin implements IRelay {

    private IMessageChannel core;

    @Override
    public void onLoad() {
        this.core = new MessageChannelCore(this);

        try {
            MessageChannelAPI.setCore(core);
        } catch (MessageChannelException exception) {
            exception.printStackTrace();
        }
        
       
    }

    @Override
    public void onEnable() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "messagechannel:proxy");
        getServer().getMessenger().registerIncomingPluginChannel(this, "messagechannel:server", (channel, player, data) -> core.getPipelineRegistry().receive(data));
    
        new CommandAPICommand("messagechannel")
        .withArguments(
            new EntitySelectorArgument("target", EntitySelectorArgument.EntitySelector.ONE_PLAYER),
            new StringArgument("target plugin"),
            new ChatComponentArgument("json message")
        )
        .executesPlayer((sender, args) -> {
            try {
                var target = (Player) args[0];
                var targetPlugin = (String) args[1];
                var jsonMessage = new JsonParser().parse((String) args[2]).getAsJsonObject();
    
                MessageChannelAPI.getPipelineRegistry().getPluginAccessPoint(targetPlugin).sendRequest(target.getName(), jsonMessage);
            } catch (Exception e) {
                Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).forEach(sender::sendMessage);
            }
        })
        .register();
    
    }

    @Override
    public boolean send(PipelineMessage message, byte[] data) {
        if (getServer().getOnlinePlayers().size() > 0) {
            Player player = (Player) getServer().getOnlinePlayers().toArray()[0];
            if (player != null) {
                player.sendPluginMessage(this, "messagechannel:proxy", data);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean broadcast(PipelineMessage message, byte[] data) {
        return false;
    }

    @Override
    public boolean isProxy() {
        return false;
    }
}