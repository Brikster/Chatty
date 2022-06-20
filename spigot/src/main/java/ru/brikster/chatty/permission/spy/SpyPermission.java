package ru.brikster.chatty.permission.spy;

import ru.brikster.chatty.permission.builder.PermissionBuilderImpl;

public class SpyPermission extends PermissionBuilderImpl {

    public SpyPermission() {
        appendNode("spy");
    }

    public String withChat(String chatName) {
        return appendNode(chatName).build();
    }

}
