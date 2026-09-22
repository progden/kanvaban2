package io.progden.kanban.spring.web;

record SetItemCapabilitiesRequest(boolean movable, boolean resizable, boolean removable) {
}
