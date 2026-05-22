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

/**
 * Represents the actor who performed the audited action.
 *
 * @param id             Subject ID (e.g., user id)
 * @param type           Actor type (e.g., USER, SYSTEM, ANONYMOUS)
 * @param email          Email address of the actor
 * @param ipAddress      IP address of the actor
 * @param userAgent      User agent of the actor's client
 * @param impersonatorId If an admin is acting on behalf of a user, this is the admin's ID
 */
public record AuditActor(
    String id,
    String type,
    String email,
    String ipAddress,
    String userAgent,
    String impersonatorId
) implements Serializable {
}
