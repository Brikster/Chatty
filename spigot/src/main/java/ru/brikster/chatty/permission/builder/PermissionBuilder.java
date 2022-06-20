package ru.brikster.chatty.permission.builder;

public interface PermissionBuilder {

    default String getRootNode() {
        return "chatty";
    }

    PermissionBuilder appendNode(String node);

    String build();

}
