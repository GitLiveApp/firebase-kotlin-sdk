// Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.

import Foundation
import FirebaseDataConnect

/// A JSON value: the form in which variables and data cross between Kotlin (as JSON text, produced and consumed with
/// kotlinx-serialization) and the Data Connect SDK (which encodes `Encodable` variables and decodes `Decodable` data
/// through JSON itself).
enum JSONValue: Codable, Hashable, Sendable {
  case null
  case bool(Bool)
  case number(Double)
  case string(String)
  case array([JSONValue])
  case object([String: JSONValue])

  init(from decoder: Decoder) throws {
    let container = try decoder.singleValueContainer()
    if container.decodeNil() {
      self = .null
    } else if let value = try? container.decode(Bool.self) {
      self = .bool(value)
    } else if let value = try? container.decode(Double.self) {
      self = .number(value)
    } else if let value = try? container.decode(String.self) {
      self = .string(value)
    } else if let value = try? container.decode([JSONValue].self) {
      self = .array(value)
    } else if let value = try? container.decode([String: JSONValue].self) {
      self = .object(value)
    } else {
      throw DecodingError.dataCorruptedError(in: container, debugDescription: "Unsupported JSON value")
    }
  }

  func encode(to encoder: Encoder) throws {
    var container = encoder.singleValueContainer()
    switch self {
    case .null: try container.encodeNil()
    case let .bool(value): try container.encode(value)
    case let .number(value): try container.encode(value)
    case let .string(value): try container.encode(value)
    case let .array(value): try container.encode(value)
    case let .object(value): try container.encode(value)
    }
  }

  /// The value of JSON text.
  init(json: String) throws {
    self = try JSONDecoder().decode(JSONValue.self, from: Data(json.utf8))
  }

  /// The JSON text of the value.
  func json() throws -> String {
    let encoder = JSONEncoder()
    encoder.outputFormatting = [.withoutEscapingSlashes]
    return String(decoding: try encoder.encode(self), as: UTF8.self)
  }
}

/// The variables of an operation, as the SDK's `OperationVariable`: encoded as the JSON object they were given as.
struct JSONVariables: OperationVariable {
  let value: JSONValue

  func encode(to encoder: Encoder) throws {
    try value.encode(to: encoder)
  }
}
