/*
 * Copyright 2026 IQKV Foundation Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Shared domain model for audit logging across the IQKV platform.
 *
 * <p>This module contains neutral, implementation-independent data structures,
 * events, records, and enums used for consistent activity tracking.
 *
 * <p>It is designed to be used by:
 * <ul>
 *   <li>Service implementations (IAM, Billing, future domain services)</li>
 *   <li>Audit providers (default or custom)</li>
 *   <li>Client applications (Platform Admin UI)</li>
 * </ul>
 *
 * <h2>Package Structure</h2>
 * <ul>
 *   <li>{@code event/} — Domain events published via message broker (RabbitMQ)</li>
 *   <li>{@code record/} — Immutable records representing persisted audit entries</li>
 *   <li>{@code enum/} — Controlled vocabulary ({@link ActivityAction}, {@link EntityType}, etc.)</li>
 *   <li>{@code dto/} — Data Transfer Objects for API communication</li>
 *   <li>{@code actor/} — Types representing who performed an action</li>
 *   <li>{@code context/} — Contextual information (tenant, IP, correlation ID, etc.)</li>
 * </ul>
 *
 * <p>All classes in this module are intentionally lightweight and serializable
 * to support efficient event publishing and storage across different backends.
 *
 * @since 0.3.0
 * @see com.iqkv.foundation.audit.spi
 */
package com.iqkv.foundation.audit.model;
