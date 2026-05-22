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

package com.iqkv.foundation.audit.model.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.iqkv.foundation.audit.model.enums.ActivitySeverity;

/**
 * Represents a normalized audit event in the platform.
 *
 * @param id           Unique identifier for this event
 * @param action       The action performed (e.g., USER_LOGIN, INVOICE_PAID)
 * @param entityType   The type of entity affected (e.g., USER, TENANT, INVOICE)
 * @param entityId     The ID of the entity affected
 * @param actor        Information about who performed the action
 * @param tenantKey    The tenant context for the action
 * @param severity     The severity level of the action
 * @param details      Dynamic metadata associated with the event
 * @param occurredAt   When the event occurred
 * @param correlationId Trace ID for correlating logs across services
 */
public record AuditEvent(
    UUID id,
    String action,
    String entityType,
    String entityId,
    AuditActor actor,
    String tenantKey,
    ActivitySeverity severity,
    Map<String, Object> details,
    Instant occurredAt,
    String correlationId
) implements Serializable {

  /**
   * Compact constructor to ensure occurredAt and id are never null.
   */
  public AuditEvent {
    if (id == null) {
      id = UUID.randomUUID();
    }
    if (occurredAt == null) {
      occurredAt = Instant.now();
    }
    if (details == null) {
      details = Map.of();
    }
  }
}
