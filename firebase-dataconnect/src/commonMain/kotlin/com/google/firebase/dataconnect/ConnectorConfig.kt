/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

/** Identifies a Data Connect connector: the [connector] of the service [serviceId] in the region [location]. */
public class ConnectorConfig(
    public val connector: String,
    public val location: String,
    public val serviceId: String,
) {
    override fun equals(other: Any?): Boolean = other is ConnectorConfig && other.connector == connector && other.location == location && other.serviceId == serviceId

    override fun hashCode(): Int = listOf(ConnectorConfig::class, connector, location, serviceId).hashCode()

    override fun toString(): String = "ConnectorConfig(connector=$connector, location=$location, serviceId=$serviceId)"
}

/** A copy of this config with the given values replaced. */
public fun ConnectorConfig.copy(
    connector: String = this.connector,
    location: String = this.location,
    serviceId: String = this.serviceId,
): ConnectorConfig = ConnectorConfig(connector = connector, location = location, serviceId = serviceId)
