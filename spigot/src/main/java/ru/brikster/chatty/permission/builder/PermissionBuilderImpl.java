package ru.brikster.chatty.permission.builder;

public class PermissionBuilderImpl implements PermissionBuilder {

    private String permission = getRootNode();

    @Override
    public PermissionBuilder appendNode(String node) {
        permission = permission + "." + node;
        return this;
    }

    @Override
    public String build() {
        return permission;
    }

}
