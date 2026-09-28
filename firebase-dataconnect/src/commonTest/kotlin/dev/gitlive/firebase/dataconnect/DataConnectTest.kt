/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect

import com.google.firebase.Timestamp
import com.google.firebase.dataconnect.AnyValue
import com.google.firebase.dataconnect.DataConnectException
import com.google.firebase.dataconnect.DataConnectOperationException
import com.google.firebase.dataconnect.DataConnectSettings
import com.google.firebase.dataconnect.DataSource
import com.google.firebase.dataconnect.EnumValue
import com.google.firebase.dataconnect.FirebaseDataConnect
import com.google.firebase.dataconnect.LocalDate
import com.google.firebase.dataconnect.OptionalVariable
import com.google.firebase.dataconnect.QueryRef
import com.google.firebase.dataconnect.getInstance
import com.google.firebase.dataconnect.serializers.EnumValueSerializer
import com.google.firebase.dataconnect.serializers.TimestampSerializer
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.dataconnect.serializers.UuidSerializer
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

/**
 * Executes the operations of the `kotlin` connector of `test/dataconnect` against the Data Connect emulator, through
 * the `com.google.firebase.dataconnect` API only: on Android the Android SDK runs them, elsewhere this module's REST
 * implementation does.
 */
@IgnoreForAndroidUnitTest
class DataConnectTest {

    enum class Status { ACTIVE, INACTIVE }

    object StatusSerializer : EnumValueSerializer<Status>(Status.entries)

    @Serializable
    data class CreatePersonVariables(
        val name: String,
        val age: OptionalVariable<Int?> = OptionalVariable.Undefined,
        val birthday: OptionalVariable<LocalDate?> = OptionalVariable.Undefined,
        val status: OptionalVariable<
            @Serializable(with = StatusSerializer::class)
            EnumValue<Status>?,
            > = OptionalVariable.Undefined,
        val tags: OptionalVariable<List<String>?> = OptionalVariable.Undefined,
        val extra: OptionalVariable<AnyValue?> = OptionalVariable.Undefined,
    )

    @Serializable
    data class CreatePersonWithIdVariables(@Serializable(with = UuidSerializer::class) val id: Uuid, val name: String)

    @Serializable
    data class PersonKey(@Serializable(with = UuidSerializer::class) val id: Uuid)

    @Serializable
    data class CreatePersonData(val person_insert: PersonKey)

    @Serializable
    data class PersonIdVariables(@Serializable(with = UuidSerializer::class) val id: Uuid)

    @Serializable
    data class Person(
        @Serializable(with = UuidSerializer::class) val id: Uuid,
        val name: String,
        val age: Int?,
        val birthday: LocalDate?,
        @Serializable(with = TimestampSerializer::class) val createdAt: Timestamp,
        @Serializable(with = StatusSerializer::class) val status: EnumValue<Status>?,
        val tags: List<String>?,
        val extra: AnyValue?,
    )

    @Serializable
    data class GetPersonData(val person: Person?)

    @Serializable
    data class PersonName(val name: String)

    @Serializable
    data class GetPersonNameData(val person: PersonName?)

    @Serializable
    data class PersonSummary(@Serializable(with = UuidSerializer::class) val id: Uuid, val name: String, val age: Int?)

    @Serializable
    data class ListPeopleData(val people: List<PersonSummary>)

    private lateinit var app: FirebaseApp
    private lateinit var dataConnect: FirebaseDataConnect

    @BeforeTest
    fun initializeFirebase() {
        app = Firebase.apps(context).firstOrNull() ?: Firebase.initialize(
            context,
            FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
        )
        dataConnect = FirebaseDataConnect.getInstance(app.compat, testConnector).apply {
            useEmulator(emulatorHost, 9399)
        }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        if (::dataConnect.isInitialized) dataConnect.suspendingClose()
        Firebase.apps(context).forEach { it.delete() }
    }

    private fun createPerson(variables: CreatePersonVariables) = dataConnect.mutation("createPerson", variables, CreatePersonData.serializer(), CreatePersonVariables.serializer())

    private fun getPerson(id: Uuid) = dataConnect.query("getPerson", PersonIdVariables(id), GetPersonData.serializer(), PersonIdVariables.serializer())

    @Test
    fun createAndGetPersonRoundTripsEveryScalar() = runTest {
        val name = "Ada ${Uuid.random()}"
        val variables = CreatePersonVariables(
            name = name,
            age = OptionalVariable.Value(36),
            birthday = OptionalVariable.Value(LocalDate(1815, 12, 10)),
            status = OptionalVariable.Value(EnumValue.Known(Status.ACTIVE)),
            tags = OptionalVariable.Value(listOf("math", "engines")),
            extra = OptionalVariable.Value(AnyValue(mapOf("engine" to "analytical", "notes" to 8.0, "published" to true, "list" to listOf("a", null)))),
        )
        val created = createPerson(variables).execute()
        val id = created.data.person_insert.id
        assertEquals(created.ref.operationName, "createPerson")

        val result = getPerson(id).execute(QueryRef.FetchPolicy.SERVER_ONLY)
        assertEquals(DataSource.SERVER, result.dataSource)
        val person = assertNotNull(result.data.person)
        assertEquals(id, person.id)
        assertEquals(name, person.name)
        assertEquals(36, person.age)
        assertEquals(LocalDate(1815, 12, 10), person.birthday)
        assertEquals(EnumValue.Known(Status.ACTIVE), person.status)
        assertEquals(listOf("math", "engines"), person.tags)
        assertEquals(AnyValue(mapOf("engine" to "analytical", "notes" to 8.0, "published" to true, "list" to listOf("a", null))), person.extra)
        assertTrue(person.createdAt.seconds > 1_700_000_000, "createdAt=${person.createdAt}")
    }

