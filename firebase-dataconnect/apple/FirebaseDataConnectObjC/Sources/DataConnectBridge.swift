// Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.

import FirebaseCore
import FirebaseDataConnect
import Foundation

/// The Objective-C face of one `DataConnect` instance, for Kotlin/Native: variables and data are JSON text, fetch
/// policies, data sources and cache storages are the integers below, and failures are `NSError`s of
/// ``DataConnectBridge/errorDomain`` whose `code` and `userInfo` carry what the SDK reports.
///
/// Codes (`NSError.code`):
/// - `0`: the operation could not be run (the request failed, or the variables or data could not be encoded or decoded).
/// - `1`: the operation ran and the server reported errors; `userInfo["errors"]` holds them as a JSON array of
///   `{ "message": "...", "path": [ "field", 0 ] }` and `userInfo["data"]` the JSON text of the data the server returned
///   nonetheless, when it did.
/// - `2`: a cache-only query for which the cache holds nothing.
///
/// Fetch policies: `0` prefer cache, `1` cache only, `2` server only. Data sources: `0` server, `1` cache. Cache
/// storages: `0` no cache, `1` memory, `2` persistent.
@objc(FDCBridge)
public final class DataConnectBridge: NSObject {
  /// The domain of the errors the bridge reports.
  @objc public static let errorDomain = "dev.gitlive.firebase.dataconnect"

  private let dataConnect: DataConnect

  /// The instance of the SDK for the app named `appName` (the default app is `__FIRAPP_DEFAULT`) and the connector; the
  /// SDK caches instances per app, connector and settings. `host` may carry a port (`host:port`), otherwise the port is
  /// 443 with SSL and 80 without.
  @objc public init(
    appName: String,
    serviceId: String,
    location: String,
    connector: String,
    host: String,
    sslEnabled: Bool,
    cacheStorage: Int,
    cacheMaxAgeSeconds: Double
  ) {
    let app = FirebaseApp.app(name: appName)
    let hostAndPort = host.split(separator: ":", maxSplits: 1).map(String.init)
    let defaultPort = sslEnabled ? 443 : 80
    let hostName = hostAndPort.first ?? host
    let port = hostAndPort.count > 1 ? (Int(hostAndPort[1]) ?? defaultPort) : defaultPort
    let cacheSettings: CacheSettings?
    switch cacheStorage {
    case 1: cacheSettings = CacheSettings(storage: .memory, maxAge: cacheMaxAgeSeconds)
    case 2: cacheSettings = CacheSettings(storage: .persistent, maxAge: cacheMaxAgeSeconds)
    default: cacheSettings = nil
    }
    let settings = DataConnectSettings(host: hostName, port: port, sslEnabled: sslEnabled, cacheSettings: cacheSettings)
    let config = ConnectorConfig(serviceId: serviceId, location: location, connector: connector)
    dataConnect = DataConnect.dataConnect(app: app, connectorConfig: config, settings: settings)
    super.init()
  }

  /// Points the instance at the emulator; must be called before any operation runs.
  @objc(useEmulator:port:)
  public func useEmulator(host: String, port: Int) {
    dataConnect.useEmulator(host: host, port: port)
  }

  /// Runs the query `name` with `variables` (a JSON object) and completes with the JSON text of its data and the data's
  /// source, or with an error.
  @objc(executeQuery:variables:fetchPolicy:completion:)
  public func executeQuery(
    _ name: String,
    variables: String,
    fetchPolicy: Int,
    completion: @escaping (String?, Int, NSError?) -> Void
  ) {
    let ref: QueryRefObservableObject<JSONValue, JSONVariables>
    do {
      let variables = JSONVariables(value: try JSONValue(json: variables))
      let anyRef = dataConnect.query(name: name, variables: variables, resultsDataType: JSONValue.self, publisher: .observableObject)
      guard let typedRef = anyRef as? QueryRefObservableObject<JSONValue, JSONVariables> else {
        completion(nil, 0, Self.nsError(code: 0, message: "unexpected query reference type \(type(of: anyRef))"))
        return
      }
      ref = typedRef
    } catch {
      completion(nil, 0, Self.nsError(for: error))
      return
    }
    let policy: QueryFetchPolicy
    switch fetchPolicy {
    case 1: policy = .cacheOnly
    case 2: policy = .serverOnly
    default: policy = .preferCache
    }
    Task {
      do {
        let result = try await ref.execute(fetchPolicy: policy)
        // The SDK completes a cache-only query the cache holds nothing for with no data rather than an error.
        guard let data = result.data else {
          completion(nil, 1, Self.nsError(code: 2, message: "no cached data for query \(name)"))
          return
        }
        completion(try data.json(), result.source == .cache ? 1 : 0, nil)
      } catch {
        completion(nil, 0, Self.nsError(for: error))
      }
    }
  }

  /// Runs the mutation `name` with `variables` (a JSON object) and completes with the JSON text of its data, or with an
  /// error.
  @objc(executeMutation:variables:completion:)
  public func executeMutation(_ name: String, variables: String, completion: @escaping (String?, NSError?) -> Void) {
    let ref: MutationRef<JSONValue, JSONVariables>
    do {
      let variables = JSONVariables(value: try JSONValue(json: variables))
      ref = dataConnect.mutation(name: name, variables: variables, resultsDataType: JSONValue.self)
    } catch {
      completion(nil, Self.nsError(for: error))
      return
    }
    Task {
      do {
        let result = try await ref.execute()
        completion(try (result.data ?? .null).json(), nil)
      } catch {
        completion(nil, Self.nsError(for: error))
      }
    }
  }

  // MARK: Errors

  private static func nsError(code: Int, message: String, userInfo: [String: Any] = [:]) -> NSError {
    var info = userInfo
    info[NSLocalizedDescriptionKey] = message
    return NSError(domain: errorDomain, code: code, userInfo: info)
  }

  /// The `NSError` of an error the SDK threw: a `DataConnectOperationError` with a response becomes code `1` carrying
  /// the errors and the raw data, anything else code `0`.
  private static func nsError(for error: Error) -> NSError {
    let dataConnectError: Error = (error as? AnyDataConnectError)?.dataConnectError ?? error
    guard let operationError = dataConnectError as? DataConnectOperationError else {
      return nsError(code: 0, message: describe(dataConnectError))
    }
    let message = operationError.message ?? operationError.debugDescription
    guard let response = operationError.response else {
      return nsError(code: 0, message: message)
    }
    var userInfo: [String: Any] = [:]
    let errors: [[String: Any]] = response.errors.map { info in
      let path: [Any] = (info.path ?? []).map { segment -> Any in
        switch segment {
        case let .field(field): return field
        case let .listIndex(index): return index
        }
      }
      return ["message": info.message, "path": path]
    }
    if let errorsData = try? JSONSerialization.data(withJSONObject: errors) {
      userInfo["errors"] = String(decoding: errorsData, as: UTF8.self)
    }
    if let rawJsonData = response.rawJsonData {
      userInfo["data"] = rawJsonData
    }
    return nsError(code: 1, message: message, userInfo: userInfo)
  }

  private static func describe(_ error: Error) -> String {
    if let dataConnectError = error as? DataConnectError {
      return dataConnectError.debugDescription
    }
    return String(describing: error)
  }
}
