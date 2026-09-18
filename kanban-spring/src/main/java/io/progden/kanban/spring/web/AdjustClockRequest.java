package io.progden.kanban.spring.web;

import java.time.Instant;

record AdjustClockRequest(Instant newTime) {
}