    @Test
    fun undefinedVariablesAreOmitted() = runTest {
        val id = createPerson(CreatePersonVariables(name = "Undefined")).execute().data.person_insert.id
        val person = assertNotNull(getPerson(id).execute(QueryRef.FetchPolicy.SERVER_ONLY).data.person)
        assertEquals("Undefined", person.name)
        assertNull(person.age)
        assertNull(person.birthday)
        assertNull(person.status)
        assertNull(person.tags)
        assertNull(person.extra)
    }

    @Test
    fun fetchPoliciesUseTheCache() = runTest {
        val id = createPerson(CreatePersonVariables(name = "Cached")).execute().data.person_insert.id
        val query = getPerson(id)
        assertFailsWith<DataConnectException> { query.execute(QueryRef.FetchPolicy.CACHE_ONLY) }
        assertEquals(DataSource.SERVER, query.execute(QueryRef.FetchPolicy.SERVER_ONLY).dataSource)
        assertEquals(DataSource.CACHE, query.execute().dataSource)
        assertEquals(DataSource.CACHE, query.execute(QueryRef.FetchPolicy.PREFER_CACHE).dataSource)
        // An equivalent query, and one decoding other fields, share the cached data.
        val equivalent = getPerson(id).execute(QueryRef.FetchPolicy.CACHE_ONLY)
        assertEquals(DataSource.CACHE, equivalent.dataSource)
        assertEquals("Cached", equivalent.data.person?.name)
        val names = query.withDataDeserializer(GetPersonNameData.serializer()).execute(QueryRef.FetchPolicy.CACHE_ONLY)
        assertEquals("Cached", names.data.person?.name)
        assertEquals(DataSource.SERVER, query.execute(QueryRef.FetchPolicy.SERVER_ONLY).dataSource)
    }

    @Test
    fun subscriptionEmitsResults() = runTest {
        val id = createPerson(CreatePersonVariables(name = "Subscribed")).execute().data.person_insert.id
        val query = getPerson(id)
        query.execute(QueryRef.FetchPolicy.SERVER_ONLY)
        val subscription = query.subscribe()
        assertEquals(query, subscription.query)
        // The first emission is the cached result; a fresh one follows once the reload the subscription triggers completes.
        val first = subscription.flow.first()
        assertEquals(query, first.query)
        assertEquals("Subscribed", first.result.getOrThrow().data.person?.name)
        assertEquals("Subscribed", query.withDataDeserializer(GetPersonNameData.serializer()).subscribe().flow.first().result.getOrThrow().data.person?.name)
    }

    @Test
    fun executionErrorsAreReported() = runTest {
        val id = Uuid.random()
        val create = dataConnect.mutation("createPersonWithId", CreatePersonWithIdVariables(id, "Twice"), CreatePersonData.serializer(), CreatePersonWithIdVariables.serializer())
        assertEquals(id, create.execute().data.person_insert.id)
        val exception = assertFailsWith<DataConnectOperationException> { create.execute() }
        assertTrue(exception.response.errors.isNotEmpty(), "errors of $exception")
        assertTrue(exception.message.orEmpty().isNotEmpty())
        // A request the server rejects outright (no such operation) fails too.
        assertFailsWith<DataConnectException> {
            dataConnect.query("noSuchQuery", Unit, Unit.serializer(), Unit.serializer()).execute(QueryRef.FetchPolicy.SERVER_ONLY)
        }
    }

    @Test
    fun listPeopleWithCopiedAndReshapedReferences() = runTest {
        val name = "Listed ${Uuid.random()}"
        val id = createPerson(CreatePersonVariables(name = name, age = OptionalVariable.Value(7))).execute().data.person_insert.id
        val list = dataConnect.query("listPeople", Unit, ListPeopleData.serializer(), Unit.serializer())
        val people = list.execute(QueryRef.FetchPolicy.SERVER_ONLY).data.people
        assertEquals(PersonSummary(id, name, 7), people.single { it.id == id })
        val copy = list.copy(callerSdkType = FirebaseDataConnect.CallerSdkType.Generated)
        assertEquals(FirebaseDataConnect.CallerSdkType.Generated, copy.callerSdkType)
        assertEquals("listPeople", copy.operationName)
        val renamed = list.withVariablesSerializer(PersonIdVariables(id), PersonIdVariables.serializer())
        assertEquals(id, renamed.variables.id)
        assertEquals(list.dataConnect, renamed.dataConnect)
    }

    @Test
    fun instancesAreCachedPerAppAndConnector() = runTest {
        assertSame(dataConnect, FirebaseDataConnect.getInstance(app.compat, testConnector))
        assertSame(dataConnect, FirebaseDataConnect.getInstance(testConnector))
        assertEquals(app.compat, dataConnect.app)
        assertEquals(testConnector, dataConnect.config)
        assertEquals(DataConnectSettings(), dataConnect.settings)
        assertFailsWith<IllegalArgumentException> { FirebaseDataConnect.getInstance(app.compat, testConnector, DataConnectSettings(host = "example.com")) }
        dataConnect.suspendingClose()
        val reopened = FirebaseDataConnect.getInstance(app.compat, testConnector)
        assertNotSame(dataConnect, reopened)
        dataConnect = reopened.apply { useEmulator(emulatorHost, 9399) }
    }
}
