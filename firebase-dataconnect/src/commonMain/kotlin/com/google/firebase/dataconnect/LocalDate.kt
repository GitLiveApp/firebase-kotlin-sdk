/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

import com.google.firebase.dataconnect.serializers.LocalDateSerializer
import kotlinx.datetime.number
import kotlinx.serialization.Serializable

/**
 * A calendar date without a time zone, the Kotlin form of the Data Connect `Date` scalar. [month] and [day] are 1-based;
 * the values are not validated, so that dates from a server can be represented as sent.
 *
 * Plain common code shared by every platform (the Android SDK's class has the same members); the conversions to and from
 * `java.time.LocalDate` exist on the Android SDK only, use [toKotlinxLocalDate] and [toDataConnectLocalDate] from common code.
 */
@Serializable(with = LocalDateSerializer::class)
public class LocalDate(public val year: Int, public val month: Int, public val day: Int) {
    override fun equals(other: Any?): Boolean = other is LocalDate && other.year == year && other.month == month && other.day == day

    override fun hashCode(): Int = listOf(LocalDate::class, year, month, day).hashCode()

    override fun toString(): String = "LocalDate(year=$year, month=$month, day=$day)"
}

/** A copy of this date with the given values replaced. */
public fun LocalDate.copy(year: Int = this.year, month: Int = this.month, day: Int = this.day): LocalDate = LocalDate(year = year, month = month, day = day)

/** This date as a `kotlinx.datetime.LocalDate`, which validates it. */
public fun LocalDate.toKotlinxLocalDate(): kotlinx.datetime.LocalDate = kotlinx.datetime.LocalDate(year = year, month = month, day = day)

/** This `kotlinx.datetime.LocalDate` as a Data Connect [LocalDate]. */
public fun kotlinx.datetime.LocalDate.toDataConnectLocalDate(): LocalDate = LocalDate(year = year, month = month.number, day = day)
