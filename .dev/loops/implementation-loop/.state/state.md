# state

> 每輪覆寫，20 行內；給下一輪／驅動腳本快速回復現況（zero context）。

T-00-scaffold：Dev 輪完成，狀態 `review-pending`。
- `kanban-core`（Gradle library，Java 25 toolchain）、`kanban-spring`（Gradle，Spring Boot 4.1.1，依賴 kanban-core）、`kanban-frontend`（pnpm，Vite + React + TypeScript）三個骨架已建立，皆無業務邏輯。
- 根 Gradle 多模組：`settings.gradle.kts` 含 `kanban-core`／`kanban-spring`；`./gradlew clean build` 全綠（含各自一個 smoke/守門測試）。
- `kanban-frontend`：`pnpm run build`、`pnpm run test`（vitest + testing-library）皆綠。
- 待 Review：跑一次 `./gradlew clean build` 與 `cd kanban-frontend && pnpm install && pnpm run build && pnpm run test`，核對 kanban-core 確實不依賴 Spring（`NoSpringDependencyTest`）。
