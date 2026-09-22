package io.progden.kanban.spring.web;

record PlaceItemRequest(String component, double x, double y, double width, double height, String anchor,
        Boolean movable, Boolean resizable, Boolean removable) {

    boolean movableOrDefault() {
        return movable == null || movable;
    }

    boolean resizableOrDefault() {
        return resizable == null || resizable;
    }

    boolean removableOrDefault() {
        return removable == null || removable;
    }
}
